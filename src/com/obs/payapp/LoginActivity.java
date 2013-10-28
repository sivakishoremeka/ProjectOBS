package com.obs.payapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.obs.object.PaymentInfo;
import com.obs.object.PrintInfo;
import com.obs.object.ResponseObj;
import com.obs.object.User;
import com.obs.utils.MySSLSocketFactory;




public class LoginActivity extends Activity {

	private Button Login;
    private ProgressDialog mProgressDialog;
	
	private SharedPreferences mPrefs;
	private SharedPreferences.Editor mPrefsEditor; 
	public final static String USER_ID = "user";
	Context context;
	PrintInfo printinfo;
	
	
	protected final static String PREFS_FILE = "mifosAppPrefs";
	protected final static String USER_CREDENTIALS_HASH = "userCredentialsHash";
	protected final static String USER_AUTHENTICATION_KEY= "userAuthorisationKey";
	private final static String INCORRECT_DETAILS = "Incorrect username or password.";
	private final static String NEED_ONLINE_AUTH_FIRST = "You must authenticate username and password online before you can do it ffline.";
	private final static String NETWORK_ERROR = "Network error.";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		mPrefs = getSharedPreferences(PREFS_FILE, 0);
        mPrefsEditor = mPrefs.edit(); 
        
        Login = (Button) findViewById(R.id.btnLogin);
        
