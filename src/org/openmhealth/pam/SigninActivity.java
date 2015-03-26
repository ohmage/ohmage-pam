
package org.openmhealth.pam;

import io.smalldatalab.android.pam.R;

import com.google.android.gms.common.ConnectionResult;

import edu.cornell.tech.smalldata.omhclientlib.dsu.DSUAuth;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class SigninActivity extends Activity {

	private TextView dsuUrl;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.activity_signin);
        this.findViewById(R.id.sign_in_button).setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v) {
        		AccountManager accountManager = (AccountManager) getSystemService(ACCOUNT_SERVICE);

        		try {
        			accountManager.addAccount(DSUAuth.ACCOUNT_TYPE, DSUAuth.ACCESS_TOKEN_TYPE, null, null, SigninActivity.this, new AccountManagerCallback<Bundle>() {
        				@Override
        				public void run(AccountManagerFuture<Bundle> future) {
        					try {
        						if(future.getResult().getString(AccountManager.KEY_ACCOUNT_NAME)!=null) {
        							// TODO handle successful account creation
        							final Intent intent = new Intent(SigninActivity.this, PAMActivity.class);
        							SigninActivity.this.startActivity(intent);
        							Toast.makeText(SigninActivity.this, "Signin successful.", Toast.LENGTH_LONG);
        							SigninActivity.this.finish();
        							
        						}else{
        							throw new Exception("No account created.");
        						}

        					} catch (Exception e) {
        						runOnUiThread(new Runnable() {
        							@Override
        							public void run() {
        								Toast.makeText(SigninActivity.this, "Login has failed, please try again.", Toast.LENGTH_SHORT).show();
        							}
        						});
        						e.printStackTrace();
        					}

        				}
        			}, new Handler());
        		} catch (Exception e){
        			e.printStackTrace();
        		}
        	}
        });
        
        // Show current DSU url
        dsuUrl = (TextView) this.findViewById(R.id.dsu_url);
        dsuUrl.setText(this.getString(R.string.dsu_root_url));
    }
    
}
