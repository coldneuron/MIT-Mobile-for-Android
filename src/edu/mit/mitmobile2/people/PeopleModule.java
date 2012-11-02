package edu.mit.mitmobile2.people;

import android.app.Activity;

import edu.mit.mitmobile2.Module;
import edu.mit.mitmobile2.R;

public class PeopleModule extends Module {

	@Override
	public String getLongName() {
		return "People Directory";
	}

	@Override
	public Class<? extends Activity> getModuleHomeActivity() {
		return PeopleActivity.class;
	}

	@Override
	public String getShortName() {
		return "Directory";
	}

	@Override
	public int getMenuIconResourceId() {
		return R.drawable.menu_directory;
	}
	
	@Override
	public String getMenuOptionTitle() {
		return "People Home";
	}

	@Override
	public int getHomeIconResourceId() {
		return R.drawable.home_people;
	}
}