        Login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String username = ((EditText) findViewById(R.id.username)).getText().toString();
				String password = ((EditText) findViewById(R.id.password)).getText().toString();
				printinfo = new PrintInfo();
				printinfo.setName(username);
				if(username.length()!=0||password.length()!=0){
				login(username, password);
				}
				else
				{
					
					Toast.makeText(LoginActivity.this, "Not valid username or password", Toast.LENGTH_LONG).show();
				}
			}
        });
		
	}
	
    public void login(String username, String password) {
    	try {
    		new AuthenticateTask().execute(username, password); 
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    

	 private class AuthenticateTask extends AsyncTask<String, Void, ResponseObj> {

	    	private User authenticatedUser; 
	    	private String username;
	    	private String password;
	    	
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				
				mProgressDialog = new ProgressDialog(LoginActivity.this); 
				mProgressDialog.setMessage("Authenticating...");
				mProgressDialog.show();
			}

			@Override
			protected ResponseObj doInBackground(String... params) {
				username = params[0];
				password = params[1];
                ResponseObj resObj = new ResponseObj();				
				
				
				if(isNetworkAvailable()) {
					return callAuthenticationApi(username, password);
				} else {
					resObj.setFailResponse(100, NETWORK_ERROR);
					return resObj;
					// check hash of username and password against stored hash
					// 		first check if there is a stored hash
					/*if (mPrefs.contains(USER_CREDENTIALS_HASH)) {
						if (mPrefs.getString(USER_CREDENTIALS_HASH, null).equals(md5(username + password))) {
							// return user profile
							Log.i("doInBackground", "Returning stored authentication key after hash validation.");
							return mPrefs.getString(USER_AUTHENTICATION_KEY, null);
						} else {
							return INCORRECT_DETAILS;
						}
					} else {
						return NEED_ONLINE_AUTH_FIRST; 
					}*/
				}
				
			}
			
			@Override
			protected void onPostExecute(ResponseObj resObj) {
				super.onPostExecute(resObj);
				
				   
				//Log.d(TAG, "onPostExecute");
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				if(resObj.getStatusCode()==200){
	     				//need to save the details of result for further use
				     	//{
						//  "deviceId": 1,
						//  "clientId": 55,
						//  "clientType": "Normal",
						//  "clientTypeId": 20
						//}
					//Intent i = new Intent(AuthenticationAcitivity.this,HomeActivity.class);
					finish();
					mPrefs = getSharedPreferences(PREFS_FILE, 0);
		            mPrefsEditor = mPrefs.edit(); 
		            mPrefsEditor.putString("USER_ID",username);
		            mPrefsEditor.commit();
					Intent intent = new Intent(LoginActivity.this, ClientSearchActivity.class); 
					startActivity(intent);
				}
				else {
					
					/*AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this,AlertDialog.THEME_HOLO_DARK);
					// Add the buttons
					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
					           public void onClick(DialogInterface dialog, int id) {
					        	   //LoginActivity.this.finish();
					           }
					       });

					AlertDialog dialog =builder.create();
					dialog.setMessage(resObj.getsErrorMessage());
					dialog.show();*/
					Toast.makeText(LoginActivity.this, resObj.getsErrorMessage(), Toast.LENGTH_SHORT).show();
				}
			}

	 
	    public boolean isNetworkAvailable(){
	    	ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);    	

	    	NetworkInfo wifiNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    	if (wifiNetwork != null && wifiNetwork.isConnected()) {
	    		return true;
	    	}

	    	NetworkInfo mobileNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	    	if (mobileNetwork != null && mobileNetwork.isConnected()) {
	    		return true;
	    	}

	    	NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
	    	if (activeNetwork != null && activeNetwork.isConnected()) {
	    		return true;
	    	}
	    	return false;
	    }

	 

	    public ResponseObj callAuthenticationApi(String username, String password) {
	    	
	    	StringBuilder builder = new StringBuilder();
	    	HttpClient client =  MySSLSocketFactory.getNewHttpClient();//new DefaultHttpClient();
	    	//https://spark.openbillingsystem.com/?tenantIdentifier=pgcable
	    	String authenticateRootUrl = "https://spark.openbillingsystem.com/mifosng-provider/api/v1/authentication?username=";
	    	//String authenticateRootUrl =  context.getString(R.string.login_url);
	    	String passwordLabel = "&password=";
	    	
	    	HttpPost httpPost = new HttpPost(authenticateRootUrl + username + passwordLabel + password);
	    	httpPost.setHeader("X-Mifos-Platform-TenantId", "default");
			httpPost.setHeader("Authorization", "Basic " +"YWRtaW46YWRtaW5AMTIz" );//"YmlsbGluZzpiaWxsaW5nYWRtaW5AMTM="
			httpPost.setHeader("Content-Type", "application/json");
	    	Log.i("callAuthenticateApi", "Calling " + httpPost.getURI());
	    	ResponseObj resObj = new ResponseObj();
	    	try {
	    		HttpResponse response = client.execute(httpPost);
	    		StatusLine statusLine = response.getStatusLine();
	    		int statusCode = statusLine.getStatusCode();
	    		HttpEntity entity ;
	    		if (statusCode == 200) {
	    			entity = response.getEntity();
	    			InputStream content = entity.getContent();
	    			BufferedReader reader = new BufferedReader(new InputStreamReader(content));
	    			String line;
	    			while ((line = reader.readLine()) != null) {
	    				builder.append(line);
	    			}
	    			resObj.setSuccessResponse(statusCode, builder.toString());
	    		} else {
	    			entity = response.getEntity();
					String content = EntityUtils.toString(entity);
					String  sError = "No details found";
					if(content.length()!=0)
						try {
							sError = new JSONObject(content).getJSONArray("errors").getJSONObject(0).getString("developerMessage");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							resObj.setFailResponse(100,e.getMessage());
						}
					resObj.setFailResponse(statusCode,sError );
					Log.e("callExternalAPI", sError + statusCode);
	    		}
	    	} catch (ClientProtocolException e) {
	    		e.printStackTrace();
	    		resObj.setFailResponse(100,e.getMessage());
	    	} catch (IOException e) {
	    		resObj.setFailResponse(100,e.getMessage());
	    		e.printStackTrace();
	    	}
	    	return resObj; 
	    }
	    

	    
	   /* public String readJsonUser (String jsonText) {
	    	Log.i("readJsonUser", "result is \r\n" + jsonText);
	    	// Create new User object 
	    	User user = new User();
	    	// Populate user object
	    	try {
	    		*//** 
	    		 * For now we're not going to deal with the whole User object - just stick with Authentication Key and ignore the rest for now.
	    		 *//*
	    		JSONObject userObject = new JSONObject(jsonText);
	    		user.setUsername(userObject.getString("username"));
	    		user.setAuthenticated(userObject.getBoolean("authenticated"));
	    		user.setBase64AuthenticationKey(userObject.getString("base64EncodedAuthenticationKey"));
	    		user.setUserId(userObject.getString("userId"));
	    		// Loop through permissions
	    		JSONArray permissionsArray = userObject.getJSONArray("permissions");
	    		String[] userPermissions = new String[permissionsArray.length()];
	    		for (int i = 0; i < permissionsArray.length(); i++) {
	    			userPermissions[i] = (String) permissionsArray.get(i); 
	    		}
	    		user.setPermissions(userPermissions);

	    		authenticatedUser = user;
	    		
	    		return user.getBase64AuthenticationKey(); 
	    		
	    	} catch (JSONException e) {
	    		e.printStackTrace();
	    		return null; 
	    	}
	    }*/

	    
	    
	    
	   /* public String md5(String s) {
	    	// Method for getting an md5 hash of a string. 
	    	
	    	String retval = "";
	    	
	    	try {
	    		// Create MD5 Hash
	    		MessageDigest digest = MessageDigest.getInstance("MD5"); 
	    		digest.update(s.getBytes("UTF-8")); 
	    		byte[] messageDigest = digest.digest(); 
	    		
	    		// Create Hex String
	    		StringBuffer hexString = new StringBuffer();
	    		for (int i = 0; i < messageDigest.length; i++) {
	    			hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
	    		}
	    		retval =  hexString.toString();
	    	} catch (NoSuchAlgorithmException e) {
	    			e.printStackTrace();
	    	} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
			}
	    	
	    	return retval; 
	    }*/
}
	    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
