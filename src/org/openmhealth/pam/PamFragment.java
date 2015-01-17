
package org.openmhealth.pam;

import java.io.IOException;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;
import org.openmhealth.android.pam.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import edu.cornell.tech.smalldata.omhclientlib.schema.PamSchema;
import edu.cornell.tech.smalldata.omhclientlib.services.OmhDsuWriter;

public class PamFragment extends Fragment {
    Bitmap[] images;
    int[] imageIds;
    private final Random random = new Random();

    private static LocationManager locationManager;
    private static String pam_photo_id;
    private static Location userLocation;

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private Button reload;
    private GridView gridview;
    private int selection = GridView.INVALID_POSITION;
    private Button submit;
    private PamProbeWriter mProbeWriter;

    /**
     * If true, we will send the response to ohmage with the probe writer
     */
    private boolean mSendResponse = false;

    public static final String[] IMAGE_FOLDERS = new String[] {
            "1_afraid",
            "2_tense",
            "3_excited",
            "4_delighted",
            "5_frustrated",
            "6_angry",
            "7_happy",
            "8_glad",
            "9_miserable",
            "10_sad",
            "11_calm",
            "12_satisfied",
            "13_gloomy",
            "14_tired",
            "15_sleepy",
            "16_serene"
    };

    public static PamFragment getInstance(boolean sendResponse) {
        PamFragment fragment = new PamFragment();
        fragment.mSendResponse = sendResponse;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadImages();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if ((VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
			if (mProbeWriter != null)
				mProbeWriter.close();
		}
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);

        locationManager = (LocationManager)
                getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location gpsloc =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location netloc =
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (isBetterLocation(gpsloc, netloc)) {
            userLocation = gpsloc;
        } else {
            userLocation = netloc;
        }

        if ((VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
			mProbeWriter = new PamProbeWriter(getActivity());
		}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.pam, container, false);

        gridview = (GridView) view.findViewById(R.id.pam_grid);
        reload = (Button) view.findViewById(R.id.pam_reload);
        reload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadImages();
                setupPAM();
            }

        });
        submit = (Button) view.findViewById(R.id.post_submit);
        submit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check PAM input
                if (null == pam_photo_id) {
                    Toast toast = Toast.makeText(getActivity(), "Please select a picture!",
                            Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    if (mSendResponse && (VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP))
                        mProbeWriter.writeResponse(userLocation, buildResponseJson(pam_photo_id));
                    
                    writeResponseToOmhDsu();

                    Bundle extras = buildResponseBundle(pam_photo_id);
                    extras.putString("feedback", "You selected: " + pam_photo_id.split("_")[1]);
                    Intent results = new Intent();
                    results.putExtras(extras);
                    getActivity().setResult(Activity.RESULT_OK, results);
                    getActivity().finish();
                }
            }
        });

        setupPAM();

        return view;
    }

    /**
     * Determines whether one Location reading is better than the current
     * Location fix
     * 
     * @param location The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to
     *            compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }
        if (location == null) {
            return false;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use
        // the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be
            // worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and
        // accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    private void loadImages() {
        images = new Bitmap[IMAGE_FOLDERS.length];
        imageIds = new int[IMAGE_FOLDERS.length];

        AssetManager assets = getResources().getAssets();
        String subFolder;
        for (int i = 0; i < IMAGE_FOLDERS.length; i++) {
            subFolder = "pam_images/" + IMAGE_FOLDERS[i];
            try {
                String filename = assets.list(subFolder)[random.nextInt(3)];
                images[i] = BitmapFactory.decodeStream(assets.open(subFolder + "/" + filename));
                imageIds[i] = filename.split("_")[1].charAt(0) - '0';
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void setupPAM() {
        // Start PAM
        gridview.setAdapter(new BaseAdapter() {

            private final int width = getActivity().getWindowManager().getDefaultDisplay()
                    .getWidth();

            @Override
            public int getCount() {
                return images.length;
            }

            @Override
            public Object getItem(int arg0) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ImageView imageView;
                if (null == convertView) {
                    imageView = new ImageView(getActivity());
                    imageView.setLayoutParams(new GridView.LayoutParams(width / 4, width / 4));
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageView.setColorFilter(null);
                } else {
                    imageView = (ImageView) convertView;
                }
                imageView.setImageBitmap(images[position]);

                if (position == selection)
                    highlightSelection(imageView);

                return imageView;
            }
        });

        gridview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (selection != GridView.INVALID_POSITION)
                    ((ImageView) parent.getChildAt(selection)).setColorFilter(null);
                highlightSelection(v);
                selection = position;
                pam_photo_id = IMAGE_FOLDERS[position];
            }
        });
    }

    private void highlightSelection(View v) {
        ((ImageView) v).setColorFilter(0xffff9933, PorterDuff.Mode.MULTIPLY);
    }

    protected JSONObject buildResponseJson(String photoId) {
        JSONObject photo = new JSONObject();
        try {
            int idx = Integer.valueOf(pam_photo_id.split("_")[0]);
            photo.put("score", idx);
            photo.put("photo_id", idx);
            photo.put("sub_photo_id", imageIds[idx-1]);
            photo.put("mood", pam_photo_id.split("_")[1]);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return photo;
    }

    protected Bundle buildResponseBundle(String photoId) {
        // Send photo id as the result of this activity
        Bundle extras = new Bundle();

        int idx = Integer.valueOf(pam_photo_id.split("_")[0]);
        extras.putDouble("score", idx);
        extras.putInt("photo_id", idx);
        extras.putInt("sub_photo_id", imageIds[idx-1]);
        extras.putString("mood", pam_photo_id.split("_")[1]);
        return extras;
    }

	private void writeResponseToOmhDsu() {
		
		int idx = Integer.valueOf(pam_photo_id.split("_")[0]);
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));;
		PamSchema pamSchema = new PamSchema(idx, calendar);
		
		OmhDsuWriter.writeDataPoint(getActivity(), pamSchema);
	}
}
