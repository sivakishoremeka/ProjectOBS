package com.obs.payapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.obs.utils.MySSLSocketFactory;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ClientSearchActivity extends Activity {

	private Button mSubmit;

	private ProgressDialog mProgressDialog;
	
	private final static String NETWORK_ERROR = "NETWORK_ERROR";

	Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_client_search);
		mSubmit = (Button) findViewById(R.id.submit);
		mSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				EditText ClientId_et = (EditText) findViewById(R.id.client_id);
				String ClientId = ClientId_et.getText().toString();
				if(ClientId.length()!=0)
					clientSearch(ClientId);
					else
						Toast.makeText(ClientSearchActivity.this, "invalid client data", Toast.LENGTH_LONG).show();
			}
		});

	}

	public void clientSearch(String Id) {
		// TODO Auto-generated method stub
		try {
			new ClientSearchTask().execute(Id);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private class ClientSearchTask extends AsyncTask<String, Void, String> {

		private String ClientId;

		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(ClientSearchActivity.this);
			mProgressDialog.setMessage("Searching...");
			mProgressDialog.show();

		}

		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			ClientId= params[0];
			if (isNetworkAvailable()) {
				return callAuthenticationApi(ClientId);
			} else {
				return null;
			}
		//	try {
		//		Thread.sleep(3000);
		// } catch (InterruptedException e) {
		//		// TODO Auto-generated catch block
		//		e.printStackTrace();
		//	}
		//	return "[]";
		//	return "[  {    \"entityId\": 380,    \"entityAccountNo\": \"000000380\",    \"entityName\": \"venkat\",    \"entityType\": \"CLIENT\",    \"parentId\": 1,    \"parentName\": \"Head Office\"  },  {    \"entityId\": 125,    \"entityAccountNo\": \"000000125\",    \"entityName\": \"Venkat Ram J\",    \"entityType\": \"CLIENT\",    \"parentId\": 1,    \"parentName\": \"Head Office\"  }]";
		}

		public String callAuthenticationApi(String id) {
			// TODO Auto-generated method stub

			StringBuilder builder = new StringBuilder();
			HttpClient client = MySSLSocketFactory.getNewHttpClient();// new
																		// DefaultHttpClient();

			String authenticateRootUrl = "https://spark.openbillingsystem.com/mifosng-provider/api/v1/search?query=";
			//String authenticateRootUrl = context.getString(R.string.login_url);

			HttpGet httpGet = new HttpGet(authenticateRootUrl + id);

			 httpGet.setHeader("X-Mifos-Platform-TenantId", "pgcable");
			 httpGet.setHeader("Authorization", "Basic "
					+ "YWRtaW46YWRtaW5AMTIz");
			 httpGet.setHeader("Content-Type", "application/json");
			Log.i("callAuthenticateApi", "Calling " +  httpGet.getURI());

			try {
				HttpResponse response = client.execute(httpGet);
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();

					BufferedReader reader = new BufferedReader(
							new InputStreamReader(content));
					String line;
					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}
				} else {
					Log.e("callAuthenticateAPI", "Failed to Search");
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return builder.toString();
		}
		
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}

			
			JSONArray jsonArray= null;
			try {
				jsonArray = new JSONArray(result);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int length =jsonArray.length();
			
		    if((!(result==null))&& (length!=0))
			// Start the ClientListActivity
		    {  
				Intent intent = new Intent(ClientSearchActivity.this, ClientViewActivity.class); 
				Bundle bundle = new Bundle();
				Log.d("ClientSearchActivity-OnPostExecute", result);
				bundle.putString("Result", result);
				intent.putExtras(bundle);
				startActivity(intent); 
			}
		    else{
		    	Toast.makeText(ClientSearchActivity.this, "invalid client data", Toast.LENGTH_LONG).show();
		    }
		}
	}

	public boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo wifiNetwork = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetwork != null && wifiNetwork.isConnected()) {
			return true;
		}

		NetworkInfo mobileNetwork = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	    if (mobileNetwork != null && mobileNetwork.isConnected()) {
			return true;
		}
    	NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
	    if (activeNetwork != null && activeNetwork.isConnected()) {
			return true;
		}
		return false;
	}
}
