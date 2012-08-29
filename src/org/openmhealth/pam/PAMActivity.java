
package org.openmhealth.pam;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class PAMActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            PamFragment details = PamFragment
                    .getInstance(!"org.openmhealth.pam.ACTION_CHOOSE_IMAGE".equals(getIntent()
                            .getAction()));
            details.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, details)
                    .commit();
        }
    }

}
