package edu.mit.mitmobile2.maps;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.MarkerSymbol;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;

import edu.mit.mitmobile2.LoaderBar;
import edu.mit.mitmobile2.MITSearchRecentSuggestions;
import edu.mit.mitmobile2.MobileWebApi;
import edu.mit.mitmobile2.R;
import edu.mit.mitmobile2.MobileWebApi.DefaultErrorListener;
import edu.mit.mitmobile2.MobileWebApi.JSONArrayResponseListener;
import edu.mit.mitmobile2.maps.MapSearch;
import edu.mit.mitmobile2.objs.MapItem;
import edu.mit.mitmobile2.objs.SearchResults;

public abstract class MapBaseActivity2 extends Activity {

	private static final String TAG = "MapBaseActivity2"; 
	protected MapView mapView;
	protected ArcGISTiledMapServiceLayer mapServiceLayer;
	protected GraphicsLayer mapGraphicsLayer;
	protected GraphicsLayer buildingsGraphicsLayer;
	
	protected Context mContext;

	ProgressDialog progress;
	MapSearch mMapSearch;
	FeatureSet featureSet; 
	String queryUrl;
	QueryTask queryTask;
	Query query;
	Context progressContext;

	//*******************************************************************************************************************************************
	// This Section Defines all the layers provided by the map server so that they can be accessed with a simple key word such as building, parking, etc

	// URLS For ArcGIS Server
	private static final String ARCGIS_SERVER_URL = "http://ims-pub.mit.edu/ArcGIS/rest/services/mobile/WhereIs_Base_Topo_Mobile/MapServer";
	private static final String GPV_URL = "http://ims-pub.mit.edu/ArcGIS/rest/services/base/GPV/MapServer";
	private static final String WHEREIS_150_URL = "http://ims-pub.mit.edu/ArcGIS/rest/services/base/WhereIs_150/MapServer";
	private static final String WHEREIS_BASE_URL = "http://ims-pub.mit.edu/ArcGIS/rest/services/base/WhereIs_Base/MapServer";
	//private static final String WHEREIS_BASE_URL = "http://mobile-dev.mit.edu";
	private static final String WHEREIS_BASE_TOPO_URL = "http://ims-pub.mit.edu/ArcGIS/rest/services/base/WhereIs_Base_Topo/MapServer";
	private static final String WHEREIS_BASE_TOPO_MOBILE_URL = "http://ims-pub.mit.edu/ArcGIS/rest/services/base/WhereIs_Base_Topo_Mobile/MapServer";
	private static final String WHEREIS_MASK_URL = "http://ims-pub.mit.edu/ArcGIS/rest/services/base/WhereIs_Mask/MapServer";
	private static final String WHEREIS_ONLINE_BROOKLINE_URL = "http://ims-pub.mit.edu/ArcGIS/rest/services/base/WhereIs_Online_Brookline/MapServer";
	private static final String WHEREIS_WORKORDER_URL = "http://ims-pub.mit.edu/ArcGIS/rest/services/base/WhereIs_Workorder/MapServer";

	// MAP LAYER INDEXES
	// map layers are referenced by index, not by a name. reserve layers for a specific search/task
	public static int MAP_LAYER_INDEX = 0;
	public static int BUILDINGS_LAYER_INDEX = 1;
	
	// Enumerated type for various map server layers
	public enum MapServerType {
		GPV,
		WHEREIS_150,
	    WHEREIS_BASE,
	    WHEREIS_BASE_TOPO,
	    WHEREIS_BASE_TOPO_MOBILE,
	    WHEREIS_MASK,
	    WHEREIS_ONLINE_BROOKLINE,
	    WHEREIS_WORKORDER
	}

	// MapServerLayer 
	public class MapServerLayer {
		MapServerType mapServer;
		String layer;
		public MapServerLayer(MapServerType mapServer, String layer) {
			super();
			this.mapServer = mapServer;
			this.layer = layer;
		}
	}

	private MapServerType mapServerType;
	
	private static Map<String, MapServerLayer> serverLayerMap;

