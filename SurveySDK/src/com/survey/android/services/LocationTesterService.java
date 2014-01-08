package com.survey.android.services;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.nsphere.locationprovider.LocationTrackerManager;
import com.nsphere.locationprovider.LocationTrackerManager.GeofencingUpdatesListener;
import com.nsphere.locationprovider.beans.Coordinates;
import com.nsphere.locationprovider.beans.Geofence;
import com.nsphere.locationprovider.common.GeofenceTrackingCriteria;
import com.nsphere.locationprovider.enums.TrackingAccuracy;
import com.nsphere.locationprovider.utils.TrackerLogger;

public class LocationTesterService extends Service {
	
	  protected static final String TAG = "LocationTesterService";
	  public static boolean IS_RUNNING = false;

	  private LocationTrackerManager locationTracker;
	
	  private Looper mServiceLooper;
	  private ServiceHandler mServiceHandler;
	  
	  public static final int BASIC_LOCATION_TRACKING_RESULT = 0;
	  public static final int GEOFENCE_ENTERED_RESULT = 1;
	  public static final int GEOFENCE_EXITED_RESULT = 2;
	  public static final int GEOFENCE_DWELLED_RESULT = 3;
	  public static final int GEOFENCE_STOPED_RESULT = 4;
	  
		GeofencingUpdatesListener geofenceListener = new GeofencingUpdatesListener() {


			public void onGeofenceEntered(Location location, int geofenceID) {
				Log.d(TAG, "Location arrived: " + location.getLatitude() + ", longitude: " + location.getLongitude() + ", by " + location.getProvider());
				sendResultBackToActivity(location, GEOFENCE_ENTERED_RESULT, getSurveyID(geofenceID));
			}

			public void onGeofenceExit(Location location, int geofenceID) {
				Log.d(TAG, "Location arrived: " + location.getLatitude() + ", longitude: " + location.getLongitude() + ", by " + location.getProvider());
				sendResultBackToActivity(location, GEOFENCE_EXITED_RESULT, getSurveyID(geofenceID));				
			}

			public void onGeofenceDwell(Location location, int geofenceID) {
				Log.d(TAG, "Location arrived: " + location.getLatitude() + ", longitude: " + location.getLongitude() + ", by " + location.getProvider());
				sendResultBackToActivity(location, GEOFENCE_DWELLED_RESULT, getSurveyID(geofenceID));
			}

			public void onGeofenceTrackingStopped() {
				sendResultBackToActivity(null, GEOFENCE_STOPED_RESULT, null);;
			}
			
			private String getSurveyID(int geofenceID) {
				String surveyID = null;
				for (int i = 0; i <surveyGeoNotifications.length(); i++) {
					if (i == geofenceID) {
						try {
							surveyID = surveyGeoNotifications.getJSONObject(i).optString("survey_id");
							break;							
						} catch (JSONException e) {
							Log.e(TAG, "JSONexc: " + e);
						}
					}
				}
				return surveyID;
				
			}
			
		};

		private JSONArray surveyGeoNotifications;

		

