package org.openmhealth.utils.reminders;

import io.smalldatalab.android.pam.R;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ReminderSetupActivity extends PreferenceActivity implements OnPreferenceClickListener,
	OnPreferenceChangeListener, OnClickListener{

	private ReminderManager mReminderManager;
	private Reminder mReminder;
	private TimePickerPreference timePref;
	private CheckBoxPreference enabledPref;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		this.addPreferencesFromResource(R.xml.reminder_setup_preferences);
		this.setContentView(R.layout.activity_reminder_setup);
		
		timePref = (TimePickerPreference) this.getPreferenceScreen()
                .findPreference("reminder_time");
        enabledPref = (CheckBoxPreference) this.findPreference("enable_reminder");
        
        PreferenceScreen screen = getPreferenceScreen();
        int prefCount = screen.getPreferenceCount();
        for (int i = 0; i < prefCount; i++) {
            screen.getPreference(i).setOnPreferenceClickListener(this);
            screen.getPreference(i).setOnPreferenceChangeListener(this);
        }

        ((Button) findViewById(R.id.cancel)).setOnClickListener(this);
        ((Button) findViewById(R.id.save)).setOnClickListener(this);
        ((Button) findViewById(R.id.delete)).setOnClickListener(this);
        
        // Setup for current reminder, if this is an edit
        mReminderManager = new ReminderManager(this);
        mReminder = new Reminder();
        if(this.getIntent().getExtras() != null){
	        Integer passedId = this.getIntent().getExtras().getInt(Reminder.EXTRA_REMINDER_ID, 0);
	        if (passedId != 0){
	        	mReminder = mReminderManager.getReminder(passedId);
	        	timePref.setTime(mReminder.hour, mReminder.minute);
	        } 
	    }
        enabledPref.setChecked(mReminder.enabled);

	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		return true;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		return true;
	}

	@Override
	public void onClick(View v) {
		 int id = v.getId();
	     if (id == R.id.save) {
	    	 // Set the values for the reminder and save it
	    	 SimpleTime time = timePref.getTime();
	    	 mReminder.hour = time.getHour();
	    	 mReminder.minute = time.getMinute();
	    	 mReminder.enabled = enabledPref.isChecked();
	    	 mReminder.notifTitle = "Reminder";
	    	 mReminder.notifText = "Please take a PAM assessment now.";
	    	 mReminderManager.upsertReminder(mReminder);
	    	 finish();
	     } else if (id == R.id.cancel) {
	    	 finish();
	     } else if (id == R.id.delete) {
	    	 mReminderManager.removeReminder(mReminder);
	    	 finish();
	     }
	     
		
	}
}
