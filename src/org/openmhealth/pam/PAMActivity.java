
package org.openmhealth.pam;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PAMActivity extends Activity {
    private String[] filenames;
    private final Random random = new Random();

    private static LocationManager locationManager;
    private static String pam_photo_id = "";
    private static Location userLocation;

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private static final Pattern p = Pattern.compile("\\d+_\\d+");
    private Button reload;
    private GridView gridview;
    private View prevSelection;
    private Button submit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pam);

        gridview = (GridView) findViewById(R.id.pam_grid);
        reload = (Button) findViewById(R.id.pam_reload);
        reload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setupPAM();
            }

        });
        submit = (Button) findViewById(R.id.post_submit);
        submit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check PAM input
                if (null == pam_photo_id) {
                    Toast toast = Toast.makeText(PAMActivity.this, "Please select a picture!",
                            Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    // Submit response to ohmage
                }
            }
        });

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Location gpsloc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location netloc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (isBetterLocation(gpsloc, netloc)) {
            userLocation = gpsloc;
        } else {
            userLocation = netloc;
        }

        // set up pam
        setupPAM();
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

    /** setup PAM */
    private void setupPAM() {
        try {
            String[] files = {
                    "pam_images/1_afraid/"
                            + this.getResources().getAssets().list("pam_images/1_afraid")[random
                                    .nextInt(2)],
                    "pam_images/2_tense/"
                            + this.getResources().getAssets().list("pam_images/2_tense")[random
                                    .nextInt(2)],
                    "pam_images/3_excited/"
                            + this.getResources().getAssets().list("pam_images/3_excited")[random
                                    .nextInt(2)],
                    "pam_images/4_delighted/"
                            + this.getResources().getAssets().list("pam_images/4_delighted")[random
                                    .nextInt(2)],
                    "pam_images/5_frustrated/"
                            + this.getResources().getAssets().list("pam_images/5_frustrated")[random
                                    .nextInt(2)],
                    "pam_images/6_angry/"
                            + this.getResources().getAssets().list("pam_images/6_angry")[random
                                    .nextInt(2)],
                    "pam_images/7_happy/"
                            + this.getResources().getAssets().list("pam_images/7_happy")[random
                                    .nextInt(2)],
                    "pam_images/8_glad/"
                            + this.getResources().getAssets().list("pam_images/8_glad")[random
                                    .nextInt(2)],
                    "pam_images/9_miserable/"
                            + this.getResources().getAssets().list("pam_images/9_miserable")[random
                                    .nextInt(2)],
                    "pam_images/10_sad/"
                            + this.getResources().getAssets().list("pam_images/10_sad")[random
                                    .nextInt(2)],
                    "pam_images/11_calm/"
                            + this.getResources().getAssets().list("pam_images/11_calm")[random
                                    .nextInt(2)],
                    "pam_images/12_satisfied/"
                            + this.getResources().getAssets().list("pam_images/12_satisfied")[random
                                    .nextInt(2)],
                    "pam_images/13_gloomy/"
                            + this.getResources().getAssets().list("pam_images/13_gloomy")[random
                                    .nextInt(2)],
                    "pam_images/14_tired/"
                            + this.getResources().getAssets().list("pam_images/14_tired")[random
                                    .nextInt(2)],
                    "pam_images/15_sleepy/"
                            + this.getResources().getAssets().list("pam_images/15_sleepy")[random
                                    .nextInt(2)],
                    "pam_images/16_serene/"
                            + this.getResources().getAssets().list("pam_images/16_serene")[random
                                    .nextInt(2)]
            };
            filenames = files;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Start PAM
        gridview.setAdapter(new BaseAdapter() {

            private final int width = getWindowManager().getDefaultDisplay().getWidth();

            @Override
            public int getCount() {
                return filenames.length;
            }

            @Override
            public Object getItem(int arg0) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @SuppressWarnings("finally")
            @Override
            public View getView(int position, View convertView,
                    ViewGroup parent) {
                ImageView imageView;
                if (null == convertView) {
                    imageView = new ImageView(PAMActivity.this);
                    imageView.setLayoutParams(new GridView.LayoutParams(width / 4, width / 4));
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                } else {
                    imageView = (ImageView) convertView;
                }
                try {
                    imageView.setImageBitmap(getBitmapFromAsset(filenames[position]));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    return imageView;
                }
            }

            private Bitmap getBitmapFromAsset(String strName) throws IOException
            {
                AssetManager assetManager = getAssets();

                InputStream istr = assetManager.open(strName);
                Bitmap bitmap = BitmapFactory.decodeStream(istr);

                return bitmap;
            }

        });

        gridview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (null != prevSelection)
                    ((ImageView) prevSelection).setColorFilter(null);
                ((ImageView) v).setColorFilter(0xffff9933, PorterDuff.Mode.MULTIPLY);
                prevSelection = v;
                Matcher matcher = p.matcher(filenames[position]);
                if (matcher.find()) {
                    String sub = matcher.group();
                    String[] parts = sub.split("_");
                    pam_photo_id = String.valueOf((Integer.parseInt(parts[0]) - 1) * 3
                            + (Integer.parseInt(parts[1])));
                }

            }
        });
    }

}