	  // Handler that receives messages from the thread
		private final class ServiceHandler extends Handler {
			public ServiceHandler(Looper looper) {
				super(looper);
			}
			@Override
			public void handleMessage(Message msg) {
				
				Bundle data = msg.getData();
				
				int accuracy = 0;    	 
				long geoFenceRefreshInterval = 0;
				float geoFenceUserMinDistance = 0;
				long duration = 0;

				boolean isPolygonal = false;


				GeofenceTrackingCriteria geoFenceTrackingCriteria = new GeofenceTrackingCriteria();
				if (accuracy == 0) {
					geoFenceTrackingCriteria.setAccuracy(TrackingAccuracy.ACCURACY_HIGH);
				} else {
					geoFenceTrackingCriteria.setAccuracy(TrackingAccuracy.ACCURACY_LOW);
				}
				geoFenceTrackingCriteria.setMinTimeInterval(geoFenceRefreshInterval);
				geoFenceTrackingCriteria.setMinDistance(geoFenceUserMinDistance);
				geoFenceTrackingCriteria.setDuration(duration);
				
				try {
					List<Geofence> geofences = new ArrayList<Geofence>();
					for (int i = 0; i < surveyGeoNotifications.length(); i++) {
						JSONObject currSurveyGeoNotification = surveyGeoNotifications.optJSONObject(i);
						JSONArray currGeoTriggers = currSurveyGeoNotification.optJSONArray("geo_triggers");
						for (int j = 0; j < currGeoTriggers.length(); j++) {
							JSONObject currGeoTrigger = currGeoTriggers.getJSONObject(i);
							double latitude = currGeoTrigger.getDouble("latitude");
							double longitude = currGeoTrigger.getDouble("longitude");
							geofences.add(new Geofence(new Coordinates(latitude, longitude), (float) currGeoTrigger.getDouble("radius")));
						}

					}

					geoFenceTrackingCriteria.setGeofences(geofences);

					if (!isPolygonal) {
						double latitude = data.getDouble("latitude", 44.821984);
						double longitude = data.getDouble("latitude", 20.418735);
						float radius = 1000;
						TrackerLogger.d(TAG, "Accuracy: " + accuracy
								+ ", geoFenceRefreshInterval: " + geoFenceRefreshInterval
								+ ", geoFenceUserMinDistance: " + geoFenceUserMinDistance
								+ ", duration: " + duration + ", lattitude: " + latitude
								+ ", longitude: " + longitude + ", radius: " + radius);



						locationTracker.startMultipleGeofencing(geoFenceTrackingCriteria, geofenceListener);
					} else {
						//
						//					try {
						//						double[] xpoints, ypoints;
						//						JSONArray polygonalPointsJSON = new JSONArray(sharedPrefs.getString(SharedPrefsUtil.GEOFENCE_POLYGONAL_POINTS, SharedPrefsUtil.SHARED_PREFS_DEFAULT_STRING_VALUE));
						//						xpoints = new double[polygonalPointsJSON.length()];
						//						ypoints = new double[polygonalPointsJSON.length()];
						//
						//						for (int i = 0; i < polygonalPointsJSON.length(); i++) {
						//							JSONObject polygonalPoint = polygonalPointsJSON.getJSONObject(i);
						//							xpoints[i] = Double.parseDouble(polygonalPoint.optString("lat"));
						//							ypoints[i] = Double.parseDouble(polygonalPoint.optString("lon"));
						//						}
						//						locationTracker.startGeofencing(new Geofence(xpoints, ypoints), geoFenceTrackingCriteria, geofenceListener);
						//
						//					} catch (JSONException e) {
						//						Log.w(TAG, "There's no geofence polygonal points in shared preferences.");
						//					} catch (Exception e) {
						//
						//					}

					}

				} catch (JSONException e) {
					Log.e(TAG, "JSONExc: " + e);
				}

				
			}
		}
	  
		public void sendResultBackToActivity(Location location, int msgCode, String surveyID) {
			Intent newIntent = new Intent("locationBroadcast");
	
			Bundle bundle = new Bundle();
			bundle.putParcelable("locationUpdate", location);
			bundle.putInt("msgCode", msgCode);
			bundle.putString("survey_id", surveyID);
			newIntent.putExtras(bundle);

			sendBroadcast(newIntent);
			
		}

	  @Override
	  public void onCreate() {
	    // Start up the thread running the service.  Note that we create a
	    // separate thread because the service normally runs in the process's
	    // main thread, which we don't want to block.  We also make it
	    // background priority so CPU-intensive work will not disrupt our UI.
		Log.i(TAG, "Location Service onCreate");
	    HandlerThread thread = new HandlerThread("ServiceStartArguments",
	            10);
	    thread.start();
	    
	    // Get the HandlerThread's Looper and use it for our Handler 
	    mServiceLooper = thread.getLooper();
	    mServiceHandler = new ServiceHandler(mServiceLooper);
	  }

	  @Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
		  Log.i(TAG, "Location Service starting");
		  IS_RUNNING = true;

		  Bundle intentExtras = intent.getExtras();

		  String surveyGeoNotificationsString = intentExtras.getString("geoSurveyNotifications");
		  Log.d(TAG, "surveyGeoNotifications: " + surveyGeoNotificationsString);
		  
