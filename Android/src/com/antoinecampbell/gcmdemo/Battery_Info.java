package com.antoinecampbell.gcmdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.widget.Toast;

public class Battery_Info extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.battery_info);
		
		Intent powerUsageIntent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
		ResolveInfo resolveInfo = getPackageManager().resolveActivity(powerUsageIntent, 0);
		// check that the Battery app exists on this device
		if(resolveInfo != null){
		    startActivity(powerUsageIntent);
		}
	}
	
	/*@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//Toast.makeText(getApplicationContext(), "Ashish", Toast.LENGTH_LONG).show();
		Intent powerUsageIntent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
		ResolveInfo resolveInfo = getPackageManager().resolveActivity(powerUsageIntent, 0);
		// check that the Battery app exists on this device
		if(resolveInfo != null){
		    startActivity(powerUsageIntent);
		}
	}*/
}
