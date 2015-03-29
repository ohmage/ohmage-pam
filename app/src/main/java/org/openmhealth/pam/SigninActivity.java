package org.openmhealth.pam;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import io.smalldatalab.android.pam.R;
import io.smalldatalab.omhclient.DSUClient;

public class SigninActivity extends Activity {

    //private TextView dsuUrl;
    private DSUClient mDSUClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_signin);
        mDSUClient =
                new DSUClient(
                        this.getString(R.string.dsu_client_url),
                        this.getString(R.string.dsu_client_id),
                        this.getString(R.string.dsu_client_secret),
                        this);
        this.findViewById(R.id.sign_in_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                login();

            }
        });
        /* TODO: Hide the DSU url for now until we have the changing URL function working */
        // Show current DSU url
        //dsuUrl = (TextView) this.findViewById(R.id.dsu_url);
        //dsuUrl.setText(this.getString(R.string.dsu_root_url));
    }

    private void login() {
        new Thread() {
            @Override
            public void run() {
                try {
                    if (mDSUClient.blockingGoogleSignIn(SigninActivity.this) != null) {
                        showToast("Sign In Succeeded");
                        setResult(RESULT_OK);
                        SigninActivity.this.finish();
                        return;
                    }
                } catch (IOException e) {
                    showToast("Sign In Failed. Please check your Internet connection");
                    Log.e(SigninActivity.class.getSimpleName(), "Network Error", e);


                } catch (Exception e) {
                    showToast("Sign In Failed. Unknown Error.");
                    Log.e(SigninActivity.class.getSimpleName(), "Sign In Failed", e);

                }

            }
        }.start();

    }

    private void showToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SigninActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
