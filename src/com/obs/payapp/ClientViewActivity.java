package com.obs.payapp;

import android.app.Activity;
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

import com.obs.object.ListClientObject;
import com.obs.utils.CustomArrayAdapter;
import com.obs.utils.MySSLSocketFactory;


import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


public class ClientViewActivity extends ListActivity {

//	private Button mSubmit;

	private ProgressDialog mProgressDialog;
	
	Context context;
	
	private final static String NETWORK_ERROR = "NETWORK_ERROR";
    private ListClientObject[] arrClientDetails = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_client_view);
		//mSubmit = (Button) findViewById(R.id.submit);
//		static final String[] MOBILE_OS = 
//	            new String[] { "Android", "iOS", "WindowsMobile", "Blackberry"};

		// mPrefs = getSharedPreferences(PREFS_FILE, 0);
		Intent intent = getIntent();
	    Bundle extras = intent.getExtras();
		String json = extras.getString("Result");
		//GridViewData gvDataObj = new GridViewData();
		try {
			JSONArray jsonArray = new JSONArray(json);
			TextView[] arrText= new TextView[jsonArray.length()];
			arrClientDetails= new ListClientObject[jsonArray.length()];
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject clientDtls = (JSONObject) jsonArray.get(i);
				if (clientDtls.has("entityAccountNo")) {
					ListClientObject obj = new ListClientObject();
					obj.setAccountno(clientDtls.getString("entityAccountNo"));  
					obj.setDisplayName(clientDtls.getString("entityName"));
					arrClientDetails[i]= obj;
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setListAdapter(new CustomArrayAdapter(this, arrClientDetails));
	}
  @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		getClientDetails(arrClientDetails[position].getAccountno());
	}
	public void getClientDetails(String Id) {
		// TODO Auto-generated method stub
		try {
			new GetClientDetailsTask().execute(Id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class GetClientDetailsTask extends AsyncTask<String, Void, String> {

		private String ClientId;

		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(ClientViewActivity.this);
			mProgressDialog.setMessage("Retriving...");
			mProgressDialog.show();

		}

		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			ClientId= params[0];
		//	ResponseObj resObj = new ResponseObj();

			if (isNetworkAvailable()) {
				return callAuthenticationApi(ClientId);
			} else {
				//resObj.setFailResponse(100, NETWORK_ERROR);
				return null;
			}
	
			//try {
			//	Thread.sleep(3000);
			//} catch (InterruptedException e) {
				// TODO Auto-generated catch block
			//	e.printStackTrace();
			//}
			//return "{  \"id\": 125,  \"accountNo\": \"000000125\",  \"status\": {    \"id\": 300,    \"code\": \"clientStatusType.active\",    \"value\": \"Active\"  },  \"active\": true,  \"activationDate\": [    2013,    7,    22  ],  \"firstname\": \"Venkat\",  \"middlename\": \"Ram\",  \"lastname\": \"J\",  \"displayName\": \"Venkat Ram J\",  \"officeId\": 1,  \"officeName\": \"Head Office\",  \"phone\": \"9676387166\",  \"addressNo\": \"Flat 106, New Area\",  \"street\": \"SR Nagar\",  \"city\": \"Hyderabad\",  \"state\": \"Andhra Pradesh\",  \"country\": \"India\",  \"zip\": \"500032\",  \"balanceAmount\": 9799.9143,  \"groups\": [],  \"categoryType\": 20}";
			}

		public String callAuthenticationApi(String ClientId) {
			// TODO Auto-generated method stub

			StringBuilder builder = new StringBuilder();
			HttpClient client = MySSLSocketFactory.getNewHttpClient();// new
																		// DefaultHttpClient();

			String authenticateRootUrl = "https://spark.openbillingsystem.com/mifosng-provider/api/v1/clients/";
			//String authenticateRootUrl =  context.getString(R.string.clientview_url);

			HttpGet httpGet = new HttpGet(authenticateRootUrl + ClientId);

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
			Intent intent = new Intent(ClientViewActivity.this, PaymentActivity.class); 
			Bundle bundle = new Bundle();
			Log.d("ClientViewActivity-OnPostExecute", result);
			bundle.putString("Result", result);
			intent.putExtras(bundle);
			startActivity(intent); 
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
}

