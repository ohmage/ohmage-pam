package org.openmhealth.utils.reminders;

import io.smalldatalab.android.pam.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.openmhealth.pam.PAMActivity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ReminderManager extends BroadcastReceiver {

	public static final String KEY_REMINDER_ACTION = "REMINDER_ACTION";
	public static final String KEY_ALARM_TYPE = "alarm_type";
	public static final String KEY_REMINDER_ID = "reminder_id";
	public static final String ALARM_TYPE_DELAY = "alarm_type_delay";
	public static final String ALARM_TYPE_REMINDER = "alarm_type_reminder";
	public static final String ALARM_TYPE_REMOVE_NOTIFICATION = "alarm_type_remove_notification";
	
	private static final String PREF_SAVED_REMINDERS = "preference_saved_reminders";
	
	private Context mContext;
	
	@SuppressLint("NewApi")
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			mContext = context;
			if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
				this.scheduleAllReminders();
			} else if (intent.getAction().equals(KEY_REMINDER_ACTION)) {
				// Deliver a notification
				Reminder reminder = this.getReminder(intent.getExtras().getInt(KEY_REMINDER_ID));
				
				Intent i = new Intent();
				i.setClass(mContext, PAMActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // this is required for calling an activity when outside of an activity
				PendingIntent contentIntent = PendingIntent.getActivity(mContext.getApplicationContext(), reminder.id, i, PendingIntent.FLAG_UPDATE_CURRENT);
				
				Notification noti = new Notification.Builder(mContext)
		         .setContentTitle(reminder.notifTitle)
		         .setContentText(reminder.notifText)
		         .setSmallIcon(R.drawable.ic_launcher)
		         .setDefaults(Notification.DEFAULT_ALL)
		         .setAutoCancel(true)
		         .setContentIntent(contentIntent)
		         .build();
				
				NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
				mNotificationManager.notify(reminder.id, noti);
			}
		} catch (Exception e){
			e.printStackTrace();
		}

	}
	
	public ReminderManager(){
		// This is only here for BroadcastReceiver
	}

	public ReminderManager(Context context){
		mContext = context;
	}
	
	public void scheduleReminder(Reminder reminder){
		// Setup the pending intent to come back here
		AlarmManager mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(mContext, ReminderManager.class);
		i.setAction(KEY_REMINDER_ACTION);
		i.putExtra(KEY_ALARM_TYPE, ALARM_TYPE_REMINDER);
		i.putExtra(KEY_REMINDER_ID, reminder.id);
		PendingIntent pi = PendingIntent.getBroadcast(mContext, reminder.id, i, PendingIntent.FLAG_UPDATE_CURRENT); // identified by reminder ID, so only one alarm per reminder

		Date deliveryTime = this.getNextOccurrence(reminder);
		mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, deliveryTime.getTime(), 24*60*60*1000, pi); // repeat daily
	}
	
	public void scheduleAllReminders(){
		ArrayList<Reminder> reminders = this.getAllReminders();
		for (Reminder it : reminders){
			if(it.enabled){
				this.scheduleReminder(it);
			}
		}
	}
	
	public void unscheduleReminder(Reminder reminder){
		AlarmManager mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(mContext, ReminderManager.class);
		i.setAction(KEY_REMINDER_ACTION);
		i.putExtra(KEY_ALARM_TYPE, ALARM_TYPE_REMINDER);
		i.putExtra(KEY_REMINDER_ID, reminder.id);
		PendingIntent pi = PendingIntent.getBroadcast(mContext, reminder.id, i, PendingIntent.FLAG_UPDATE_CURRENT); // identified by reminder ID, so only one alarm per reminder

		mAlarmManager.cancel(pi);
	}
	
	public Date getNextOccurrence(Reminder reminder){
		Calendar setTo = Calendar.getInstance();
		setTo.set(Calendar.HOUR_OF_DAY, reminder.hour);
		setTo.set(Calendar.MINUTE, reminder.minute);
		setTo.set(Calendar.SECOND, 0);
		
		Calendar now = Calendar.getInstance();
		if (now.getTimeInMillis() > setTo.getTimeInMillis()){
			// previous time today, so set for tomorrow
			setTo.add(Calendar.DAY_OF_YEAR, 1);
		}
		return setTo.getTime();
	}
	
	public Reminder getReminder(Integer id){
		ArrayList<Reminder> reminders = this.getAllReminders();
		for(Reminder it : reminders){
			if (it.id.equals(id)){
				return it;
			}
		}
		return null;
	}
	
	public ArrayList<Reminder> getAllReminders(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		ArrayList<Reminder> reminders = new ArrayList<Reminder>();
		try {
			JSONArray jsons = new JSONArray(prefs.getString(PREF_SAVED_REMINDERS, "[]"));
			for(int i = 0; i < jsons.length(); i++){
				Reminder r = new Reminder();
				r.fromJson(jsons.getJSONObject(i));
				reminders.add(r);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return reminders;
	}
	
	public void upsertReminder(Reminder reminder){
		ArrayList<Reminder> reminders = this.getAllReminders();
		// remove it if this one already exists
		for(Reminder it : reminders){
			if (it.id.equals(reminder.id)){
				reminders.remove(it);
				break;
			}
		}
		reminders.add(reminder);
		if(reminder.enabled){
			this.scheduleReminder(reminder);
		} else {
			this.unscheduleReminder(reminder);
		}
		this.saveAllReminders(reminders);
	}
	
	public void removeReminder(Reminder reminder){
		ArrayList<Reminder> reminders = this.getAllReminders();
		for(Reminder it : reminders){
			if (it.id.equals(reminder.id)){
				this.unscheduleReminder(it);
				reminders.remove(it);
				break;
			}
		}
		this.saveAllReminders(reminders);
	}
	
	private void saveAllReminders(ArrayList<Reminder> reminders){
		JSONArray jsons = new JSONArray();
		for(Reminder it : reminders){
			jsons.put(it.toJson());
		}
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		prefs.edit().putString(PREF_SAVED_REMINDERS, jsons.toString()).commit();
		
	}
	
	public void removeAllReminders(){
		ArrayList<Reminder> reminders = this.getAllReminders();
		for(Reminder it : reminders){
			this.unscheduleReminder(it);
		}
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		prefs.edit().putString(PREF_SAVED_REMINDERS, "[]").commit();
	}
	
}
