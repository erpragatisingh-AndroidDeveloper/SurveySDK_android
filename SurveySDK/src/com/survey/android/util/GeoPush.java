package com.survey.android.util;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import android.content.Context;

import com.survey.android.session.Configuration;
import com.survey.android.util.ConstantData;
import com.survey.android.util.Log;
import com.survey.android.util.GeoPush;
import com.survey.android.webclient.RestClient;

import org.json.JSONException;

public class GeoPush {
	private static final String TAG = "GeoPush";
	private static Map<String, GeoPush> synchronizedPushes = Collections.synchronizedMap(new HashMap<String, GeoPush>()); 	
	private static long synchronizedPushesRefreshedAt;
	public static final long REFRESH_INTERVAL = 1000 * 60 * 120; // 2 HOURS
	
	private String token;
	private String geo_trigger_id;
	private long pushtimemillis;
	
	public String getToken() {
		return this.token;
	}	
	
	public String getGeoTriggerId() {
		return this.geo_trigger_id;
	}	
	
	public long getPushTimeMillis() {
		return this.pushtimemillis;
	}	
	
	public GeoPush(String token, String geo_trigger_id, long pushtimemillis)	{
		this.token = token;
		this.geo_trigger_id = geo_trigger_id;
		this.pushtimemillis = pushtimemillis;
	}
	
	public static void SendPush(String token, String geo_trigger_id,Context context) {
		//com.survey.android.session.Configuration 
	   	//configuration = new com.survey.android.session.Configuration(context);
		
		if(Configuration.isGeoPushEnabled()){
			GeoPush p = null;
			boolean push = false;
			Log.d(TAG, "(usertoken, geo_trigger_id) : " + "(" + token + "," + geo_trigger_id + ")");	
			Log.d(TAG, "synchronizedPushes: " + synchronizedPushes);		
			
			try {
				// check if list/hashmap of GeoPush exists 
				if (synchronizedPushes == null) {
					// create new list/hashmap of GeoPush objects
					synchronized (synchronizedPushes) {
						synchronizedPushes = Collections.synchronizedMap(new HashMap<String, GeoPush>());
						synchronizedPushesRefreshedAt = System.currentTimeMillis();
					}
				}

				synchronized (synchronizedPushes) {
					// check last refresh time and create new collection if over the configured amount of time
					if (System.currentTimeMillis() - synchronizedPushesRefreshedAt > REFRESH_INTERVAL) {
						// create new list/hashmap of GeoPush objects
						synchronizedPushes = Collections.synchronizedMap(new HashMap<String, GeoPush>());
						synchronizedPushesRefreshedAt = System.currentTimeMillis();
					}
				
					// check if current geo_trigger_id is already present in the hashmap
					if (synchronizedPushes.containsKey(geo_trigger_id)) {
						p = (GeoPush)synchronizedPushes.get(geo_trigger_id);
						long currenttimemillis = System.currentTimeMillis();
						// check if this geotrigger has been pushed within the configured interval, 
						// then do nothing
						if (currenttimemillis - p.getPushTimeMillis() < ConstantData.PUSH_CHECK_INTERVAL) {
							Log.d(TAG, "GeoTrigger Push already Called in last 30 min, Do Nothing");	
							return;
						}
						else
							push = true;
					}
					else {
						// add new GeoPush object to hashmap
						synchronized (synchronizedPushes) {					
							p = new GeoPush(token, geo_trigger_id, System.currentTimeMillis());
							synchronizedPushes.put(geo_trigger_id, p);
							push = true;
						}
					}
				}
				
				// check if geopush needs to be called
				if (push) {
					Log.d(TAG, "Calling GeoTrigger Push");
					RestClient.getGeoPush(token, geo_trigger_id);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
