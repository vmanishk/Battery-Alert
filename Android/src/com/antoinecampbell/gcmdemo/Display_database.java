package com.antoinecampbell.gcmdemo;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class Display_database extends Activity {
	DatabaseHandler db;
	ListView lv;
	Contact contact;
	List<Contact> lis;
	List<String> list;
	Button update_btn;
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_data);
		lv = (ListView) findViewById(R.id.lvlist1);
		db = new DatabaseHandler(getApplicationContext());
			lis = db.getAllContacts();
			list = new ArrayList<String>();
		if(lis.isEmpty()){
			Toast.makeText(getApplicationContext(), "No contacts are Selected Please select", Toast.LENGTH_SHORT).show();
		}
		else{
			for(int i=0;i<lis.size();i++){
				list.add(lis.get(i).getName());
			}
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this, 
                android.R.layout.simple_list_item_1,
                list);

        	lv.setAdapter(arrayAdapter);
		}
        update_btn = (Button) findViewById(R.id.update_cont);
        update_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(getApplicationContext(),Phone_List.class);
				startActivity(i);
				finish();
			}
		});
	}
}
