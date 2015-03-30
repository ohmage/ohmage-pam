package org.openmhealth.utils.reminders;

import java.util.ArrayList;

import io.smalldatalab.android.pam.R;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class ReminderListActivity extends ListActivity {

	private Button addBtn;
	private ArrayList<Reminder> reminders = new ArrayList<Reminder>();
	private ReminderAdapter mReminderAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_reminder_list);
		
		// Fetch the existing reminders
		ReminderManager mReminderManager = new ReminderManager(this);
		reminders = mReminderManager.getAllReminders();

		// Setup all the view elements
		addBtn = (Button) this.findViewById(R.id.add_reminder);
		addBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				final Intent intent = new Intent(ReminderListActivity.this, ReminderSetupActivity.class);
				ReminderListActivity.this.startActivity(intent);
			}
			
		});
		mReminderAdapter = new ReminderAdapter(this, 0, reminders);
		this.getListView().setAdapter(mReminderAdapter);;
		this.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() 
    	{
    		public void onItemClick(AdapterView parent, View v, int position, long id) 
    		{
    			final Intent intent = new Intent(ReminderListActivity.this, ReminderSetupActivity.class);
    			intent.putExtra(Reminder.EXTRA_REMINDER_ID, reminders.get(position).id);
    			ReminderListActivity.this.startActivity(intent);
    		}
    	});
	}
	
	@Override
	public void onResume(){
		super.onResume();
		ReminderManager mReminderManager = new ReminderManager(this);
		reminders = mReminderManager.getAllReminders();
		mReminderAdapter.notifyDataSetChanged();
	}
	
	private class ReminderAdapter extends ArrayAdapter<Reminder>{
		private Context mContext;
	    private LayoutInflater inflater;

	    public ReminderAdapter(Context context, int resource,
				ArrayList<Reminder> objects) {
			super(context, resource, objects);
			mContext = context;
			inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

	    public int getCount() {
	        return reminders.size();
	    }

	    public View getView(int position, View convertView, ViewGroup parent) {
	    	try {
	            Reminder item = reminders.get(position);
	            View v = null;
	            if (convertView == null) {
	            	v = inflater.inflate(R.layout.item_reminder, null);

	            } else {
	                v = convertView;
	            }

	            TextView header = (TextView) v.findViewById(R.id.text1);
	            TextView description = (TextView) v.findViewById(R.id.text2);

	            SimpleTime sd = new SimpleTime(item.hour, item.minute);
	            header.setText(sd.toString(true));
	            if(item.enabled){
	            	description.setText("On");
	            } else {
	            	description.setText("Off");
	            }
	            return v;
	        } catch (Exception ex) {
	            return null;
	        }
	    }
		
		
	}
}