		  JSONArray receivedSurveyGeoNotifications = null;
		try {
			receivedSurveyGeoNotifications = new JSONArray(surveyGeoNotificationsString);
			if (locationTracker == null) {
				  locationTracker = new LocationTrackerManager(getApplicationContext());
				  Log.d(TAG, "initializing surveyGeoNotifications");
				  surveyGeoNotifications = receivedSurveyGeoNotifications;
			  } else {
				  updateSurveyGeoNotifications(receivedSurveyGeoNotifications);
			  }

			  // For each start request, send a message to start a job and deliver the
			  // start ID so we know which request we're stopping when we finish the job
			  Bundle bundle = new Bundle();
//			  bundle.putDouble("latitude", latitude);
//			  bundle.putDouble("longitude", longitude);

			  Message msg = mServiceHandler.obtainMessage();
			  msg.setData(bundle);
			  msg.arg1 = startId;
			  mServiceHandler.sendMessage(msg);
		} catch (JSONException e) {
			Log.e(TAG, "JSONexc: " + e);
		}

		  // If we get killed, after returning from here, restart
		  return START_REDELIVER_INTENT;
	  }

	  private void updateSurveyGeoNotifications(
			JSONArray receivedSurveyGeoNotifications) throws JSONException {
		if (surveyGeoNotifications == null) {
			Log.d(TAG, "initializing surveyGeoNotifications");
			surveyGeoNotifications = receivedSurveyGeoNotifications;
		} else {
			try {			
				Log.d(TAG, "updating surveyGeoNotifications");
				for (int i = 0; i <receivedSurveyGeoNotifications.length(); i++) {
					JSONObject currNewGeoNotificaiton = receivedSurveyGeoNotifications.getJSONObject(i);

					if (!surveyGeoNotificationExists(currNewGeoNotificaiton)) {
						Log.d(TAG, "adding new geoNotification: " + currNewGeoNotificaiton.optString("_id"));
						surveyGeoNotifications.put(currNewGeoNotificaiton);
					} else {

						for (int j = 0; j < surveyGeoNotifications.length(); j++) {
							JSONObject currSurveyGeoNotification = surveyGeoNotifications.getJSONObject(j);

							if (currSurveyGeoNotification.optString("survey_id").equals(currNewGeoNotificaiton.optString("_id"))) {

								JSONArray currGeoTriggers = currSurveyGeoNotification.optJSONArray("geo_triggers");
								JSONArray newGeoTriggers = currNewGeoNotificaiton.optJSONArray("geo_triggers");
								for (int k = 0; k < newGeoTriggers.length(); k++) {
									JSONObject newGeoTrigger = newGeoTriggers.getJSONObject(k);
									if (!geoTriggerExists(currGeoTriggers, newGeoTrigger)) {
										Log.d(TAG, "adding new geoTrigger: " + newGeoTrigger.optString("_id") + " to geoNotification: " + currSurveyGeoNotification.optString("survey_id"));
										currGeoTriggers.put(newGeoTrigger);
									}
								}
								break;
							}
						}
					} 
				}
			}
			catch (JSONException e) {
			Log.e(TAG, "JSONExc: " + e);
			}
		}
	}

	  private boolean surveyGeoNotificationExists(
			  JSONObject currNewGeoNotificaiton) throws JSONException {
		  for (int i = 0; i < surveyGeoNotifications.length(); i++) {
			  JSONObject currGeoNotification = surveyGeoNotifications.getJSONObject(i);
			  if (currGeoNotification.getString("survey_id").equals(currNewGeoNotificaiton.getString("_id"))) {
				  return true;
			  }
		  }
		  return false;
	  }

	public boolean geoTriggerExists(JSONArray currJSONObjGeoTriggers,
			JSONObject newGeoTrigger) throws JSONException {
		for (int i = 0; i < currJSONObjGeoTriggers.length(); i++) {
			JSONObject currGeoTrigger = currJSONObjGeoTriggers.getJSONObject(i);
			if (newGeoTrigger.optString("_id").equals(currGeoTrigger.opt("_id"))) {
				return true;
			}
		}
		return false;
	}

	@Override
	  public IBinder onBind(Intent intent) {
	      // We don't provide binding, so return null
	      return null;
	  }
	  
	  @Override
	  public void onDestroy() {
		  if (locationTracker != null) {
			  locationTracker.stopTracking();
		  }
		  IS_RUNNING = false;
		  Log.d(TAG, "Location service destroyed"); 
	  }
	}
