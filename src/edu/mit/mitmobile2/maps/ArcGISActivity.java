package edu.mit.mitmobile2.maps;

import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnZoomListener;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;
import com.google.android.maps.GeoPoint;

import edu.mit.mitmobile2.MITNewsWidgetActivity;
import edu.mit.mitmobile2.Module;
import edu.mit.mitmobile2.R;
import edu.mit.mitmobile2.TitleBar;
import edu.mit.mitmobile2.shuttles.ShuttlesActivity;
import edu.mit.mitmobile2.shuttles.ShuttlesModule;

public class ArcGISActivity extends MapBaseActivity2 {

	private static final String TAG = "ArcGISActivity";
	public static final String MODULE_SHUTTLE = "shuttle";

	// Generic Menu
	static final int MENU_SEARCH = Menu.FIRST+1;
	static final int MENU_MYLOC  = Menu.FIRST+2;
	static final int MENU_BOOKMARKS = Menu.FIRST+3;
	static final int MENU_BROWSE = Menu.FIRST+4;
	static final int MENU_LAYERS = Menu.FIRST+6;

	// Shuttle Menu
	static final int MENU_SHUTTLES = Menu.FIRST+5;
	//static final int MENU_REFRESH  = Menu.FIRST+6;
	static final int MENU_SHUTTLE_LIST_VIEW = Menu.FIRST+7;
	static final int MENU_MAP_LIST_VIEW = Menu.FIRST+8;
	static final int MENU_CALL_SAFERIDE = Menu.FIRST+9;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		progressContext = ArcGISActivity.this;
	    TitleBar titleBar = (TitleBar) findViewById(R.id.mapTitleBar);
	    titleBar.setTitle("Campus Map");

	    this.setSearchActivity(MapSearchActivity.class);
	    	    
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		
		switch (item.getItemId()) {
		case MENU_HOME:
			i = new Intent(this,MITNewsWidgetActivity.class);  
			startActivity(i);
			finish();
			break;
		case MENU_SEARCH: 
			//SearchManager sm = (SearchManager) ctx.getSystemService(Context.SEARCH_SERVICE);
			//sm.startSearch(null, false, null, false);
			onSearchRequested();
			break;
			
		case MENU_LAYERS: 
			selectLayers();
			break;
	
		case MENU_MYLOC: 
			GeoPoint me = myLocationOverlay.getMyLocation();
			//if (me!=null) mctrl.animateTo(me);
			break;
		case MENU_BOOKMARKS: 
			i = new Intent(this,MITMapBrowseResultsActivity.class);  
			startActivity(i);
			break;
		case MENU_BROWSE: 
			i = new Intent(this,MITMapBrowseCatsActivity.class);  
			startActivity(i);
			break;
			
		case MENU_SHUTTLES: 
			i = new Intent(this,ShuttlesActivity.class);  
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			finish();
			break; 
			
		case MENU_SHUTTLE_LIST_VIEW:
			finish();
			break;
			
		case MENU_MAP_LIST_VIEW:
			if(mListView.getVisibility() == View.GONE) {
				MapItemsAdapter adapter = new MapItemsAdapter(this, mMapItems);
				mListView.setAdapter(adapter);
				mListView.setOnItemClickListener(adapter.showMapDetailsOnItemClickListener());
				mapView.setVisibility(View.GONE);
				mListView.setVisibility(View.VISIBLE);
			} else {
				mListView.setVisibility(View.GONE);
				mapView.setVisibility(View.VISIBLE);
			}
			break;
			// FIXME
			/*
		case MENU_MAP_LIST_VIEW: 
			i = new Intent(this, MITMapActivity.class);
			i.putExtra(MITMapActivity.KEY_MODULE, MITMapActivity.MODULE_SHUTTLE); 
			RoutesAsyncListView sv = (RoutesAsyncListView) getScreen(getSelectedIndex());
			i.putExtra(MITMapActivity.KEY_HEADER_TITLE, sv.ri.title);
			Global.curStops = (ArrayList<Stops>) sv.m_stops;
			startActivity(i);
			break;
			*/
		case MENU_CALL_SAFERIDE: 
			i = new Intent(Intent.ACTION_DIAL);
			i.setData(Uri.parse("tel:617-253-2997"));
			startActivity(i);
			break;

		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		menu.clear();
		
		menu.add(0, MENU_HOME, Menu.NONE, "Home")
		  .setIcon(R.drawable.menu_home);
		
		if (module != null && module.equals(MODULE_SHUTTLE)) {
			Module shuttleModule = new ShuttlesModule();
			menu.add(0, MENU_SHUTTLES, Menu.NONE, shuttleModule.getMenuOptionTitle())
			  .setIcon(shuttleModule.getMenuIconResourceId());
			menu.add(0, MENU_SHUTTLE_LIST_VIEW, Menu.NONE, "List View")
			  .setIcon(R.drawable.menu_browse);
			//menu.add(0, MENU_CALL_SAFERIDE, Menu.NONE, "Saferide")
			//	.setIcon(android.R.drawable.ic_menu_call);
		} else {
			menu.add(0, MENU_SEARCH, Menu.NONE, "Search")
			  .setIcon(R.drawable.menu_search);
			menu.add(0, MENU_MYLOC, Menu.NONE, "My Location") 
			  .setIcon(R.drawable.menu_mylocation);
			menu.add(0, MENU_BOOKMARKS, Menu.NONE, "Bookmarks")
			  .setIcon(R.drawable.menu_bookmarks);
			menu.add(0, MENU_BROWSE, Menu.NONE, "Browse")
			  .setIcon(R.drawable.menu_browse);
			menu.add(0, MENU_LAYERS, Menu.NONE, "Layers")
			  .setIcon(R.drawable.menu_settings);
			
		}
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onSearchRequested() {
		if (MODULE_SHUTTLE.equals(module)) return false;
		return super.onSearchRequested();
	}
	
}
