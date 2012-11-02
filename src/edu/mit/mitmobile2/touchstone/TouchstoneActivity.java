package edu.mit.mitmobile2.touchstone;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.mit.mitmobile2.FullScreenLoader;
import edu.mit.mitmobile2.MITClient;
import edu.mit.mitmobile2.MITClientData;
import edu.mit.mitmobile2.Module;
import edu.mit.mitmobile2.ModuleActivity;
import edu.mit.mitmobile2.R;

public class TouchstoneActivity extends ModuleActivity implements OnSharedPreferenceChangeListener {
	
	private Context mContext;	

	SharedPreferences pref;
	String user;
	String password;
	Document document;
	EditText touchstoneUsername;
	EditText touchstonePassword;
	//Button cancelButton;
	Button loginButton;
	CheckBox rememberLoginCB; 
	TextView mError;
    @SuppressWarnings("unused")
	private LinearLayout touchstoneContents;
	private FullScreenLoader touchstoneLoadingView;

    
	AlertDialog alert;
	public static SharedPreferences prefs;
	public static final String TAG = "TouchstoneActivity";
	
	Bundle extras;
	
	/**
	 * @throws IOException 
	 * @throws ClientProtocolException **************************************************/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG,"onCreate()");
		super.onCreate(savedInstanceState);
		mContext = this;
		
        createViews(); 
	}
		
	private void createViews() {
		Log.d(TAG,"createViews()");
		setContentView(R.layout.touchstone_login);

		extras = getIntent().getExtras();
		String key = "";
		key = (String)extras.getString(key);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		final SharedPreferences.Editor prefsEditor = prefs.edit();

		touchstoneUsername = (EditText)findViewById(R.id.touchstoneUsername);
		touchstonePassword = (EditText)findViewById(R.id.touchstonePassword);

		// load existing pref values
		touchstoneUsername.setText(prefs.getString("PREF_TOUCHSTONE_USERNAME", ""));
		touchstonePassword.setText(prefs.getString("PREF_TOUCHSTONE_PASSWORD", ""));

		//cancelButton = (Button)findViewById(R.id.touchstoneCancelButton);
		loginButton = (Button)findViewById(R.id.touchstoneLoginButton);
		rememberLoginCB =(CheckBox)findViewById(R.id.rememberLoginCB);

	    touchstoneLoadingView = (FullScreenLoader)findViewById(R.id.touchstoneLoadingView);
	    mError = (TextView)touchstoneLoadingView.findViewById(R.id.fullScreenLoadingErrorTV); 
	    touchstoneContents = (LinearLayout)findViewById(R.id.touchstoneContents);
	    
	    loginButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String requestKey = extras.getString("requestKey");
				MITClient.setUser(touchstoneUsername.getEditableText().toString());
				MITClient.setPassword(touchstonePassword.getEditableText().toString());
				MITClientData clientData = (MITClientData)MITClient.requestMap.get(requestKey);
				clientData.setTouchstoneState(MITClient.TOUCHSTONE_LOGIN);

				// Store remember password setting
				prefsEditor.putBoolean("PREF_TOUCHSTONE_REMEMBER_LOGIN", rememberLoginCB.isChecked());
				prefsEditor.commit();
				finish();
			}
		});
						
		// Dialog for invalid username or password
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Please enter a valid username and password.")
		       .setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           @Override
				public void onClick(DialogInterface dialog, int id) {
		                Log.d(TAG,"finish");
		           }
		       });
		alert = builder.create();
		// End Create Dialog
		
		// display the alert dialog if in the auth_error_state
		String touchstoneState = extras.getString("touchstoneState");
		Log.d(TAG,"touchstoneState = " + touchstoneState);
		if (touchstoneState != null && touchstoneState.equalsIgnoreCase(MITClient.AUTH_ERROR_STATE)) {
			//touchstoneUsername.setText("");
			//touchstonePassword.setText("");
			alert.show();
		}
		
	}
/*	
    private Handler loginUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	Log.d(TAG,"handleMessage");
			touchstoneContents.setVisibility(View.VISIBLE);
        	touchstoneLoadingView.setVisibility(View.GONE);

            if (msg.arg1 == MobileWebApi.SUCCESS) {
            	Log.d(TAG,"MobileWebApi success");
               
            	UserIdentity identity = (UserIdentity)msg.obj;
                Log.d(TAG,"identity = " + identity.getUsername());
            } 
            else if (msg.arg1 == MobileWebApi.ERROR) {
            	Log.d(TAG,"show login error");
            	mError.setText("Error logging into Touchstone");
            	touchstoneLoadingView.showError();
            } 
            else if (msg.arg1 == MobileWebApi.CANCELLED) {
            	touchstoneLoadingView.showError();
            }
        }
    };
*/
	@Override
	protected Module getModule() {
		return null;
	}

	@Override
	public boolean isModuleHomeActivity() {
		return true;
	}

	/*
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_INFO:
			//Intent intent = new Intent(mContext, FacilitiesInfoActivity.class);					
			//startActivity(intent);
			return true;
		case MENU_PREFS:
			//Intent intent = new Intent(mContext, FacilitiesInfoActivity.class);					
        	startActivity( new Intent(this, TouchstonePrefsActivity.class) );
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	*/
	
	@Override
	protected void prepareActivityOptionsMenu(Menu menu) { 
		/*
		menu.add(0, MENU_INFO, Menu.NONE, "Info")
		  .setIcon(R.drawable.menu_about);
		
		menu.add(1, MENU_PREFS, Menu.NONE, "Prefs")
		  .setIcon(R.drawable.main_repeat);
		 */
	}
    

	
	public static String responseContentToString(HttpResponse response) {
		try {
		InputStream inputStream = response.getEntity().getContent();
		ByteArrayOutputStream content = new ByteArrayOutputStream();
		// Read response into a buffered stream
		int readBytes = 0;
		byte[] sBuffer = new byte[512];
  		while ((readBytes = inputStream.read(sBuffer)) != -1) {
  			content.write(sBuffer, 0, readBytes);
  		}

  		// Return result from buffered stream
  		String dataAsString = new String(content.toByteArray());
  		return dataAsString;
		}
		catch (IOException e) {
			return null;
		}
	}
	
	@Override
	public synchronized void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
	}

	
	@Override
	public void onBackPressed() {
		String requestKey = extras.getString("requestKey");
		Log.d(TAG,"cancelling request " + requestKey);
		((MITClientData)MITClient.requestMap.get(requestKey)).setTouchstoneState(MITClient.TOUCHSTONE_CANCEL);
		finish();
	}

}
