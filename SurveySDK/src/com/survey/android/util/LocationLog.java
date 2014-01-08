package com.survey.android.util;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import com.survey.android.util.ConstantData;
import com.survey.android.util.Log;
import com.survey.android.util.LocationLog;
import com.survey.android.webclient.RestClient;

import org.json.JSONException;

@SuppressWarnings("unused")
public class LocationLog {
	private static final String TAG = "LocationLog";
	private static Map<String, LocationLog> synchronizedLocationLog = Collections.synchronizedMap(new HashMap<String, LocationLog>()); 	
	private static long synchronizedLocationLogRefreshedAt;
	public static final long REFRESH_INTERVAL = 1000 * 60 * 120; // 2 HOURS
	
	private String token;
	private String geo_trigger_id;
	private double latitude;
	private double longitude;
	private String reason;
	private long pushtimemillis;
	
	public long getPushTimeMillis() {
		return this.pushtimemillis;
	}	
	
	public LocationLog(String token, String geo_trigger_id, 
			double latitude, double longitude, 
			String reason, long pushtimemillis)	{
		this.token = token;
		this.geo_trigger_id = geo_trigger_id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.reason = reason;
		this.pushtimemillis = pushtimemillis;
	}
	
	public static void SetLocationLog(String token, String geo_trigger_id, 
			double latitude, double longitude, String reason) {
		LocationLog p = null;		
		boolean loglocation = false;
		Log.d(TAG, "(usertoken, geo_trigger_id, reason) : " + "(" + token + "," + geo_trigger_id + "," + reason + ")");	
		Log.d(TAG, "synchronizedLocationLog: " + synchronizedLocationLog);		
		String key = geo_trigger_id + reason;
		
		try {
			// check if list/hashmap of LocationLog exists 
			if (synchronizedLocationLog == null) {
				// create new list/hashmap of LocationLog objects
				synchronized (synchronizedLocationLog) {
					synchronizedLocationLog = Collections.synchronizedMap(new HashMap<String, LocationLog>());
					synchronizedLocationLogRefreshedAt = System.currentTimeMillis();					
				}
			}
			
			synchronized (synchronizedLocationLog) {			
				// check last refresh time and create new collection if over the configured amount of time
				if (System.currentTimeMillis() - synchronizedLocationLogRefreshedAt > REFRESH_INTERVAL) {
					// create new list/hashmap of LocationLog objects
					synchronizedLocationLog = Collections.synchronizedMap(new HashMap<String, LocationLog>());
					synchronizedLocationLogRefreshedAt = System.currentTimeMillis();
				}				
			
				// check if current geo_trigger_id is already present in the hashmap
				if (synchronizedLocationLog.containsKey(key)) {
					p = (LocationLog)synchronizedLocationLog.get(key);
					long currenttimemillis = System.currentTimeMillis();
					// check if this geotrigger has been pushed within the configured interval, 
					// then do nothing
					if (currenttimemillis - p.getPushTimeMillis() < ConstantData.LOCATIONLOG_INTERVAL) {
						Log.d(TAG, "GeoTrigger Fence Event already recorded in last 10 min, Do Nothing");	
						return;
					}
					else
						loglocation = true;
				}
				else {
					// add new LocationLog object to hashmap
					synchronized (synchronizedLocationLog) {					
						p = new LocationLog(token, geo_trigger_id, latitude, longitude, reason, System.currentTimeMillis());
						synchronizedLocationLog.put(key, p);
						loglocation = true;
					}
				}
			}	
			
			// check if locationlog needs to be recorded			
			if (loglocation) {
				Log.d(TAG, "Calling GeoTrigger LocationLog api");
				RestClient.setLocationLog(token, geo_trigger_id, latitude, longitude, reason);				
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
