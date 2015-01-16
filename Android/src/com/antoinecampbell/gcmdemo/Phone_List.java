package com.antoinecampbell.gcmdemo;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public class Phone_List extends Activity {
	
	private ArrayList<String> results = new ArrayList<String>();
	private SQLiteDatabase newDB;
	DatabaseHandler db;
	ListView listViewPhoneBook;
	static List<Contact> l;
	List<Contact> list;
	Button done_btn;
	ListView lv;
	Contact contact;
	CheckBox ch;
	ContentResolver contentResolver;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.phone_list);
		db = new DatabaseHandler(getApplicationContext());
		//setContentView(R.layout.list_row);
		
	//	ActionBar actionbar= getSupportActionBar();
	//	actionbar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00C4CD")));
		
		//l = db.getAllContacts();
		//listViewPhoneBook=(ListView)findViewById(R.id.listPhoneBook);
		done_btn = (Button) findViewById(R.id.done);
		
	//	displayList();
		
		contentResolver = getContentResolver();
		
		Log.e("",contentResolver.toString());
		l = new ArrayList<Contact>();
		selectContacts();
		lv = (ListView) findViewById(R.id.lvlist);
		list = db.getAllContacts();
		//l = db.getAllContacts();
		Log.e("\n\n\n\n","");
		Log.e(l.toString(),"");
		lv.setAdapter(new CustomAdapterForList(getApplicationContext(), R.layout.list_row, l));
	
		done_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				for (int i = 0; i < l.size(); i++) {
					v = lv.getAdapter().getView(i, null, null);
					ch = (CheckBox) v.findViewById(R.id.checkBox1);
					contact= new Contact();
					String x=l.get(i)._name;
					contact.setName(x);
					contact.setPhoneNumber(l.get(i)._phone_number);
					if(ch.isChecked())
					{
						if(!list.contains(contact))
							db.addContact(contact);
					}
					else
					{
						if(list.contains(contact)){
							db.deleteContact(contact);
						}
					}
				}
				Intent i = new Intent(getApplicationContext(),Display_database.class);
				startActivity(i);
				finish();
			}
		});
		
		
	}
	
	// view list here
	public void addContacts(){
		for (int i = 0; i < l.size(); i++) {
			View v = lv.getAdapter().getView(i, null, null);
			ch = (CheckBox) v.findViewById(R.id.checkBox1);
			contact= new Contact();
			String x=l.get(i)._name;
			contact.setName(x);
			contact.setPhoneNumber(l.get(i)._phone_number);
			if(ch.isChecked())
			{
				
				db.addContact(contact);
			}
			else
			{
				if(list.contains(contact)){
					db.deleteContact(contact);
				}
			}
		}
	}
	
	public List<Contact> selectContacts(){
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
       

        
        Cursor cursor = contentResolver.query(CONTENT_URI, null,null, null, null); 
        
        
        
        if (cursor.getCount() > 0){
        	/*String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
            String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
            int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));
            
            if (hasPhoneNumber > 0) {
           	 
                // Query and loop for every phone number of the contact
                Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);

                while (phoneCursor.moveToNext()) {
                    phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));

                }
                phoneCursor.close();
                Log.e(name, phoneNumber);
                l.add(new Contact(name, phoneNumber));
            }*/
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
	                    
	                   
	                    l.add(new Contact(name, phoneNumber));
	                Log.e(name, phoneNumber);
	            }
	        }
        }
        
       
        return l;
	}
	
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	// Handle action bar item clicks here. The action bar will
	// automatically handle clicks on the Home/Up button, so long
	// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_done) {
			for (int i = 0; i < l.size(); i++) {
				View v = lv.getAdapter().getView(i, null, null);
				ch = (CheckBox) v.findViewById(R.id.checkBox1);
				if(ch.isChecked())
				{
					contact= new Contact();
					String x=l.get(i)._name;
					contact.setName(x);
					db.updateContact(contact);
				}
				else
				{
					contact= new Contact();
					String x=l.get(i)._name;
					contact.setName(x);
				}
			}
			//startService(new Intent(MainActivity.this, ChatHeadService.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}*/
}