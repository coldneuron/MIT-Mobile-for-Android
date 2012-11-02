package edu.mit.mitmobile2.maps;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import edu.mit.mitmobile2.MITNewsWidgetActivity;
import edu.mit.mitmobile2.Module;
import edu.mit.mitmobile2.R;
import edu.mit.mitmobile2.SliderActivity;
import edu.mit.mitmobile2.objs.MapItem;

public class MITMapDetailsSliderActivity extends SliderActivity {
	
	int MODE = MODE_ADD;
	public static int MODE_ADD = 0;
	public static int MODE_REMOVE = 1;
	String text_add_remove;

	public static final String KEY_SHARE_SUBJECT = "subject";
	public static final String KEY_SHARE_TEXT = "text";
	public static final String KEY_MODE = "add_remove";
	
	public static final String TEXT_ADD = "Add Bookmark";
	public static final String TEXT_RM = "Remove";
	
	String share_subject;
	String share_text;

	static final int MENU_MAPS_RESET = MENU_MODULE_HOME;
	static final int MENU_ADD_REMOVE = MENU_LAST + 1;
	static final int MENU_VIEW_PIN = MENU_LAST + 2;
	static final int MENU_GOOGLE   = MENU_LAST + 3;
	
	MapItem focusedMapItem;

	MapsDB mDB;
    
	List<MapItem> mapDetails;

	String courseId;

	private int mSelectedPosition;
	private int mStartPosition;
	private static final String MAP_ITEMS_KEY = "map_items";
	
	public static void launchMapDetails(Context context, List<MapItem> mapItems, int position) {
		Intent i = new Intent(context, MITMapDetailsSliderActivity.class);		
		i.putParcelableArrayListExtra(MAP_ITEMS_KEY, new ArrayList<MapItem>(mapItems));
		i.putExtra(KEY_POSITION, position);
		context.startActivity(i);	
	}

	/****************************************************/
    @Override
    public void onCreate(Bundle savedInstanceState) {

    	super.onCreate(savedInstanceState);

    	Bundle extras = getIntent().getExtras();
        if (extras!=null){  
        	share_subject = extras.getString(KEY_SHARE_SUBJECT); 
        	share_text = extras.getString(KEY_SHARE_TEXT); 
        	MODE = extras.getInt(KEY_MODE,MODE_ADD); 
        } 

        // FIXME
        if (share_subject==null) {
        	share_subject = "";
        	share_text = "";
        }

        mDB =  MapsDB.getInstance(this);
        
        mapDetails = getIntent().getParcelableArrayListExtra(MAP_ITEMS_KEY);
        mStartPosition = getPositionValue();
        
		createViews();
	}
    
    
	/****************************************************/
    void createViews() {

    	MapDetailsView cv;
    	
    	for (int x=0; x<mapDetails.size(); x++) {

    		MapItem mi = mapDetails.get(x);
    		
    		cv = new MapDetailsView(this,mi);
    		
    		addScreen(cv, mi.name, "Map Detail");    		
    	}
    
    	setPosition(mStartPosition);        
    
    }
	/****************************************************/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	
		Intent i;
		
		switch (item.getItemId()) {
		case MENU_HOME:
			i = new Intent(this,MITNewsWidgetActivity.class); 
			startActivity(i);
			finish();
			return true;
		case MENU_MAPS_RESET: 
			i = new Intent(this,MITMapActivity.class);  
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			return true;
		case MENU_ADD_REMOVE: 

			if (TEXT_ADD.equals(text_add_remove)) {
				mDB.saveMapItem(focusedMapItem);
			} else {
				mDB.delete(focusedMapItem);
			}
		    
		    return true;
		case MENU_VIEW_PIN: 
			MITMapActivity.viewMapItem(this, focusedMapItem);
			break;
		case MENU_GOOGLE: 
			
			// for more details look at
			// http://developer.android.com/guide/appendix/g-app-intents.html
			String uri = "geo:0,0?q=" + URLEncoder.encode(focusedMapItem.street+",Cambridge,MA"); 
			i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			startActivity(i);
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void prepareActivityOptionsMenu(Menu menu) {
		
		mSelectedPosition = getPosition();
		focusedMapItem = mapDetails.get(mSelectedPosition);
		
		MapItem databaseMapItem = mDB.retrieveMapItem(focusedMapItem.id);
		
		if (databaseMapItem==null) {
			text_add_remove = TEXT_ADD;
			menu.add(0, MENU_ADD_REMOVE, Menu.NONE, text_add_remove)
			  .setIcon(R.drawable.menu_add_bookmark);  
		}
		else {
			focusedMapItem = databaseMapItem;  // need id
			text_add_remove = TEXT_RM;
			menu.add(0, MENU_ADD_REMOVE, Menu.NONE, text_add_remove)
			  .setIcon(R.drawable.menu_remove_bookmark);  
		}
		
		menu.add(0, MENU_VIEW_PIN, Menu.NONE, "View on Map")  // shows pin
		  .setIcon(R.drawable.menu_view_on_map);
		menu.add(0, MENU_GOOGLE, Menu.NONE, "Google Map")
		  .setIcon(R.drawable.menu_google_map);
	}
	
	
	@Override
	protected Module getModule() {
		return new MapsModule();
	}
	@Override
	public boolean isModuleHomeActivity() {
		return false;
	}
	
}
