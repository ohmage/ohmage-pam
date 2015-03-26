package org.openmhealth.utils.reminders;

import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

public class Reminder {
	Integer id;
	String notifTitle; // Title to show on notification
	String notifText;  // Text of notification
	Integer hour;  // Hour of the day to deliver reminder
	Integer minute; // Minute of the hour to deliver reminder
	boolean enabled = true; // Enabled to sent notification.  Defaults to true;
	
	public static final String EXTRA_REMINDER_ID = "reminder_id";
	private static final String KEY_ID = "id";
	private static final String KEY_HOUR = "hour";
	private static final String KEY_MIN = "minute";
	private static final String KEY_ENABLED = "enabled";
	private static final String KEY_TITLE = "title";
	private static final String KEY_TEXT = "text";
	
	public Reminder(){
		Random r = new Random();
		this.id = r.nextInt();
	}
	
	public JSONObject toJson(){
		
		JSONObject json = new JSONObject();
		try {
			json.put(KEY_ID, id);
			json.put(KEY_HOUR, hour);
			json.put(KEY_MIN, minute);
			json.put(KEY_ENABLED, enabled);
			json.put(KEY_TITLE, notifTitle);
			json.put(KEY_TEXT, notifText);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	}
	
	public void fromJson(JSONObject json){
		try {
			this.id = json.getInt(KEY_ID);
			this.hour = json.getInt(KEY_HOUR);
			this.minute = json.getInt(KEY_MIN);
			this.enabled = json.getBoolean(KEY_ENABLED);
			this.notifTitle = json.getString(KEY_TITLE);
			this.notifText = json.getString(KEY_TEXT);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
