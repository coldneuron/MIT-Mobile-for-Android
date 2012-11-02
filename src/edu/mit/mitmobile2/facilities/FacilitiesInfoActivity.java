package edu.mit.mitmobile2.facilities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import edu.mit.mitmobile2.Module;
import edu.mit.mitmobile2.ModuleActivity;
import edu.mit.mitmobile2.R;

//public class FacilitiesActivity extends ModuleActivity implements OnClickListener {
public class FacilitiesInfoActivity extends ModuleActivity {

	SharedPreferences pref;
	
	public static final String TAG = "FacilitiesInfoActivity";
	
	/****************************************************/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG,"onCreate()");
		super.onCreate(savedInstanceState);
		createViews();
	}

	private void createViews() {
		setContentView(R.layout.facilities_info);
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
	protected void prepareActivityOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		
	}


}