	// GPV Layer
	public static final String TREES = "trees"; // #6
	public static final String STAIRS = "stairs"; // #10
	public static final String BUILDINGS_OTHER = "buildings other"; // #14
	public static final String BUILDINGS_OTHER_DROPSHADOW = "buildings other dropshadow"; // #15
	public static final String SPORT = "sport"; // #18
	public static final String SIDEWALK = "sidewalk"; // #19
	public static final String PAVEMENT_MARKINGS = "pavement markings"; // #20
	public static final String PARKING = "parking"; // #21
	public static final String GRASS = "grass"; // #22
	public static final String WALKWAY = "walkway"; // #23
	public static final String PARKING_LOTS = "parking lots"; // #24
	public static final String BLOCKS = "blocks"; // #26

	// WhereIs_Base Layer
	public static final String T_STOPS_LARGE = "t stops large"; // #5
	public static final String T_STOPS_SMALL = "t stops small"; // #6
	public static final String BUILDING_NAMES = "building names"; // #3
	
	
	public static final String BUILDINGS = "buildings";
	public static final String LANDMARKS = "landmarks";

	// End of Map Server Layer Definitions
	//*******************************************************************************************************************************************

	protected Class searchActivity;
	
	// used to reset out of List Mode
	public static final String KEY_VIEW_PINS = "view_pins";
	
	
	public static final String KEY_TITLE = "title";
	public static final String KEY_SNIPPET = "snippet";
	public static final String KEY_MODULE = "module";
	public static final String KEY_HEADER_TITLE = "header_title";
	public static final String KEY_HEADER_SUBTITLE = "header_subtitle";
	
	static final int MENU_HOME   = Menu.FIRST;

	public static final String KEY_POSITION = "pos";
	protected static final String SEARCH_TERM_KEY = "search";

	//protected MapController mctrl;
	protected FixedMyLocation myLocationOverlay;
	protected List<MapItem> mMapItems;
	//protected GeoPoint center;
	
	protected String title;
	protected String snippet;
	protected String module;
	protected int bubble_pos = -1;

	//protected List<Overlay>  ovrlys;
	
	protected String mHeaderTitle = null;
	protected String mHeaderSubtitle = null;
	
	protected MITMapsDataModel mdm;
	
	protected LoaderBar mapSearchLoader;
	protected ListView mListView;
	
	
	static int INIT_ZOOM = 17;
	static int INIT_ZOOM_ONE_ITEM = 18;
	
	protected static final String MAP_ITEMS_KEY = "map_items";
	
	/****************************************************/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		mContext = this;
		Log.d(TAG,"onCreate()");
	    
		
		//mapView.setBuiltInZoomControls(true);	

		// Initialize Map View
		if (mapView == null) {
			Log.d(TAG,"initializing mapView");
			mapInit();
		}
		else {
			Log.d(TAG,"mapView not null");			
		}
	    
//    	Bundle extras = getIntent().getExtras();
//    		    
//        if (extras!=null){ 
//
//        	String action = getIntent().getAction();
//    		if(action != null && action.equals(Intent.ACTION_SEARCH)) {
//    			mMapSearch = (MapSearch)getIntent().getExtras().getSerializable(MapBaseActivity2.MAP_SEARCH_OBJECT);
//    			if (mMapSearch == null) {
//    				Log.d(TAG,"mMapSearch is null");
//    			}
//    			else {
//    				Log.d(TAG,"search type = " + mMapSearch.getType());
//    				Log.d(TAG,"performing search");
//    				performSearch(mMapSearch);
//    			}
//    		}
//
//        }
		/*
        else {
			// run a query test
			mMapSearch = new MapSearch();
			mMapSearch.type = MapSearchType.CANNED_BUILDING;
			mMapSearch.building = new String[] {"32"};
			performSearch(mMapSearch);    			
		}
        */	    
		Object init = getLastNonConfigurationInstance();
		if (init != null) {
			mapView.restoreState((String) init);
		}
	}
	
	
	/****************************************************/
	/****************************************************/
	protected List<MapItem> loadMapItems(Intent intent) {
		return intent.getParcelableArrayListExtra(MAP_ITEMS_KEY);
	}
	/****************************************************/

	

	/****************************************************/
