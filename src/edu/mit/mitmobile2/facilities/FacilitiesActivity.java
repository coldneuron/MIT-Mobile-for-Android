package edu.mit.mitmobile2.facilities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import edu.mit.mitmobile2.CommonActions;
import edu.mit.mitmobile2.Global;
import edu.mit.mitmobile2.Module;
import edu.mit.mitmobile2.ModuleActivity;
import edu.mit.mitmobile2.R;
import edu.mit.mitmobile2.TwoLineActionRow;
import edu.mit.mitmobile2.objs.FacilitiesItem.LocationRecord;

//public class FacilitiesActivity extends ModuleActivity implements OnClickListener {
public class FacilitiesActivity extends ModuleActivity {
	

	private Context mContext;	

	TextView emergencyContactsTV;

	SharedPreferences pref;
	
	
	
	public static final String TAG = "FacilitiesActivity";
	private static final int MENU_INFO = 0;
	
	/****************************************************/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG,"onCreate()");
		super.onCreate(savedInstanceState);
		mContext = this;
        Handler uiHandler = new Handler();

        // call getVersionMap incase it failed in the Global activity before the correct mobile server was selected
        Global.getVersionInfo(mContext, uiHandler);
		createViews();
	}

	private void createViews() {
		setContentView(R.layout.facilities_main);

		String title1 = "";
		String title2 = "";

		// Report a Problem
		TwoLineActionRow reportAProblemActionRow = (TwoLineActionRow) findViewById(R.id.facilitiesReportAProblemActionRow);
		title1 = "Report a Problem";
		title2 = "";
		reportAProblemActionRow.setTitle(title1 + " " + title2, TextView.BufferType.SPANNABLE);
		//reportAProblemActionRow.setActionIconResource(R.drawable.arrow_right_normal);
		reportAProblemActionRow.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent;
				Log.d(TAG,"clicked report a problem");
				intent = new Intent(mContext, FacilitiesProblemLocationActivity.class);					
				startActivity(intent);
			}
		});
		
		// Call Facilities
		final String phone = mContext.getString(R.string.facilities_phone);
		TwoLineActionRow callFacilitiesActionRow = (TwoLineActionRow) findViewById(R.id.facilitiesCallFacilitiesActionRow);
		title1 = "Call Facilities";
		title2 = "(" + phone + ")";
		callFacilitiesActionRow.setTitle(title1 + " " + title2, TextView.BufferType.SPANNABLE);
				
		callFacilitiesActionRow.setActionIconResource(R.drawable.action_phone);
		callFacilitiesActionRow.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CommonActions.callPhone(FacilitiesActivity.this, phone);
			}
		});

		// Email Facilities
		final String facilitiesEmail = mContext.getString(R.string.facilities_email);
		TwoLineActionRow emailFacilitiesActionRow = (TwoLineActionRow) findViewById(R.id.facilitiesEmailFacilitiesActionRow);
		title1 = "Email Facilities";
		title2 = "(" + facilitiesEmail + ")";
		emailFacilitiesActionRow.setTitle(title1 + " " + title2, TextView.BufferType.SPANNABLE);
				
		emailFacilitiesActionRow.setActionIconResource(R.drawable.action_email);
		emailFacilitiesActionRow.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CommonActions.composeEmail(mContext, "facilities@mit.edu");
			}
		});

	}

	@Override
	protected Module getModule() {
		return new FacilitiesModule();
	}

	@Override
	public boolean isModuleHomeActivity() {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_INFO:
			Intent intent = new Intent(mContext, FacilitiesInfoActivity.class);					
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected void prepareActivityOptionsMenu(Menu menu) { 
		menu.add(0, MENU_INFO, Menu.NONE, "Info")
		  .setIcon(R.drawable.menu_about);
	}


	public static void launchActivityForLocation(Context context, LocationRecord location) {
		Global.sharedData.getFacilitiesData().setLocationId(location.id);
		Global.sharedData.getFacilitiesData().setLocationName(location.name);
		Global.sharedData.getFacilitiesData().setBuildingNumber(location.bldgnum);
		
		if(location.leased_bldg_services) {
			FacilitiesLeasedBuildingActivity.launch(context, location);
		}
		// If there is no building number for the selected location, go to the inside/outside selection activity, else retrieve the rooms for the location
		else if (location.bldgnum == null || location.bldgnum.equals("")) {
			Intent intent = new Intent(context, FacilitiesInsideOutsideActivity.class);
			context.startActivity(intent);
		}
		else {
			Intent intent = new Intent(context, FacilitiesRoomLocationsActivity.class);
			context.startActivity(intent);
		}
	}
}
