package com.antoinecampbell.gcmdemo;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.widget.Spinner;
import android.widget.Toast;

public class ServiceSetting extends Service {

	public static final String MyPREFERENCES = "MyPrefs" ;
	private static final int NOTIFY_ME_ID=1337;
	SharedPreferences sharedpreferences;
	int bat_per;
	String msg;
	int Avail_battery_perc;
	Editor ed;
	private Socket client;
	private PrintWriter printwriter;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "Service started", Toast.LENGTH_SHORT).show();
		sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
		ed = sharedpreferences.edit();
		bat_per = sharedpreferences.getInt("battery_percentage", -1);
		msg = sharedpreferences.getString("message", null);
		new check().execute();
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "Service destroyed", Toast.LENGTH_SHORT).show();
		super.onDestroy();
	}
	
	public int getBatteryPercentage() {
		   
		
		  BroadcastReceiver batteryLevel = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				context.unregisterReceiver(this);
			    int currentLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			    int level=-1;
			    if (currentLevel >= 0 && scale > 0) {
			     level = (currentLevel * 100) / scale;
			    }
			    Avail_battery_perc=level;
			}
		 };
		 return Avail_battery_perc;
	}
	
	//Is plugged in
	public static boolean isPhonePluggedIn(Context context) {
		  boolean charging = false;
		  String result = "No";
		  final Intent batteryIntent = context.registerReceiver(null,
		    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		  int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		  boolean batteryCharge = status == BatteryManager.BATTERY_STATUS_CHARGING;
		 
		  int chargePlug = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		  boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
		  boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
		  
		  if (batteryCharge)
		   charging = true;
		  if (usbCharge)
		   charging = true;
		  if (acCharge)
		   charging = true;
		 
		  return charging;
	}

	private class check extends AsyncTask<String, String, String> {

		int flag,i;
		String res;
		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			boolean sent = sharedpreferences.getBoolean("sent", false);
			flag = 1;
			/*if(isPhonePluggedIn(getApplicationContext())) {
				flag = 0;
				if(!sent){
					flag=1;
				}
				ed.putBoolean("sent", true);
				ed.commit();
				res = "Plugged in";
			}
			else*/ {
				if(getBatteryPercentage()<bat_per) {
					if(sent) {
						flag = 0;
						res = "Message already sent";
					}
					else	{
						flag = 1;
						res = "Sending message";
					}
				}
				else {
					flag = 0;
					ed.putBoolean("sent", false);
					ed.commit();
					res = "Battery Percentage okay";
				}
			}
			//flag=1;
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			
			super.onPostExecute(result);
		//	Log.e(msg,"");
		//	Toast.makeText(getApplicationContext(), res+"\nflag = "+flag, Toast.LENGTH_LONG).show();
//			Toast.makeText(getApplicationContext(), "flag = " + flag, Toast.LENGTH_LONG).show();
			if(flag == 1) {
				if (msg != "")
				{
					msg += "has been send by " + GcmActivity.regid;
				    sendMessage(msg);
				    Toast.makeText(getApplicationContext(), "msg sent", Toast.LENGTH_LONG).show();
				    Log.e("msg sent","");
				   // NotificationAlert();
				    turnoffThings();
				}
				ed.putBoolean("sent", true);
				ed.commit();
			}
			new check().execute();
		}
		
		private void sendMessage(String message)
	    {
		if(GcmActivity.regid == null || GcmActivity.regid.equals(""))
		{
		    Toast.makeText(getApplicationContext(), "You must register first", Toast.LENGTH_LONG).show();
		    return;
		}
		String messageType = "Broadcast";
		new AsyncTask<String, Void, String>()
		{
		    @Override
		    protected String doInBackground(String... params)
		    {
			String msg = "";
			try
			{
			    Bundle data = new Bundle();
			    data.putString("message", params[0]);
			    /*if(params[1].equals("Echo"))
			    {
				data.putString("action", "com.antoinecampbell.gcmdemo.ECHO");
			    }*/
			    //else if(params[1].equals("Broadcast"))
			    {
				data.putString("action", "com.antoinecampbell.gcmdemo.BROADCAST");
			    }/*
			    else if(params[1].equals("Notification"))
			    {
				data.putString("action", "com.antoinecampbell.gcmdemo.NOTIFICATION");
			    }*/
			    String id = Integer.toString(GcmActivity.msgId.incrementAndGet());
			    GcmActivity.gcm.send(Globals.GCM_SENDER_ID + "@gcm.googleapis.com", id, Globals.GCM_TIME_TO_LIVE, data);
			    msg = "Sent message";
			}
			catch (IOException ex)
			{
			    msg = "Error :" + ex.getMessage();
			}
			return msg;
		    }

		    @Override
		    protected void onPostExecute(String msg)
		    {
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
		    }
		}.execute(message, messageType);
	    }
		
	}
	
	private SharedPreferences getGcmPreferences(Context context)
    {
	// This sample app persists the registration ID in shared preferences,
	// but how you store the regID in your app is up to you.
	return getSharedPreferences(Globals.PREFS_NAME, Context.MODE_PRIVATE);
    }
	//turning off things
	void turnoffThings(){
		Power_Saver p = new Power_Saver();
		p.turnbluetoothoff();
	//	turnBrightnessoff();
	//	p.turnWifioff();
		NotificationAlert();
	}
	
	public void turnBrightnessoff(){
		int progress = 1;
		android.provider.Settings.System.putInt(getContentResolver(),
			      android.provider.Settings.System.SCREEN_BRIGHTNESS,
			      progress);
	}
	
	void NotificationAlert(){
		final NotificationManager mgr=
	            (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
	        Notification note=new Notification(R.drawable.cbchk_blue,
	                                                        "Android Example Status message!",
	                                                        System.currentTimeMillis());
	         
	        // This pending intent will open after notification click
	        PendingIntent i=PendingIntent.getActivity(this, 0,
	                                                new Intent(this, MainActivity.class),
	                                                0);
	         
	        note.setLatestEventInfo(this, "Battery Management",
	                                "Wifi, Bluetooth and Brightness has been turned down for saving power", i);
	         
	        //After uncomment this line you will see number of notification arrived
	        //note.number=2;
	        mgr.notify(NOTIFY_ME_ID, note);
	}
}
