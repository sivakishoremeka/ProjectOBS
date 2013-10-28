package com.obs.payapp;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.obs.object.PaymentInfo;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;




public class PrintActivity extends Activity {
	

private SharedPreferences mPrefs;
private SharedPreferences.Editor mPrefsEditor; 
protected final static String PREFS_FILE = "mifosAppPrefs";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_print);
		
		mPrefs = getSharedPreferences(PREFS_FILE, 0);
        mPrefsEditor = mPrefs.edit(); 
        mPrefsEditor.putString("CNAME", "kishore");
        mPrefsEditor.commit();
		
	}
	
	public void btnPrint_Onclick(View v) {
		// TODO Auto-generated method stub
		try {
			
			Intent intent = new Intent(PrintActivity.this, BluetoothChatActivity.class); 
			startActivity(intent);
			} catch (Exception e) {
			e.printStackTrace();
		}

	}

	

}
