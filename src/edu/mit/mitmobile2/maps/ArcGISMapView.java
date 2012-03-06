package edu.mit.mitmobile2.maps;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;

import edu.mit.mitmobile2.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

public class ArcGISMapView extends MapView{

	private static String TAG = "ArcGISMap";
	private ArcGISTiledMapServiceLayer serviceLayer;
	public Context mContext;
	private String queryUrl;
	private QueryTask queryTask;
	private Query query;
	ProgressDialog progress;
	private FeatureSet featureSet;
	private GraphicsLayer graphicsLayer;
	private Graphic[] highlightGraphics; 
	
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

	public class MapServerLayer {
		MapServerType mapServer;
		String layer;
		public MapServerLayer(MapServerType mapServer, String layer) {
			super();
			this.mapServer = mapServer;
			this.layer = layer;
		}
	}
	
	private static final String ARCGIS_SERVER_URL = "http://ims-pub.mit.edu/ArcGIS/rest/services/mobile/WhereIs_Base_Topo_Mobile/MapServer";
	private static final String GPV_URL = "http://ims-pub.mit.edu/ArcGIS/rest/services/base/GPV/MapServer";
	private static final String WHEREIS_150_URL = "http://ims-pub.mit.edu/ArcGIS/rest/services/base/WhereIs_150/MapServer";
	private static final String WHEREIS_BASE_URL = "http://ims-pub.mit.edu/ArcGIS/rest/services/base/WhereIs_Base/MapServer";
	private static final String WHEREIS_BASE_TOPO_URL = "http://ims-pub.mit.edu/ArcGIS/rest/services/base/WhereIs_Base_Topo/MapServer";
	private static final String WHEREIS_BASE_TOPO_MOBILE_URL = "http://ims-pub.mit.edu/ArcGIS/rest/services/base/WhereIs_Base_Topo_Mobile/MapServer";
	private static final String WHEREIS_MASK_URL = "http://ims-pub.mit.edu/ArcGIS/rest/services/base/WhereIs_Mask/MapServer";
	private static final String WHEREIS_ONLINE_BROOKLINE_URL = "http://ims-pub.mit.edu/ArcGIS/rest/services/base/WhereIs_Online_Brookline/MapServer";
	private static final String WHEREIS_WORKORDER_URL = "http://ims-pub.mit.edu/ArcGIS/rest/services/base/WhereIs_Workorder/MapServer";
	
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
	
	private MapServerType mapServerType;
	
	private Map<String, MapServerLayer> serverLayerMap;

	public ArcGISMapView(Context arg0) {
		super(arg0);
		this.mContext = arg0;
		Log.d(TAG,"ArcGISMapView(Context arg0)");
		// TODO Auto-generated constructor stub
	}
	
