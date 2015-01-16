package com.antoinecampbell.gcmdemo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.audiofx.BassBoost.Settings;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.view.WindowManager;
import android.widget.SeekBar;


public class Power_Saver extends Activity {
	
	Last_Message last_msg_var;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.power_saver);
		
		startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
	}
	
	//Change brightness
	public void turnBrightnessoff(){
		int progress = 3;
		android.provider.Settings.System.putInt(getContentResolver(),
			      android.provider.Settings.System.SCREEN_BRIGHTNESS,
			      progress);
	}
	
	// turning off wifi
	public void turnWifioff(){
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifi.setWifiEnabled(false);
	}
	
	//turning off data connections
	/*public void turnDataoff(){
		private void setMobileDataEnabled(Context context, boolean enabled){
			final ConnectivityManager conman = (ConnectivityManager)  context.getSystemService(Context.CONNECTIVITY_SERVICE);
			final Class conmanClass = Class.forName(conman.getClass().getName());
			final Field connectivityManagerField = conmanClass.getDeclaredField("mService");
			connectivityManagerField.setAccessible(true);
			final Object connectivityManager = connectivityManagerField.get(conman);
			final Class connectivityManagerClass =  Class.forName(connectivityManager.getClass().getName());
			final Method setMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
			setMobileDataEnabledMethod.setAccessible(true);

			setMobileDataEnabledMethod.invoke(connectivityManager, enabled);
	
		}
	}*/

	//turn off bluetooth
	public void turnbluetoothoff(){
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter.isEnabled()){
			mBluetoothAdapter.disable();
		}
	}
/*
	//turn off GPS
	private void turnGPSOff(){
    	String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

    	if(provider.contains("gps")){ //if gps is enabled
        	final Intent poke = new Intent();
        	poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
        	poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
        	poke.setData(Uri.parse("3")); 
        	sendBroadcast(poke);
    		}
	}


	//lower brightness of whole phone
	
	/*first setting it to manual and not auto*/
	//Settings.System.putInt(getContentResolver(), SCREEN_BRIGHTNESS_MODE, SCREEN_BRIGHTNESS_MODE_MANUAL);
	
	/*changing the brightness value*/
	/*public void brightness(){
	WindowManager.LayoutParams localLayoutParams = getWindow()
            .getAttributes();
    	localLayoutParams.screenBrightness = 0.12F;
    	getWindow().setAttributes(localLayoutParams);

    	Settings.System.putInt(this.resolver, "screen_brightness", 30);

	} 
	//turn off vibrate
	
	/*Never vibrate on ring*/
	/*public void vibration(){
	setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
	/*Never vibrate on notify*/
	/*setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);

	}
	//Notify the user
	public void notification(){
	NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(“Our App Name”)
        .setContentText(“Wifi,Data Connection,Blutooth,GPS,Brightness and vibrate settings are changed!”);

	// Creates an explicit intent for an Activity in your app
	Intent resultIntent = new Intent(this, ResultActivity.class);

	// The stack builder object will contain an artificial back stack for the
	// started Activity.
	// This ensures that navigating backward from the Activity leads out of
	// your application to the Home screen.
	TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
	// Adds the back stack for the Intent (but not the Intent itself)
	stackBuilder.addParentStack(ResultActivity.class);
	// Adds the Intent that starts the Activity to the top of the stack
	stackBuilder.addNextIntent(resultIntent);
	PendingIntent resultPendingIntent =
	        stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
	mBuilder.setContentIntent(resultPendingIntent);
	NotificationManager mNotificationManager =
	    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	// mId allows you to update the notification later on.
	mNotificationManager.notify(mId, mBuilder.build());
	}*/
	
}