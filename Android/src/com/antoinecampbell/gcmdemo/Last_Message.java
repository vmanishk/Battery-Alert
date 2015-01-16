package com.antoinecampbell.gcmdemo;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class Last_Message extends Activity {
	ImageButton plus_btn, minus_btn;
	Button show_btn,save,send_btn;
	TextView bat_per;
	EditText editText;
	String perc, msg;
	int per_num, Avail_battery_perc;
	SharedPreferences sharedpreferences;
	
	TextView message ;
	   public static final String MyPREFERENCES = "MyPrefs" ;
	   public static final String Name = "nameKey"; 
	  //
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.last_message);
		
		message = (TextView) findViewById(R.id.message);
	      
		sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
		
	      if (sharedpreferences.contains(Name))
	      {
	         message.setText(sharedpreferences.getString(Name, ""));

	      }
	    
		//buttons
		show_btn = (Button) findViewById(R.id.show_cont);
		plus_btn = (ImageButton)findViewById(R.id.plus_btn);
		minus_btn = (ImageButton)findViewById(R.id.minus_btn);
		bat_per = (TextView) findViewById(R.id.battery_percentage);
		save = (Button) findViewById(R.id.send);
		editText = (EditText) findViewById(R.id.message);
		send_btn = (Button) findViewById(R.id.send_btn);
		
		int get_bat_per = sharedpreferences.getInt("battery_percentage", -1);
		String get_message_def = sharedpreferences.getString("message", null);
		if(get_bat_per!=-1) {
			bat_per.setText(get_bat_per+"%");
		}
		if(get_message_def != null) {
			editText.setText(get_message_def);
		}
		
		
		//actions
		editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
		    @Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        boolean handled = false;
		        if (actionId == EditorInfo.IME_ACTION_SEND) {
		        	editText.setText(editText.getText().toString(), TextView.BufferType.EDITABLE);
		            handled = true;
		        }
		        return handled;
		    }
		});
		
		save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
/*				msg = editText.getText().toString(); // get the text message on the text field
				textField.setText(""); // Reset the text field to blank
				SendMessage sendMessageTask = new SendMessage();
				sendMessageTask.execute();*/
				perc = bat_per.getText().toString();
				perc = perc.substring(0, perc.length()-1);
				Editor ed = sharedpreferences.edit();
				ed.putInt("battery_percentage", Integer.parseInt(perc));
				ed.putString("message", editText.getText().toString());
				ed.commit();
				editText.setText("");
			}
		});
		
		show_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(getApplicationContext(),Display_database.class);
				startActivity(i);
				finish();
			}
		});
		
		plus_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				perc = bat_per.getText().toString();
				per_num = Integer.parseInt(perc.substring(0, perc.length()-1));
				if(per_num!=99) {
					per_num++;
					perc = per_num+"%";
					bat_per.setText(perc);
				}
			}
		});
		
		plus_btn.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {
				// TODO Auto-generated method stub
				return false;
			}
		});

		minus_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				perc = bat_per.getText().toString();
				per_num = Integer.parseInt(perc.substring(0, perc.length()-1));
				if(per_num!=1) {
					per_num--;
					perc = per_num+"%";
					bat_per.setText(perc);
				}
			}
		});

		minus_btn.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		
		send_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				sendSMSMessage();
			}
		});
		
	}
	
	protected void sendSMSMessage() {
	      Log.i("Send SMS", "");
	      List<Contact> l = new ArrayList<Contact>();
	      DatabaseHandler db = new DatabaseHandler(getApplicationContext());
	      l = db.getAllContacts();
	      String ph_no;
	      if(l.isEmpty()){
	    	  Toast.makeText(getApplicationContext(), "No Contacts Selected", Toast.LENGTH_LONG).show();
	      }
	      for(int i=0;i<l.size();i++){
	    	  String phoneNo = l.get(i)._phone_number;
	    	  String message = editText.getText().toString();

	    	  try {
	    		  SmsManager smsManager = SmsManager.getDefault();
	    		  smsManager.sendTextMessage(phoneNo, null, message, null, null);
	    		  Toast.makeText(getApplicationContext(), "SMS sent.",
	    				  Toast.LENGTH_LONG).show();
	    	  } catch (Exception e) {
	    		  Toast.makeText(getApplicationContext(),
	    				  "SMS faild, please try again.",
	    				  Toast.LENGTH_LONG).show();
	    		  e.printStackTrace();
	    	  }
	      }
	   }
	
	/*private class SendMessage extends AsyncTask<Void, Void, Void> {
		 
		@Override
		protected Void doInBackground(Void... params) {
			try {
 
				client = new Socket("172.16.6.189", 4444); // connect to the server
				printwriter = new PrintWriter(client.getOutputStream(), true);
				printwriter.write(msg); // write the message to output stream
 
				printwriter.flush();
				printwriter.close();
				client.close(); // closing the connection

			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
 
	}*/
	/*
	public void run(View view){
	      String n  = message.getText().toString();
	      Editor editor = sharedpreferences.edit();
	      editor.putString(Name, n);

	      editor.commit(); 

	   }
	
	@Override
	   public boolean onCreateOptionsMenu(Menu menu) {
	      // Inflate the menu; this adds items to the action bar if it is present.
	      getMenuInflater().inflate(R.menu.main, menu);
	      return true;
	   }
	*/
	// function for database
	/*public void fetchContacts() {
		
			//deleteDatabase || update
	        String phoneNumber = null;
	        String email = null;
	 
	        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
	        String _ID = ContactsContract.Contacts._ID;
	        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
	        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
	     //   String IS_SELECT = ContactsContract.Contacts.IS_SELECT;
	 
	        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
	        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
	        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
	 
	        Uri EmailCONTENT_URI =  ContactsContract.CommonDataKinds.Email.CONTENT_URI;
	        String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
	        String DATA = ContactsContract.CommonDataKinds.Email.DATA;
	 
	        StringBuffer output = new StringBuffer();
	 
	        ContentResolver contentResolver = getContentResolver();
	 
	        Cursor cursor = contentResolver.query(CONTENT_URI, null,null, null, null); 
	 
	        // Loop for every contact in the phone
	        if (cursor.getCount() > 0) {
	 
	            while (cursor.moveToNext()) {
	 
	                String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
	                String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
	                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));
	 
	                if (hasPhoneNumber > 0) {
	 
	                    // Query and loop for every phone number of the contact
	                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);
	 
	                    while (phoneCursor.moveToNext()) {
	                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
	 
	                    }
	 
	                    phoneCursor.close();
	 
	   /*                 // Query and loop for every email of the contact
	                    Cursor emailCursor = contentResolver.query(EmailCONTENT_URI,    null, EmailCONTACT_ID+ " = ?", new String[] { contact_id }, null);
	 
	                    while (emailCursor.moveToNext()) {
	 
	                        email = emailCursor.getString(emailCursor.getColumnIndex(DATA));
	 
	                        output.append("\nEmail:" + email);
	 
                   }

	                    emailCursor.close();
	                }*/
	   /*             db.addContact(new Contact(name, phoneNumber));
	                Log.e(name, phoneNumber);
	            }
	        }
	    }
	
	}*/
	/*
	//Battery Percentage check
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
	}*/
	
}
