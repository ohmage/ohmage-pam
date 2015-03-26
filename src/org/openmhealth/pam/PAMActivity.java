
package org.openmhealth.pam;

import edu.cornell.tech.smalldata.omhclientlib.dsu.DSUAuth;
import edu.cornell.tech.smalldata.omhclientlib.dsu.DSUClient;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class PAMActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if signed in
        if (DSUClient.isSignedIn(this, DSUAuth.ACCOUNT_TYPE)){
        	if (savedInstanceState == null) {
        		PamFragment details = PamFragment
        				.getInstance(!"org.openmhealth.pam.ACTION_CHOOSE_IMAGE".equals(getIntent()
        						.getAction()));
        		details.setArguments(getIntent().getExtras());
        		getSupportFragmentManager().beginTransaction().add(android.R.id.content, details)
        		.commit();
        	}
        } else {
        	Intent mIntent = new Intent(this, SigninActivity.class);
        	this.startActivity(mIntent);
        	finish();
        }
    }

}
