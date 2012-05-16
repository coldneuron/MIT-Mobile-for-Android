package edu.mit.mitmobile2.facilities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import edu.mit.mitmobile2.MITMenuItem;
import edu.mit.mitmobile2.NewModule;
import edu.mit.mitmobile2.R;

public class FacilitiesModule extends NewModule {

	@Override
	public String getLongName() {
		return "Bldg Services";
	}

	@Override
	public String getShortName() {
		return "Bldg Services";
	}
	
	@Override
	public Class<? extends Activity> getModuleHomeActivity() {
		return FacilitiesActivity.class;
	}

	@Override
	public int getMenuIconResourceId() {
		return R.drawable.menu_facilities;
	}

	@Override
	public String getMenuOptionTitle() {
		return "Bldg Services";
	}
	
	@Override
	public int getHomeIconResourceId() {
		return R.drawable.home_facilities;
	}

	@Override
	public List<MITMenuItem> getPrimaryOptions() {
		// TODO Auto-generated method stub
		List<MITMenuItem> menuItems = new ArrayList<MITMenuItem>();
		menuItems.add(new MITMenuItem("menu_info", "", R.drawable.menu_info));
		return menuItems;
	}

	@Override
	public List<MITMenuItem> getSecondaryOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onItemSelected(Activity activity, String id) {
		// TODO Auto-generated method stub
		if (id.equals("menu_info")) {
			Intent intent = new Intent(activity, FacilitiesInfoActivity.class);					
			activity.startActivity(intent);
		}
		return false;
	}
}
