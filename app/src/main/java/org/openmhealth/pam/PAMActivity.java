package org.openmhealth.pam;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import io.smalldatalab.android.pam.R;
import io.smalldatalab.omhclient.DSUClient;


public class PAMActivity extends FragmentActivity {
    private DSUClient mDSUClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            PamFragment details = new PamFragment();
            details.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, details)
                    .commit();
        }


    }

}
