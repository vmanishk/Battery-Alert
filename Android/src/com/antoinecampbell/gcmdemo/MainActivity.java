package com.antoinecampbell.gcmdemo;

import android.os.Bundle;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MainActivity extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.android_tab_layout);
		TabHost th = getTabHost();
		
		TabSpec last_msg_spec = th.newTabSpec("Last Message");
		last_msg_spec.setIndicator("Last Message");
		Intent last_msg = new Intent(getApplicationContext(), Last_Message.class);
		last_msg_spec.setContent(last_msg);
		
		TabSpec power_saver_spec = th.newTabSpec("Power Saver");
		power_saver_spec.setIndicator("Power Saver");
		Intent power_saver = new Intent(getApplicationContext(), Power_Saver.class);
		power_saver_spec.setContent(power_saver);
		
		TabSpec bat_info_spec = th.newTabSpec("Battery Info");
		bat_info_spec.setIndicator("Battery Info");
		Intent bat_info = new Intent(getApplicationContext(), Battery_Info.class);
		bat_info_spec.setContent(bat_info);
		
		th.addTab(last_msg_spec);
		th.addTab(power_saver_spec);
		th.addTab(bat_info_spec);
		
		Intent i = new Intent(getApplicationContext(), ServiceSetting.class);
		startService(i);
	}
	
	

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case R.id.action_settings:
			Intent i = new Intent(getApplicationContext(),SettingsActivity.class);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	

}
