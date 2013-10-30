package com.obs.payapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import com.obs.object.PaymentInfo;
import com.obs.object.PrintInfo;
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
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class PaymentActivity extends Activity {

	public String mobileno;
	TextView AccountNo;
	TextView DisplayName;
	TextView HardwareDetails;
	TextView OfficeName;
	TextView Address;
	TextView Phone;
	TextView Street;
	TextView City;
	TextView State;
	TextView Country;
	TextView zip;
	TextView BalanceAmmount;
	EditText PayAmount;
	Button Pay;
	Button Print;
	int ClientId;
	private ProgressDialog mProgressDialog;
	RadioButton radioBtn;
	Context context;
	PaymentInfo payinfo;
	PrintInfo printinfo;
	private SharedPreferences mPrefs;
	private SharedPreferences.Editor mPrefsEditor; 
	protected final static String PREFS_FILE = "mifosAppPrefs";
	public final static String RESOURCE_ID = "resourceId";
	public final static String CLIENT_ID = "clientId";
	public final static String CLIENT_NAME="clientName";
	public final static String HARDWARE_DETAILS="hardwaredetails";
	public final static String PAYMENT_CODE = "paymentCode";
	public final static String AMOUNT_PAID="amountPaid";
	public final static String REMARKS = "remarks";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		Intent i = getIntent();
		Bundle extras = i.getExtras();
		String json="";
		if (extras != null) {
			json = extras.getString("Result");
		}
		setContentView(R.layout.activity_payment);

		AccountNo = (TextView)findViewById(R.id.accountno);
		DisplayName= (TextView)findViewById(R.id.display_name);
		HardwareDetails= (TextView)findViewById(R.id.hw_details);
		OfficeName= (TextView)findViewById(R.id.office);
		Address= (TextView)findViewById(R.id.addressno);
		Phone= (TextView)findViewById(R.id.phone);
		Street= (TextView)findViewById(R.id.street);
		City= (TextView)findViewById(R.id.city);
		State= (TextView)findViewById(R.id.state);
		Country= (TextView)findViewById(R.id.country);
		zip= (TextView)findViewById(R.id.zip);
		BalanceAmmount= (TextView)findViewById(R.id.balance_amount);
		PayAmount= (EditText)findViewById(R.id.payment_amount);
		Pay= (Button)findViewById(R.id.Pay);
		Print= (Button)findViewById(R.id.Print);
		radioBtn = (RadioButton)findViewById(R.id.cash);

		JSONObject clientDtls;
		try {
			
			clientDtls = new JSONObject(json);
			mobileno=clientDtls.getString("phone");
			ClientId= Integer.parseInt(clientDtls.getString("id"));
			AccountNo.setText(clientDtls.getString("accountNo"));
			DisplayName.setText(clientDtls.getString("displayName"));
			HardwareDetails.setText((clientDtls.getJSONArray("hardwareDetails")).getString(0));
			OfficeName.setText(clientDtls.getString("officeName"));
			Address.setText(clientDtls.getString("addressNo"));
			Phone.setText(clientDtls.getString("phone"));
			Street.setText(clientDtls.getString("street"));
			City.setText(clientDtls.getString("street"));
			State.setText(clientDtls.getString("state"));
			Country.setText(clientDtls.getString("country"));
			zip.setText(clientDtls.getString("zip"));
			BalanceAmmount.setText(clientDtls.getString("balanceAmount"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void payBtn_onClick(View v) {
		// TODO Auto-generated method stub
		try {
			payinfo = new PaymentInfo();
			payinfo.setClientId(ClientId+"");
			payinfo.setClientName(DisplayName.getText().toString());
			payinfo.setHardwareDetails(HardwareDetails.getText().toString());
			payinfo.setDateFormat("dd MMMM yyyy");
			payinfo.setLocale("en");
			Date date= new Date();
			SimpleDateFormat  formater = new SimpleDateFormat("dd MMMM yyyy");
			payinfo.setPaymentDate(formater.format(date));
			if(radioBtn.isChecked()){
				payinfo.setPaymentCode("CA");
				
			}
			else
				payinfo.setPaymentCode("CQ");
			payinfo.setAmountPaid(PayAmount.getText().toString());
			payinfo.setRemarks("remarks");


			new AuthenticateTask().execute(payinfo);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	public void onClick_printBtn(View v) {
		// TODO Auto-generated method stub
		try {
			/*Intent intent = new Intent(PaymentActivity.this, BluetoothChatActivity.class); 
			startActivity(intent);*/
			finish();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	/*protected Void callPayment(PaymentInfo paymentInfo, String id) {

		// TODO Auto-generated method stub
		try {
			new AuthenticateTask().execute(paymentInfo,id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;


	}*/
	private class AuthenticateTask extends AsyncTask<PaymentInfo, Void, String> {
		PaymentInfo paymentInfo;

		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(PaymentActivity.this);
			mProgressDialog.setMessage("Searching...");
			mProgressDialog.show();

		}


		@Override
		protected String doInBackground(PaymentInfo... params) {
			paymentInfo = (PaymentInfo) params[0];


			if (isNetworkAvailable()) {
				return callPayment(paymentInfo);
			} else {
				//resObj.setFailResponse(100, NETWORK_ERROR);
				return null;
			}


		}

		protected void onPostExecute(String result) {

			try{
				super.onPostExecute(result);

				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}

				if (isNetworkAvailable()) {

					sentSMS(result);

				} else {
					//resObj.setFailResponse(100, NETWORK_ERROR);
				}

				if(!(result==null))
					// Start the ClientListActivity
					mPrefs = getSharedPreferences(PREFS_FILE, 0);
			        mPrefsEditor = mPrefs.edit(); 
			        mPrefsEditor.putString(RESOURCE_ID,result+"");
			        mPrefsEditor.putString(CLIENT_ID,ClientId+"");
			        mPrefsEditor.putString(CLIENT_NAME,payinfo.getClientName());
			        mPrefsEditor.putString(HARDWARE_DETAILS,payinfo.getHardwareDetails());
			        mPrefsEditor.putString(PAYMENT_CODE,payinfo.getPaymentCode());
			        mPrefsEditor.putString(AMOUNT_PAID,payinfo.getAmountPaid());
			        mPrefsEditor.putString(REMARKS,payinfo.getRemarks());
			        mPrefsEditor.commit();
					Toast.makeText(PaymentActivity.this, "Payment done successfully and resource id :"+result, Toast.LENGTH_LONG).show();
					Intent intent = new Intent(PaymentActivity.this, BluetoothChatActivity.class); 
					startActivity(intent);
				    //startActivity(new Intent(PaymentActivity.this,ClientSearchActivity.class));
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}

		public String callPayment(PaymentInfo paymentInfo) {

			StringBuilder builder = new StringBuilder();
			HttpClient client = MySSLSocketFactory.getNewHttpClient();// new
			// DefaultHttpClient();

			String authenticateRootUrl = "https://spark.openbillingsystem.com/mifosng-provider/api/v1/payments/";
			//String authenticateRootUrl = context.getString(R.string.clientpayment_url);

			HttpPost httpPost = new HttpPost(authenticateRootUrl + paymentInfo.getClientId());

			httpPost.setHeader("X-Mifos-Platform-TenantId", "pgcable");
			httpPost.setHeader("Authorization", "Basic "
					+ "YWRtaW46YWRtaW5AMTIz");
			httpPost.setHeader("Content-Type", "application/json");
			Log.i("callAuthenticateApi", "Calling " +  httpPost.getURI());

			JSONObject clientJson = writeClientJson(paymentInfo);

			try {
				StringEntity jsonEntity = new StringEntity(clientJson.toString());
				Log.i("callAddClientApi", "jsonEntity is " + clientJson.toString());
				httpPost.setEntity(jsonEntity); 
				Log.i("callAddClientApi", "httpPost is " + httpPost.toString()); 

				HttpResponse response = client.execute(httpPost);
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
					Log.e("callAuthenticateAPI", "Failed to download file");
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return readJsonUser(builder.toString());
			//return null;
			//return null;
		}




		public JSONObject writeClientJson(PaymentInfo paymentInfo) {
			JSONObject clientJson = new JSONObject();
			try {
				clientJson.put("clientId", paymentInfo.getClientId());
				clientJson.put("dateFormat", "dd MMMM yyyy");
				clientJson.put("paymentDate", paymentInfo.getPaymentDate());
				clientJson.put("paymentCode", "23");//paymentInfo.getPaymentCode());
				clientJson.put("amountPaid", paymentInfo.getAmountPaid()); 
				clientJson.put("locale", "en");
				clientJson.put("remarks", "hai");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return clientJson; 
		}



		public void sentSMS(String resourceId) throws IOException {
			String postData="";
			String retval = "";

			String User ="avshyd";
			String passwd = "21345348";
			String mobilenumber = "91"+mobileno;
			String message = "Payment done successfully";
			String sid = "Sender_Id";
			String mtype = "N";
			String DR = "Y";		



			postData += "User=" + URLEncoder.encode(User,"UTF-8") + "&passwd=" + passwd + "&mobilenumber=" + mobilenumber + "&message=" + URLEncoder.encode(message,"UTF-8") + "&sid=" + sid + "&mtype=" + mtype + "&DR=" + DR;
			URL url = new URL("http://smscountry.com/SMSCwebservice_Bulk.aspx");
			HttpURLConnection urlconnection = (HttpURLConnection) url.openConnection();

			// If You Are Behind The Proxy Server Set IP And PORT else Comment Below 4 Lines
			//Properties sysProps = System.getProperties();
			//sysProps.put("proxySet", "true");
			//sysProps.put("proxyHost", "Proxy Ip");
			//sysProps.put("proxyPort", "PORT");

			urlconnection.setRequestMethod("POST");
			urlconnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			urlconnection.setDoOutput(true);
			OutputStreamWriter out = new OutputStreamWriter(urlconnection.getOutputStream());
			out.write(postData);
			out.close();
			BufferedReader in = new BufferedReader(	new InputStreamReader(urlconnection.getInputStream()));
			String decodedString;
			while ((decodedString = in.readLine()) != null) {
				retval += decodedString;
			}
			in.close();

			System.out.println(retval);
		}
		//////////////////////

		public String readJsonUser(String jsonText) {
			Log.i("readJsonUser", "result is \r\n" + jsonText);
			try {
				/**
				 * For now we're not going to deal with the whole User object - just
				 * stick with Authentication Key and ignore the rest for now.
				 */
				JSONObject userObject = new JSONObject(jsonText);
				return userObject.getString("resourceId");

			} catch (JSONException e) {
				e.printStackTrace();
				return null;
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
}







