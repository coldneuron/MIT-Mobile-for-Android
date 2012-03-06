package edu.mit.mitmobile2.maps;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import edu.mit.mitmobile2.R;
import edu.mit.mitmobile2.maps.MapBaseActivity2.MapSearchType;

public class MapSearchActivity extends Activity {

	private static final String TAG = "MapSearchActivity";
	Context mContext;
	EditText mMapSearchText;
	ListView mListView;
	String searchTypes[] = new String[6];
	Bundle extras;
	MapSearch mMapSearch;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContext = this;
		
		mMapSearch = new MapSearch();
		extras = getIntent().getExtras();

		this.setContentView(R.layout.map_search);

		//  populate searchTypes[] - later on this will hold complex objects, for now, just strings
		searchTypes[0] = MapBaseActivity2.MapSearchType.KEYWORD;
		searchTypes[1] = MapBaseActivity2.MapSearchType.CANNED_BUILDING;
		searchTypes[2] = MapBaseActivity2.MapSearchType.CANNED_PARKING;
		searchTypes[3] = MapBaseActivity2.MapSearchType.CANNED_DINING;
		searchTypes[4] = MapBaseActivity2.MapSearchType.CANNED_LIBRARY;
		searchTypes[5] = MapBaseActivity2.MapSearchType.CANNED_RESTROOM;
		
		ArrayList<String> entries = 
	        new ArrayList<String>(Arrays.asList(searchTypes));

		ArrayAdapter<String> arrAdapt=
	        new ArrayAdapter<String>(this, R.layout.simple_row, entries);
		
		mMapSearchText = (EditText)findViewById(R.id.mapSearchText);
	
		mListView = (ListView)findViewById(R.id.mapSearchListView);
		mListView.setAdapter(arrAdapt);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				String searchType = searchTypes[position];
				String searchText = mMapSearchText.getEditableText().toString();

				String returnActivityName = extras.getString(MapBaseActivity2.RETURN_ACTIVITY);
				
				// set MapSearch data
				mMapSearch.setType(searchType);
				if (searchType.equalsIgnoreCase(MapSearchType.CANNED_BUILDING)) {
					mMapSearch.setBuilding(new String[] {searchText});
				}
				else {
					mMapSearch.getCriteria().put(MapSearch.CriteriaType.KEY_WORDS,new String[] {searchText} );					
				}
				
				try {
					Class returnClass;
					returnClass = Class.forName(returnActivityName);
					Intent i = new Intent(mContext,returnClass);
					i.setAction(Intent.ACTION_SEARCH);

					Log.d(TAG,"before put extra");
			        i.putExtra(MapBaseActivity2.MAP_SEARCH_OBJECT, mMapSearch);
					Log.d(TAG,"after put extra");
					startActivity(i);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 
			}
		});


	}

	@Override
	public boolean onSearchRequested() {
		// TODO Auto-generated method stub
		return super.onSearchRequested();
	}
	
}