//	protected void doSearch(final String searchTerm) {
//		final LoaderBar loaderBar = (LoaderBar) findViewById(R.id.mapSearchLoader);
//		loaderBar.setLoadingMessage("Searching for " + searchTerm);
//		loaderBar.setFailedMessage("Search failed!");
//		loaderBar.enableAnimation();
//		loaderBar.startLoading();
//		
//		final Handler updateResultsUI = new Handler() {
//			
//			@Override
//			public void handleMessage(Message message) {
//				if(message.arg1 == MobileWebApi.SUCCESS) {
//					mMapItems = MITMapsDataModel.getSearchResults(searchTerm);
//					if(mMapItems.size() == 0) {
//						Toast.makeText(MapBaseActivity2.this, "No matches found", Toast.LENGTH_LONG).show();
//					}
//					//setOverlays();
//					loaderBar.setLastLoaded(new Date());
//				} else {
//					Toast.makeText(MapBaseActivity2.this, MobileWebApi.NETWORK_ERROR, Toast.LENGTH_LONG).show();
//					loaderBar.errorLoading();
//				}
//			}
//		};
//				
//		MITSearchRecentSuggestions suggestions = new MITSearchRecentSuggestions(this, MapsSearchSuggestionsProvider.AUTHORITY, MapsSearchSuggestionsProvider.MODE);
//		suggestions.saveRecentQuery(searchTerm.toLowerCase(), null);
//		
//		MITMapsDataModel.executeSearch(searchTerm, updateResultsUI, this);
//	}
	/****************************************************/
	@Override
	public void onLowMemory() {
		super.onLowMemory();
	//	mapView.lowMemory = true;
	}
	/****************************************************/
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG,"onResume");
		mapView.unpause();
		
    	Bundle extras = getIntent().getExtras();

        if (extras!=null){ 

        	mMapSearch = (MapSearch)getIntent().getExtras().getSerializable(MapBaseActivity2.MAP_SEARCH_OBJECT);
    		if (mMapSearch == null) {
    			Log.d(TAG,"mMapSearch is null");
    		}
    		else {
    			Log.d(TAG,"search type = " + mMapSearch.getType());
    			Log.d(TAG,"performing search");
    			performSearch(mMapSearch);
    		}
        }
        else {
        	Log.d(TAG,"no extras");
        }

	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG,"onPause");
		mapView.pause();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG,"onDestroy");
		mapView.recycle();
		//mapView.stop();
	}
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		setIntent(intent);
		Log.d(TAG,"onNewIntent");
	}


	/****************************************************/
	//@Override
	//protected boolean isRouteDisplayed() {
	//    return false;
	//}
	
	protected int getLayoutId() {
		return R.layout.maps2;
	}
	
	// TODO set configChanges attrib
	/*
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    // Checks the orientation of the screen
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    	//
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	        //
	    }
	}
	*/

	//********************************************************************************************************************************
	// NEW METHODS
	public void mapInit() {

		setContentView(getLayoutId());

	    mapView = new MapView(mContext);
	    
		mapView = (MapView) findViewById(R.id.map);
		Log.d(TAG,"mapView = (MapView) findViewById(R.id.map)");
		mapSearchLoader = (LoaderBar)findViewById(R.id.mapSearchLoader);
		
		
		// Define Layers
		mapServiceLayer = new ArcGISTiledMapServiceLayer(ARCGIS_SERVER_URL);
		//mapGraphicsLayer = new GraphicsLayer();

		// Add Layers

		//Layer 0 is the map layer
		mapView.addLayer(mapServiceLayer);
		Log.d(TAG,"mapView.addLayer(mapServiceLayer)");

		// Layer 1 is the buildings layer
		buildingsGraphicsLayer = new GraphicsLayer();
		mapView.addLayer(buildingsGraphicsLayer);

		MapBaseActivity2.serverLayerMap = new HashMap<String, MapServerLayer>();

		// BEGIN GPV LAYERS
		
		// Add Trees Layer
		MapBaseActivity2.serverLayerMap.put(TREES, new MapServerLayer(MapServerType.GPV,"6"));

		// Add Stairs Layer
		MapBaseActivity2.serverLayerMap.put(STAIRS, new MapServerLayer(MapServerType.GPV,"10"));
		
		// Add Buildings Other Layer
		MapBaseActivity2.serverLayerMap.put(BUILDINGS_OTHER, new MapServerLayer(MapServerType.GPV,"14"));

		// Add Buildings Other Dropshadow Layer
		MapBaseActivity2.serverLayerMap.put(BUILDINGS_OTHER_DROPSHADOW, new MapServerLayer(MapServerType.GPV,"15"));

		// Add Sport Layer
		MapBaseActivity2.serverLayerMap.put(SPORT, new MapServerLayer(MapServerType.GPV,"18"));

		// Add Sidewalk Layer
		MapBaseActivity2.serverLayerMap.put(SIDEWALK, new MapServerLayer(MapServerType.GPV,"19"));

		// Add Pavement Markings Layer
		MapBaseActivity2.serverLayerMap.put(PAVEMENT_MARKINGS, new MapServerLayer(MapServerType.GPV,"20"));

		// Add Parking Layer
		MapBaseActivity2.serverLayerMap.put(PARKING, new MapServerLayer(MapServerType.GPV,"21"));

		// Add Grass Layer
		MapBaseActivity2.serverLayerMap.put(GRASS, new MapServerLayer(MapServerType.GPV,"22"));

		// Add Walkway Layer
		MapBaseActivity2.serverLayerMap.put(WALKWAY, new MapServerLayer(MapServerType.GPV,"23"));

		// Add Parking Lots Layer
		MapBaseActivity2.serverLayerMap.put(PARKING_LOTS, new MapServerLayer(MapServerType.GPV,"24"));

		// Add Blocks Layer
		MapBaseActivity2.serverLayerMap.put(BLOCKS, new MapServerLayer(MapServerType.GPV,"26"));

		// END GPV LAYERS
		
		// BEGIN WHEREIS_BASE LAYERS
		
		// T_STOPS_LARGE
		MapBaseActivity2.serverLayerMap.put(T_STOPS_LARGE, new MapServerLayer(MapServerType.WHEREIS_BASE,"5"));

		// T_STOPS_SMALL
		MapBaseActivity2.serverLayerMap.put(T_STOPS_SMALL, new MapServerLayer(MapServerType.WHEREIS_BASE,"6"));

		// BUILDING_NAMES
		MapBaseActivity2.serverLayerMap.put(BUILDING_NAMES, new MapServerLayer(MapServerType.WHEREIS_BASE,"3"));
		
		// END WHEREIS_BASE LAYERS
		
		// Add Buildings Layer
		MapBaseActivity2.serverLayerMap.put(BUILDINGS, new MapServerLayer(MapServerType.WHEREIS_BASE,"9"));
		Log.d(TAG,"added server layer map: " + BUILDINGS);
		// Add Landmarks Layer
		MapBaseActivity2.serverLayerMap.put(LANDMARKS, new MapServerLayer(MapServerType.WHEREIS_BASE,"7"));
		
	}

	public static String getQueryUrl(String queryType) {
		String url = null;
		MapServerLayer mapServerLayer = (MapServerLayer) MapBaseActivity2.serverLayerMap.get(queryType);
		
		if (mapServerLayer.mapServer == MapServerType.GPV) {
			url = GPV_URL + "/" + mapServerLayer.layer;
		}

		if (mapServerLayer.mapServer == MapServerType.WHEREIS_150) {
			url = WHEREIS_150_URL + "/" + mapServerLayer.layer;
		}

		if (mapServerLayer.mapServer == MapServerType.WHEREIS_BASE) {
			url = WHEREIS_BASE_URL + "/" + mapServerLayer.layer;
		}

		if (mapServerLayer.mapServer == MapServerType.WHEREIS_BASE_TOPO) {
			url = WHEREIS_BASE_TOPO_URL + "/" + mapServerLayer.layer;
		}

		if (mapServerLayer.mapServer == MapServerType.WHEREIS_BASE_TOPO_MOBILE) {
			url = WHEREIS_BASE_TOPO_MOBILE_URL + "/" + mapServerLayer.layer;
		}

		if (mapServerLayer.mapServer == MapServerType.WHEREIS_MASK) {
			url = WHEREIS_MASK_URL + "/" + mapServerLayer.layer;
		}

		if (mapServerLayer.mapServer == MapServerType.WHEREIS_ONLINE_BROOKLINE) {
			url = WHEREIS_ONLINE_BROOKLINE_URL + "/" + mapServerLayer.layer;
		}

		if (mapServerLayer.mapServer == MapServerType.WHEREIS_WORKORDER) {
			url = WHEREIS_WORKORDER_URL + "/" + mapServerLayer.layer;
		}
		
		Log.d(TAG,"query url = " + url);
		return url;
	}

	public static final String KEYWORD = "KEYWORD";
	
	public static final String MAP_SEARCH_OBJECT = "MAP_SEARCH_OBJECT";
	public static final String RETURN_ACTIVITY = "RETURN_ACTIVITY";
	
	public static class MapSearchType {
	    public static final String KEYWORD = "KEYWORD";
	    public static final String CANNED_BUILDING = "CANNED_BUILDING"; 
	    public static final String CANNED_LIBRARY = "CANNED_LIBRARY";
	    public static final String CANNED_PARKING = "CANNED_PARKING";
	    public static final String CANNED_DINING = "CANNED_DINING";
	    public static final String CANNED_RESTROOM = "CANNED_RESTROOM";
	}
	
	public static class MapSymbolType {
		
		// These Symbol Types use images from R.drawable
	    public static final String MAP_BUILDING = "MAP_BUILDING"; 
	    public static final String MAP_ATHENA = "MAP_ATHENA"; 
	    public static final String MAP_CURRENT = "MAP_CURRENT";
	    public static final String MAP_CURRENT_STOP = "MAP_CURRENT_STOP"; 
	    public static final String MAP_ENDING_ARROW = "MAP_ENDING_ARROW"; 
	    public static final String MAP_FOOD_SERVICES = "MAP_FOOD_SERVICES"; 
	    public static final String MAP_FUTURE = "MAP_FUTURE"; 
	    public static final String MAP_GREEN_SPACE = "MAP_GREEN_SPACEP"; 
	    public static final String MAP_HOTEL = "MAP_HOTEL";
	    public static final String MAP_LIBRARIES = "MAP_LIBRARIES"; 
	    public static final String MAP_MUSEUM = "MAP_MUSEUM"; 	    
	    public static final String MAP_PARKING = "MAP_PARKING";
	    public static final String MAP_PAST = "MAP_PAST";
	    public static final String MAP_SHUTTLE = "MAP_SHUTTLE";
	    public static final String MAP_SHUTTLE_NEXT = "MAP_SHUTTLE_NEXT";
	    public static final String MAP_RED_PIN = "MAP_RED_PIN";
	    public static final String MAP_RESIDENCE = "MAP_RESIDENCE";
	    public static final String MAP_ROOM = "MAP_ROOM";
	    public static final String MAP_STREET = "MAP_STREET";
	    
	    // These Symbol Types are generated images
	    public static final String MAP_CIRCLE = "MAP_CIRCLE";
	    public static final String MAP_CROSS = "MAP_CROSS";
	    public static final String MAP_DIAMOND = "MAP_DIAMOND";
	    public static final String MAP_SQUARE = "MAP_SQUARE";
	    public static final String MAP_X = "MAP_X";
	}

	private static int DEFAULT_SYMBOL_SIZE = 20;
	private static int DEFAULT_SYMBOL_COLOR = Color.RED;
	
	public class MapSearchOption {
		MapSearchType type;
		String label;
	}
	
	public enum MapCriteriaType {
		KEYWORD,
		HANDICAPPED_ACCESSIBLE,
		ID_REQUIRED,
		NEAR_ME
	}
	
	// END NEW METHODS
	//*************************************************************************************************************************

	public boolean onSearchRequested() {
		Log.d(TAG,"onSearchRequested()");
		try {
			Intent intent = new Intent(this,this.getSearchActivity());  
			intent.putExtra(RETURN_ACTIVITY,this.getClass().getName());
			startActivity(intent);
			Log.d(TAG,"search activity started");
		}
		catch (Exception e) {
			Log.d(TAG,e.getMessage());
		}
		return true;
	}
	public Class getSearchActivity() {
		return searchActivity;
	}
	public void setSearchActivity(Class searchActivity) {
		this.searchActivity = searchActivity;
	}

	public void performSearch(MapSearch mMapSearch) {
		Log.d(TAG,"performSearch()");

		// Key Word Search
		
		// Building Search
		runQuery(mMapSearch);
	}
	
	private class AsyncQueryTask extends AsyncTask<MapSearch, Void, List> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			Log.d(TAG,"onPreExecute()");
			//mapSearchLoader.startLoading();
		}

		@Override
		protected List doInBackground(MapSearch... params) {
			// TODO Auto-generated method stub
			List data = new ArrayList();
			MapSearch m = params[0];
			FeatureSet featureSet = new FeatureSet();
//			String queryUrl;
//			String building = "";

			// BUILDING SEARCH
			if (m.type.equalsIgnoreCase(MapBaseActivity2.MapSearchType.CANNED_BUILDING)) {
				featureSet = buildingSearch(m); // lets see if this is run synchronously 
			}
			
			// KEYWORD SEARCH
			if (m.type.equalsIgnoreCase(MapBaseActivity2.MapSearchType.KEYWORD)) {
				featureSet = keywordSearch(m);
			}
						
			data.add(m);
			data.add(featureSet);
			return data;

		}

		// BUILDING SEARCH
		public FeatureSet buildingSearch(MapSearch m) {
				FeatureSet featureSet = new FeatureSet();
				String queryUrl = MapBaseActivity2.getQueryUrl(MapBaseActivity2.BUILDINGS);
				String building = "";
				if (m.building != null && m.building.length > 0 ) {
					building = (m.building)[0]; // ultimately, this should be an array. We;re just using the first value for testing purposes 
				}
				queryTask = new QueryTask(queryUrl);
				query = new Query();
				query.setReturnGeometry(true);
				query.setReturnIdsOnly(false);
				query.setText(building);
				try {
					featureSet = queryTask.execute(query);
				}
				catch (Exception e) {
					Log.d(TAG,"exception");
					e.printStackTrace();
					featureSet = null;
				}
				return featureSet;
		}

		// KEYWORD SEARCH 
		public FeatureSet keywordSearch(MapSearch m) {
			FeatureSet featureSet = new FeatureSet();
			String[] keywords = (String[])m.getCriteria().get(MapSearch.CriteriaType.KEY_WORDS);
			String searchTerm = keywords[0];
			
			// First search the whereis server to get buildings matching the keyword search
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("q", searchTerm);
			params.put("command", "search");
			MobileWebApi webApi = new MobileWebApi(false, true, "Campus map", mContext, keywordUiHandler);
			
			webApi.requestJSONArray("/map", params, new JSONArrayResponseListener(new DefaultErrorListener(keywordUiHandler), null) {
				@Override
				public void onResponse(JSONArray array) throws JSONException {
					
					List<MapItem> mapsItems = MapParser.parseMapItems(array);
					
					for (MapItem m : mapsItems) {
						Log.d(TAG,m.bldgnum);
					}
										
					MobileWebApi.sendSuccessMessage(keywordUiHandler, mapsItems);
				}
				
			});
			
			return null;
		}
		
		Handler keywordUiHandler = null;
		