	public ArcGISMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Log.d(TAG,"ArcGISMapView(Context context, AttributeSet attrs, int defStyle)");
		// TODO Auto-generated constructor stub
	}

	public ArcGISMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		Log.d(TAG,"ArcGISMapView(Context context, AttributeSet attrs)");
		this.init();
		// TODO Auto-generated constructor stub
	}

	public ArcGISMapView(Context context, SpatialReference spatialreference,Envelope extent) {
		super(context, spatialreference, extent);
		Log.d(TAG,"ArcGISMapView(Context context, SpatialReference spatialreference,Envelope extent) ");
		// TODO Auto-generated constructor stub
	}

	public ArcGISMapView(Context context, String url, String user, String passwd, String bingMapsAppId) {
		super(context, url, user, passwd, bingMapsAppId);
		Log.d(TAG,"ArcGISMapView(Context context, String url, String user, String passwd, String bingMapsAppId)");
		// TODO Auto-generated constructor stub
	}

	public ArcGISMapView(Context context, String url, String user, String passwd) {
		super(context, url, user, passwd);
		Log.d(TAG,"ArcGISMapView(Context context, String url, String user, String passwd)");
		// TODO Auto-generated constructor stub
	}

	public void init() {
		
		// Define Layers
		serviceLayer = new ArcGISTiledMapServiceLayer(ARCGIS_SERVER_URL);
		graphicsLayer = new GraphicsLayer();
		
		// Add Layers
		this.addLayer(serviceLayer);
		this.addLayer(graphicsLayer);

		this.serverLayerMap = new HashMap<String, MapServerLayer>();

		// BEGIN GPV LAYERS
		
		// Add Trees Layer
		this.serverLayerMap.put(TREES, new MapServerLayer(mapServerType.GPV,"6"));

		// Add Stairs Layer
		this.serverLayerMap.put(STAIRS, new MapServerLayer(mapServerType.GPV,"10"));
		
		// Add Buildings Other Layer
		this.serverLayerMap.put(BUILDINGS_OTHER, new MapServerLayer(mapServerType.GPV,"14"));

		// Add Buildings Other Dropshadow Layer
		this.serverLayerMap.put(BUILDINGS_OTHER_DROPSHADOW, new MapServerLayer(mapServerType.GPV,"15"));

		// Add Sport Layer
		this.serverLayerMap.put(SPORT, new MapServerLayer(mapServerType.GPV,"18"));

		// Add Sidewalk Layer
		this.serverLayerMap.put(SIDEWALK, new MapServerLayer(mapServerType.GPV,"19"));

		// Add Pavement Markings Layer
		this.serverLayerMap.put(PAVEMENT_MARKINGS, new MapServerLayer(mapServerType.GPV,"20"));

		// Add Parking Layer
		this.serverLayerMap.put(PARKING, new MapServerLayer(mapServerType.GPV,"21"));

		// Add Grass Layer
		this.serverLayerMap.put(GRASS, new MapServerLayer(mapServerType.GPV,"22"));

		// Add Walkway Layer
		this.serverLayerMap.put(WALKWAY, new MapServerLayer(mapServerType.GPV,"23"));

		// Add Parking Lots Layer
		this.serverLayerMap.put(PARKING_LOTS, new MapServerLayer(mapServerType.GPV,"24"));

		// Add Blocks Layer
		this.serverLayerMap.put(BLOCKS, new MapServerLayer(mapServerType.GPV,"26"));

		// END GPV LAYERS
		
		// BEGIN WHEREIS_BASE LAYERS
		
		// T_STOPS_LARGE
		this.serverLayerMap.put(T_STOPS_LARGE, new MapServerLayer(mapServerType.WHEREIS_BASE,"5"));

		// T_STOPS_SMALL
		this.serverLayerMap.put(T_STOPS_SMALL, new MapServerLayer(mapServerType.WHEREIS_BASE,"6"));

		// BUILDING_NAMES
		this.serverLayerMap.put(BUILDING_NAMES, new MapServerLayer(mapServerType.WHEREIS_BASE,"3"));
		
		// END WHEREIS_BASE LAYERS
		
		// Add Buildings Layer
		this.serverLayerMap.put(BUILDINGS, new MapServerLayer(mapServerType.WHEREIS_BASE,"9"));

		// Add Landmarks Layer
		this.serverLayerMap.put(LANDMARKS, new MapServerLayer(mapServerType.WHEREIS_BASE,"7"));
		
	}
	
	public String getQueryUrl(String queryType) {
		String url = null;
		
		MapServerLayer mapServerLayer = (MapServerLayer) this.serverLayerMap.get(queryType);
		
		if (mapServerLayer.mapServer == mapServerType.GPV) {
			url = GPV_URL + "/" + mapServerLayer.layer;
		}

		if (mapServerLayer.mapServer == mapServerType.WHEREIS_150) {
			url = WHEREIS_150_URL + "/" + mapServerLayer.layer;
		}

		if (mapServerLayer.mapServer == mapServerType.WHEREIS_BASE) {
			url = WHEREIS_BASE_URL + "/" + mapServerLayer.layer;
		}

		if (mapServerLayer.mapServer == mapServerType.WHEREIS_BASE_TOPO) {
			url = WHEREIS_BASE_TOPO_URL + "/" + mapServerLayer.layer;
		}

		if (mapServerLayer.mapServer == mapServerType.WHEREIS_BASE_TOPO_MOBILE) {
			url = WHEREIS_BASE_TOPO_MOBILE_URL + "/" + mapServerLayer.layer;
		}

		if (mapServerLayer.mapServer == mapServerType.WHEREIS_MASK) {
			url = WHEREIS_MASK_URL + "/" + mapServerLayer.layer;
		}

		if (mapServerLayer.mapServer == mapServerType.WHEREIS_ONLINE_BROOKLINE) {
			url = WHEREIS_ONLINE_BROOKLINE_URL + "/" + mapServerLayer.layer;
		}

		if (mapServerLayer.mapServer == mapServerType.WHEREIS_WORKORDER) {
			url = WHEREIS_WORKORDER_URL + "/" + mapServerLayer.layer;
		}
		
		Log.d(TAG,"query url = " + url);
		return url;
	}
	
	public void runQuery(String queryType,String queryCriteria, String where) {
		queryUrl = this.getQueryUrl(queryType);
		Log.d(TAG,"queryUrl = " + queryUrl);
		queryTask = new QueryTask( queryUrl);
		query = new Query();
		query.setReturnGeometry(true);
		query.setReturnIdsOnly(false);
		query.setText(queryCriteria);
		query.setWhere(where);
		//if (queryCriteria.trim().length() < 1) {
		//	query.setWhere("1=1"); // return all results
		//}
		String[] queryParams = {queryUrl};
		AsyncQueryTask asyncQuery = new AsyncQueryTask();
		asyncQuery.execute(queryParams);
		//Debug results
		//Graphic graphics[] = featureSet.getGraphics();
		//Log.d(TAG,"featureSet: " + graphics.length + " result(s) found");

	}

	/**
	 * 
	 * Query Task executes asynchronously.
	 * 
	 */
	private class AsyncQueryTask extends AsyncTask<String, Void, FeatureSet> {
		protected void onPreExecute() {
			progress = ProgressDialog.show(mContext, "",
					"Please wait....query task is executing");
		}

		/**
		 * First member in parameter array is the query URL; second member is
		 * the where clause.
		 */
		protected FeatureSet doInBackground(String... queryParams) {
			Log.d(TAG,"doInBackground()");			
			try {
				featureSet = queryTask.execute(query);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				return featureSet;
			}
			return featureSet;

		}

		protected void onPostExecute(FeatureSet result) {
			processResult(result);
			String message = "";
//			if (result != null) {
//
//				if (result.getGraphics() != null) {
//					
//					// clear graphics layer
//					graphicsLayer.removeAll();
//					
//					Graphic graphics[] = result.getGraphics();
//					message = graphics.length + " result(s) found";
//					Log.d(TAG,"num graphics = " + graphics.length);
//					highlightGraphics = new Graphic[graphics.length];
//					for (int i = 0; i < graphics.length; i++) {
//						Graphic graphic = graphics[i];
//						Geometry geometry = graphic.getGeometry();
//						Log.d(TAG,"geometry type = " + geometry.getType());
//						/////////////////////////
//		                Random r = new Random();
//		                int color = Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255));
//
//		                /*
//		                 * Create appropriate symbol, based on geometry type
//		                 */
//		                if (geometry.getType().name().equalsIgnoreCase("point")) {
//		                  //SimpleMarkerSymbol sms = new SimpleMarkerSymbol(color, 20, STYLE.SQUARE);
//		                  PictureMarkerSymbol pms = new PictureMarkerSymbol((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.map_red_pin));
//		                  //highlightGraphics[i] = new Graphic(geometry, sms);
//		                  highlightGraphics[i] = new Graphic(geometry, pms);			                
//		                } else if (geometry.getType().name().equalsIgnoreCase("polyline")) {
//		                  SimpleLineSymbol sls = new SimpleLineSymbol(color, 5);
//		                  highlightGraphics[i] = new Graphic(geometry, sls);
//		                } else if (geometry.getType().name().equalsIgnoreCase("polygon")) {
//		                  SimpleFillSymbol sfs = new SimpleFillSymbol(color);
//		                  sfs.setAlpha(75);
//		                  highlightGraphics[i] = new Graphic(geometry, sfs);
//		                }
//
//		                
//		                /**
//		                 * set the Graphic's geometry, add it to GraphicLayer and refresh the Graphic Layer
//		                 */
//		                graphicsLayer.addGraphic(highlightGraphics[i]);
//		                Log.d(TAG,"graphics layer spatial reference = " + graphicsLayer.getSpatialReference().getText());
//					}
//				}

				
		}

		// PROCESSRESULTS
		protected void processResult(FeatureSet result) {
			String message = "";
			if (result != null) {
				if (result.getGraphics() != null) {
					
					// clear graphics layer
					graphicsLayer.removeAll();
					
					Graphic graphics[] = result.getGraphics();
					message = graphics.length + " result(s) found";
					Log.d(TAG,"num graphics = " + graphics.length);
					highlightGraphics = new Graphic[graphics.length];
					for (int i = 0; i < graphics.length; i++) {
						Graphic graphic = graphics[i];
						Geometry geometry = graphic.getGeometry();
						Log.d(TAG,"geometry type = " + geometry.getType());
						/////////////////////////
		                Random r = new Random();
		                int color = Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255));

		                /*
		                 * Create appropriate symbol, based on geometry type
		                 */
		                if (geometry.getType().name().equalsIgnoreCase("point")) {
		                  //SimpleMarkerSymbol sms = new SimpleMarkerSymbol(color, 20, STYLE.SQUARE);
		                  PictureMarkerSymbol pms = new PictureMarkerSymbol((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.map_red_pin));
		                  //highlightGraphics[i] = new Graphic(geometry, sms);
		                  highlightGraphics[i] = new Graphic(geometry, pms);			                
		                } else if (geometry.getType().name().equalsIgnoreCase("polyline")) {
		                  //SimpleLineSymbol sls = new SimpleLineSymbol(color, 5);
		                  //highlightGraphics[i] = new Graphic(geometry, sls);
		                 
		                } else if (geometry.getType().name().equalsIgnoreCase("polygon")) {
		                  Envelope env = new Envelope();
		                  geometry.queryEnvelope(env);
		                  Point point = env.getCenter();
		                  PictureMarkerSymbol pms = new PictureMarkerSymbol((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.map_red_pin));
		                  highlightGraphics[i] = new Graphic(point, pms);			                
		                  // temporarily use center points for polylines
		                  //SimpleFillSymbol sfs = new SimpleFillSymbol(color);
		                  //sfs.setAlpha(75);
		                  //highlightGraphics[i] = new Graphic(geometry, sfs);
		                }

		                
		                /**
		                 * set the Graphic's geometry, add it to GraphicLayer and refresh the Graphic Layer
		                 */
		                graphicsLayer.addGraphic(highlightGraphics[i]);
		                Log.d(TAG,"graphics layer spatial reference = " + graphicsLayer.getSpatialReference().getText());
					}
				}
			}
			progress.dismiss();

			Toast toast = Toast.makeText(mContext, message,
					Toast.LENGTH_LONG);
			toast.show();

		}
		// End Process Results
		
	}


}

