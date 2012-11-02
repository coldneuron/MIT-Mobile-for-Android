package edu.mit.mitmobile2;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.util.Log;
import edu.mit.mitmobile2.MobileWebApi.HttpClientType;

public class MITConnectionWrapper extends ConnectionWrapper {

	public static final String TAG = "MITConnectionWrapper";

	static Context mContext;
	
	public MITConnectionWrapper(Context context, HttpClientType httpClientType) {
		super();
		Log.d(TAG,"MITConnectionWrapper");
		this.mContext = context;
	}
		
	public HttpResponse httpClientResponse(HttpGet httpGet) throws ClientProtocolException, IOException {
		Log.d(TAG,"httpClientResponse from MITConnectionWrapper");
		MITClient mitClient = new MITClient(mContext);
		HttpResponse response = null;
		Log.d(TAG,"httpGet = " + httpGet.getURI());
		try {
			Log.d(TAG,"before get response");
			response = mitClient.getResponse(httpGet);
			Log.d(TAG,"after get response");
			//DEBUG
			//Log.d(TAG,"response = " + response);
		}
		catch (Exception e) {
			Log.d(TAG,e.getStackTrace().toString());
			//Log.d(TAG,e.getMessage());
		}
		return response;
	}
}