//		HashMap<String, String> params = new HashMap<String, String>();
//
//		params.put("q", searchTerm);
//		params.put("command", "search");
//		MobileWebApi webApi = new MobileWebApi(false, true, "Campus map", context, uiHandler);
//		webApi.requestJSONArray("/map", params, new JSONArrayResponseListener(new DefaultErrorListener(uiHandler), null) {

		
		@Override
		protected void onPostExecute(List data) {
			// TODO Auto-generated method stub
			MapSearch m = (MapSearch)data.get(0);
			FeatureSet result = (FeatureSet)data.get(1);
			Geometry[] geometryList;
			
			// get the appropriate symbol based on the mapsearch parameters
			MarkerSymbol markerSymbol = getMarkerSymbol(m);
			String message = "";
			Log.d(TAG,"onPostExecute");
			if (result == null) {
				Log.d(TAG,"result is null");
				message = "no results found";
			}
			else {
				Graphic[] highlightGraphics;
				if (result.getGraphics() != null) {
					Log.d(TAG,"graphics not null");	
						// clear graphics layer
						buildingsGraphicsLayer.removeAll();
						
						Graphic graphics[] = result.getGraphics();
						geometryList = new Geometry[graphics.length];
						message = graphics.length + " results found";
						Log.d(TAG,"num graphics = " + graphics.length);
						highlightGraphics = new Graphic[graphics.length];
						
						for (int i = 0; i < graphics.length; i++) {
							Graphic graphic = graphics[i];
							Geometry geometry = graphic.getGeometry();
							geometryList[i] = geometry;
							Log.d(TAG,"geometry type = " + geometry.getType());
							/////////////////////////
			                Random r = new Random();
			                int color = Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255));
			                Log.d(TAG,"color value = " + color);

			                /*
			                 * Create appropriate symbol, based on geometry type
			                 */
			                if (geometry.getType().name().equalsIgnoreCase("point")) {
			                  highlightGraphics[i] = new Graphic(geometry, markerSymbol);			                
			                } 
			                else if (geometry.getType().name().equalsIgnoreCase("polyline")) {
			                  //SimpleLineSymbol sls = new SimpleLineSymbol(color, 5);
			                  //highlightGraphics[i] = new Graphic(geometry, sls);
			                 
			                } 
			                else if (geometry.getType().name().equalsIgnoreCase("polygon")) {
			                  Envelope env = new Envelope();
			                  geometry.queryEnvelope(env);
			            
			                  Point point = env.getCenter();

			                  highlightGraphics[i] = new Graphic(point, markerSymbol);
			                }
						}
						
						// Add highlight graphics to graphics layer
						buildingsGraphicsLayer.addGraphics(highlightGraphics);

						// The the extent of the map to the extent of the search results
						Geometry unionGeometry = GeometryEngine.union(geometryList,mapView.getSpatialReference());
						mapView.setExtent(unionGeometry,100);				

						Log.d(TAG,"num layers on map = " + mapView.getLayers().length);
					}
					else {
						Log.d(TAG,"graphics isnull");	
					}
				}
				//progress.dismiss();

				Toast toast = Toast.makeText(mContext, message,
						Toast.LENGTH_LONG);
				toast.show();
			}
		}
		


	// RUNQUERY
	public void runQuery(MapSearch mMapSearch) {
		Log.d(TAG,"runQuery()");
		AsyncQueryTask asyncQuery = new AsyncQueryTask();
		asyncQuery.execute(mMapSearch);
	}

	// ASYNCQUERY
	/**
	 * 
	 * Query Task executes asynchronously.
	 * 
	 */

	public MarkerSymbol getMarkerSymbol(MapSearch m) {
		MarkerSymbol s = null;
		int color = MapBaseActivity2.DEFAULT_SYMBOL_COLOR;
	
		// If No marker image is defined, use the default marker for the search type
		if (m.image == null || m.image.length() == 0) {
			
			// CANNED_BUILDING
			if (m.type.equalsIgnoreCase(MapBaseActivity2.MapSearchType.CANNED_BUILDING)) {
				 PictureMarkerSymbol pms = new PictureMarkerSymbol((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.map_building_number));
				 return pms;
			}

			// CANNED PARKING
			if (m.type.equalsIgnoreCase(MapBaseActivity2.MapSearchType.CANNED_LIBRARY)) {
				 PictureMarkerSymbol pms = new PictureMarkerSymbol((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.map_building_number));
				 return pms;
			}
		}
		else {
			
			// If no color is defined, used the default color
			if (m.color == null || m.color.trim().length() == 0) {
				color = MapBaseActivity2.DEFAULT_SYMBOL_COLOR;
			}
			
			if (m.image.equalsIgnoreCase(MapSymbolType.MAP_ATHENA)) {
				 PictureMarkerSymbol pms = new PictureMarkerSymbol((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.map_athena));
				 return pms;
			}
			if (m.image.equalsIgnoreCase(MapSymbolType.MAP_BUILDING)) {
				 PictureMarkerSymbol pms = new PictureMarkerSymbol((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.map_building_number));				
				 return pms;
			}
			if (m.image.equalsIgnoreCase(MapSymbolType.MAP_CURRENT)) {
				 PictureMarkerSymbol pms = new PictureMarkerSymbol((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.map_current));				
				 return pms;
			}
			if (m.image.equalsIgnoreCase(MapSymbolType.MAP_CURRENT_STOP)) {
				 PictureMarkerSymbol pms = new PictureMarkerSymbol((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.map_currentstop));				
				 return pms;
			}
			if (m.image.equalsIgnoreCase(MapSymbolType.MAP_ENDING_ARROW)) {
				 PictureMarkerSymbol pms = new PictureMarkerSymbol((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.map_ending_arrow));				
				 return pms;
			}
			if (m.image.equalsIgnoreCase(MapSymbolType.MAP_FOOD_SERVICES)) {
				 PictureMarkerSymbol pms = new PictureMarkerSymbol((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.map_food_services));				
				 return pms;
			}
			if (m.image.equalsIgnoreCase(MapSymbolType.MAP_FUTURE)) {
				 PictureMarkerSymbol pms = new PictureMarkerSymbol((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.map_future));				
				 return pms;
			}
			if (m.image.equalsIgnoreCase(MapSymbolType.MAP_GREEN_SPACE)) {
				 PictureMarkerSymbol pms = new PictureMarkerSymbol((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.map_green_space));				
				 return pms;
			}
			if (m.image.equalsIgnoreCase(MapSymbolType.MAP_HOTEL)) {
				 PictureMarkerSymbol pms = new PictureMarkerSymbol((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.map_hotel));				
				 return pms;
			}
			if (m.image.equalsIgnoreCase(MapSymbolType.MAP_LIBRARIES)) {
				 PictureMarkerSymbol pms = new PictureMarkerSymbol((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.map_libraries));				
				 return pms;
			}
			if (m.image.equalsIgnoreCase(MapSymbolType.MAP_MUSEUM)) {
				 PictureMarkerSymbol pms = new PictureMarkerSymbol((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.map_museum));				
				 return pms;
			}
			if (m.image.equalsIgnoreCase(MapSymbolType.MAP_PAST)) {
				 PictureMarkerSymbol pms = new PictureMarkerSymbol((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.map_past));				
				 return pms;
			}
			if (m.image.equalsIgnoreCase(MapSymbolType.MAP_SHUTTLE)) {
				 PictureMarkerSymbol pms = new PictureMarkerSymbol((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.map_pin_shuttle_stop_complete));				
				 return pms;
			}
			if (m.image.equalsIgnoreCase(MapSymbolType.MAP_SHUTTLE_NEXT)) {
				 PictureMarkerSymbol pms = new PictureMarkerSymbol((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.map_pin_shuttle_stop_complete_next));				
				 return pms;
			}
			if (m.image.equalsIgnoreCase(MapSymbolType.MAP_RED_PIN)) {
				 PictureMarkerSymbol pms = new PictureMarkerSymbol((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.map_red_pin));				
				 return pms;
			}
			if (m.image.equalsIgnoreCase(MapSymbolType.MAP_RESIDENCE)) {
				 PictureMarkerSymbol pms = new PictureMarkerSymbol((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.map_residence));				
				 return pms;
			}
			if (m.image.equalsIgnoreCase(MapSymbolType.MAP_ROOM)) {
				 PictureMarkerSymbol pms = new PictureMarkerSymbol((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.map_selected_rooms));				
				 return pms;
			}
			if (m.image.equalsIgnoreCase(MapSymbolType.MAP_STREET)) {
				 PictureMarkerSymbol pms = new PictureMarkerSymbol((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.map_street));				
				 return pms;
			}

			// Simple Marker Symbols

			// Circle
			if (m.image.equalsIgnoreCase(MapSymbolType.MAP_CIRCLE)) {
				 SimpleMarkerSymbol sms = new SimpleMarkerSymbol(color, MapBaseActivity2.DEFAULT_SYMBOL_SIZE, STYLE.CIRCLE);			
				 return sms;
			}

			// Cross
			if (m.image.equalsIgnoreCase(MapSymbolType.MAP_CROSS)) {
				 SimpleMarkerSymbol sms = new SimpleMarkerSymbol(color, MapBaseActivity2.DEFAULT_SYMBOL_SIZE, STYLE.CROSS);			
				 return sms;
			}

			// Diamond
			if (m.image.equalsIgnoreCase(MapSymbolType.MAP_DIAMOND)) {
				 SimpleMarkerSymbol sms = new SimpleMarkerSymbol(color, MapBaseActivity2.DEFAULT_SYMBOL_SIZE, STYLE.DIAMOND);			
				 return sms;
			}

			// Square
			if (m.image.equalsIgnoreCase(MapSymbolType.MAP_SQUARE)) {
				 SimpleMarkerSymbol sms = new SimpleMarkerSymbol(color, MapBaseActivity2.DEFAULT_SYMBOL_SIZE, STYLE.SQUARE);			
				 return sms;
			}
			
			// X
			if (m.image.equalsIgnoreCase(MapSymbolType.MAP_X)) {
				 SimpleMarkerSymbol sms = new SimpleMarkerSymbol(color, MapBaseActivity2.DEFAULT_SYMBOL_SIZE, STYLE.X);			
				 return sms;
			}

		}
		return s;
	}
		
}

