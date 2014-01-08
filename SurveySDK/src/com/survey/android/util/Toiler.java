package com.survey.android.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.survey.android.R;
import com.survey.android.c2dm.C2DMTokenRefresher;
import com.survey.android.geofence.GeofenceRemover;
import com.survey.android.geofence.GeofenceRequester;
import com.survey.android.geofence.SimpleGeofence;
import com.survey.android.model.QuestionModel;
import com.survey.android.webclient.RestClient;
//import com.survey.surveydata.ConstantData;

public class Toiler {

	private static final String TAG = "Toiler";
   	private static JSONArray geoSurveys;
   	private static Date LastPollGeoSurveyDate;   
   	private static long LastPollGeoSurveyTimeMillis;
   	private static Location LastPollGeoSurveyLocation;
    // Store a list of geofences to add
   	private static List<Geofence> currentGeofences;
    private static GeofenceRequester GeofenceRequester;
    // Remove geofences handler
    private static GeofenceRemover GeofenceRemover;    
    // Store the list of geofences to remove
    private static List<String> GeofenceIdsToRemove;    
   	
	/**
	 * Checks if value is valid based on question type
	 * 
	 * @param value
	 *            String
	 * @param question
	 *            String
	 * @return boolean - is valid (true) or not valid (false)
	 */
	public static boolean isValidText(String value, QuestionModel question) {
		boolean result = true;
		try {
			String responseType = question.getResponseType();
			if (value == null || value.equals("")) {
				result = false;
			} else if (responseType
					.equals(ConstantData.RESPONSE_TYPE_NUMERIC_DECIMAL)
					|| responseType
							.equals(ConstantData.RESPONSE_TYPE_NUMERIC_INTEGER)) {
				double min = question.getMin();
				double max = question.getMax();
				double val = Double.parseDouble(value);

				if ((max != 0 && (val < min || val > max))
						|| (max == 0 && val < min))
					result = false;

			}
		} catch (Exception exc) {
			result = false;
		}
		return result;
	}
	  
	 /**
	   * Store image and get uri.
	   * 
	   * @param bitmap
	   *            the bitmap
	   * @param fileName
	   *            the file name of stored image
	   * @return uri, or null if some problem occurred
	   */
	  @SuppressWarnings("deprecation")
	public static Uri storeImageAndGetUri(Bitmap bitmap, String fileName, Context context) {

		File newFile = null;
		// We can only read the media
		File appImagesFolder;
		Log.d(TAG, "Created image on internal storage");
		    appImagesFolder = context.getDir(
		        "capturedPhotos", Context.MODE_WORLD_WRITEABLE);
		newFile = new File(appImagesFolder.getAbsolutePath(), fileName);

		Uri uri = Uri.fromFile(newFile);
		Log.d(TAG, "uri: " + uri);
		return uri;
	  }
	  
	public static void refreshAndScheduleNotificationToken(Context context) {
		
		if (checkPlayServices(context)) {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context.getApplicationContext());
			String registrationId = prefs.getString(
					"registration_id", null);
			Log.d(TAG, "registration_id: " + registrationId);
			
			//com.survey.android.session.Configuration 
    		//configuration = new com.survey.android.session.Configuration(context);
			
			Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
			registrationIntent.putExtra("app", PendingIntent.getBroadcast(context, 0, new Intent(), 0));
			registrationIntent.putExtra("sender",com.survey.android.session.Configuration.getGcmProjectId());
			context.startService(registrationIntent);
			
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.HOUR, 24);
			Intent intent = new Intent(context, C2DMTokenRefresher.class);
			PendingIntent sender = PendingIntent.getBroadcast(context, ConstantData.PENDINGINTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	
			// Get the AlarmManager service
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
		}
	}

	 /**
	   * Register with GCM using deprecated library in case Google Play Services is not available.
	   * 
	   * @param context
	   *            the Context
	   * @return void
	   */	
	public static void registerWithGCMUsingOldLib(Context context) {
    	try {
    		//com.survey.android.session.Configuration 
    		//configuration = new com.survey.android.session.Configuration(context);
            GCMRegistrar.checkDevice(context);
            GCMRegistrar.checkManifest(context);
            GCMRegistrar.register(context, com.survey.android.session.Configuration.getGcmProjectId());
    	} catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }			
	}
	
	/**
	 * Based on question type create string used like hint for TextView
	 * 
	 * @param question
	 * @return String - created hint
	 */
	public static String getHintForNumbers(QuestionModel question) {
		String result = "";
		try {
			double min = question.getMin();
			double max = question.getMax();
			if (question.getResponseType().equals(
					ConstantData.RESPONSE_TYPE_NUMERIC_DECIMAL)) {

				result += "from " + min + " to "
						+ (max == 0 ? "infinity" : max);
			} else if (question.getResponseType().equals(
					ConstantData.RESPONSE_TYPE_NUMERIC_INTEGER)) {
				int mini = (int) min;
				int maxi = (int) max;
				result += "From " + mini + " to "
						+ (maxi == 0 ? "Infinity" : maxi);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Creates string which is used for name for multimedia file uploaded on
	 * Amazon
	 * 
	 * @param responseType
	 * @param token
	 * @param surveyId
	 * @param responseId
	 * @param time
	 * @param extension
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static String validNameS3(String responseType, String token,
			String surveyId, String responseId, String time, String extension)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		StringBuilder b = new StringBuilder();
		b.append(token);
		b.append(surveyId);
		b.append(responseId);
		b.append(time);
		return /*
				 * (responseType.equals(ConstantData.RESPONSE_TYPE_PHOTO)?
				 * "uploads/photos/":"uploads/videos/")+
				 */SHA1(b.toString()) + "." + extension;
	}

	public static boolean isServiceRunning(Context context, String serviceName) {
		Log.i(TAG, "checking if " + serviceName + " service running");
	    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceName.equals(service.service.getClassName())) {
	        	  Log.d(TAG, "returning true");
	            return true;
	        }
	    }
	    Log.d(TAG, "returning false");
	    return false;
	}
	
	private static String SHA1(String text) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		MessageDigest md;
		md = MessageDigest.getInstance("SHA-1");
		byte[] sha1hash = new byte[40];
		md.update(text.getBytes("iso-8859-1"), 0, text.length());
		sha1hash = md.digest();
		return convertToHex(sha1hash);
	}

	/**
	 * Converts array of bytes to String of hex digits (characters)
	 * 
	 * @param data
	 * @return
	 */
	private static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	/**
	 * Checks if internet is available at the moment
	 * 
	 * @param context
	 *            - Activity
	 * @return boolean - is available (true) or not available (false)
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return false;
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Checks if email address is valid or not based on hardcoded regex inside
	 * 
	 * @param emailAddress
	 *            - String
	 * @return boolean - is valid (true) or invalid (false)
	 */
	public static boolean isValidEmail(String emailAddress) {

		if (emailAddress == null)
			return false;
		String expression = "^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		/* /^\w+(?:\.\w+)*@\w+(?:\.\w+)+$/ */// less rigorous
		CharSequence inputStr = emailAddress;
		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		return matcher.matches();
	}

	/**
	 * Checks if email address is valid or not (just does it contain "@" or not)
	 * 
	 * @param emailAddress
	 * @return
	 */
	public static boolean isValidEmailLessRigorous(String emailAddress) {
		if (emailAddress == null)
			return false;
		return emailAddress.contains("@");
	}

	/**
	 * Checks if user is older than 18 years
	 * 
	 * @param year
	 *            - year of birth
	 * @param month
	 *            - month of birth
	 * @param day
	 *            - day of birth
	 * @return
	 */
	public static boolean isOlderThan(int year, int month, int day) {

		boolean result = true;
		try {
			Calendar myBirthDate = Calendar.getInstance();
			myBirthDate.clear();
			myBirthDate.set(year, month, day);
			Calendar now = Calendar.getInstance();
			Calendar clone = (Calendar) myBirthDate.clone(); // Otherwise
																// changes are
																// been
																// reflected.
			int years = -1;
			while (!clone.after(now)) {
				clone.add(Calendar.YEAR, 1);
				years++;
			}
			System.out.println(years); // 32
			result = years >= 18;
		} catch (Exception e) {
			result = true;
		}
		return result;

		// Calendar today = Calendar.getInstance();
		// today.add(Calendar.DAY_OF_MONTH, -day);
		// today.add(Calendar.MONTH, -month);
		// today.add(Calendar.YEAR,-year);
		// int a=today.get(Calendar.YEAR);
		// return today.get(Calendar.YEAR)>18;
	}

	/**
	 * Shows and changes message inside toast, no stack messages
	 * 
	 * @param context
	 * @param toast
	 * @param s
	 */
	public static void showToast(Context context, Toast toast, String s) {
		if (toast == null) {
			toast = Toast.makeText(context, s, Toast.LENGTH_SHORT);
		} else {
			toast.setText(s);
		}
		toast.show();
	}
	
	/** Shows progress bar at the top right corner of activity view
	 * @param activity
	 */
	public static void showTitleProgressBar(Activity activity) {
		Log.d(TAG, "Show title progress bar for activity: " + activity.toString());
		Activity parentActivity = activity.getParent();
		if (parentActivity != null) {
			parentActivity.setProgressBarIndeterminateVisibility(true);
		} else {
			activity.setProgressBarIndeterminateVisibility(true);
		}
	}
	
	/** Hide progress bar at the top right corner of activity view
	 * @param activity
	 */
	public static void hideTitleProgressBar(Activity activity) {
		Log.d(TAG, "Hide title progress bar for activity: " + activity.toString());
		Activity parentActivity = activity.getParent();
		if (parentActivity != null) {
			parentActivity.setProgressBarIndeterminateVisibility(false);
		} else {
			activity.setProgressBarIndeterminateVisibility(false);
		}
	}

	/**
	 * 
	 * @param context
	 * @param message
	 */
	public static void customToast(final Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(context.getResources().getString(R.string.retry_in))
				.setCancelable(false);
		final AlertDialog alert = builder.create();
		alert.show();
		CountDownTimer counterTemp = new CountDownTimer(
				ConstantData.FIVE_SECONDS, ConstantData.SECOND) {
			public void onTick(long millisUntilFinished) {
				alert.setMessage(context.getResources().getString(
						R.string.retry_in)
						+ " "
						+ millisUntilFinished
						/ 1000
						+ " "
						+ context.getResources().getString(R.string.seconds));
			}

			public void onFinish() {
				if (alert.isShowing()) {
					alert.dismiss();
				}
			}
		};
		counterTemp.start();
	}

	public static void switchLanguage(Activity activity, Locale newLocale) {
    	Locale.setDefault(newLocale);
    	Configuration appConfig = activity.getBaseContext().getResources().getConfiguration();
    	appConfig.locale = newLocale;
    	activity.getBaseContext().getResources().updateConfiguration(appConfig, activity.getBaseContext().getResources().getDisplayMetrics());
	}

    /**
     * Pull down geosurveys and create Geofences using configured number of geotriggers.
     * This is called periodically by GeoSurveyPollService and the Geofences are fully refreshed.
     *
     * @return void
     */	
	public static void retrieveAndRunGeoSurveys(Context context, String userToken, boolean forceRefresh) {
	   	
		//com.survey.android.session.Configuration 
	   	//configuration = new com.survey.android.session.Configuration(context);
		
		if (com.survey.android.session.Configuration.isGeoPushEnabled()) {
			
			List<GeoTrigger> geotriggers = new ArrayList<GeoTrigger>();
		   	
		   	// check if google play services IS available on device, otherwise do not
		   	// run code to register geofences
		   	if (checkPlayServices(context)) {
		   		
				// If geofence list is not initialized, then do it
				if (currentGeofences == null) {	   		
					currentGeofences = new ArrayList<Geofence>();
				}
		        
				// If geofence removal list is not initialized, then do it
				if (GeofenceIdsToRemove == null) {	   		
					GeofenceIdsToRemove = new ArrayList<String>();
				}
				
		        // Instantiate a Geofence requester
				if (GeofenceRequester == null) {	  
					GeofenceRequester = new GeofenceRequester(context);
				}
		        
		        // Instantiate a Geofence remover
				if (GeofenceRequester == null) {	 			
					GeofenceRemover = new GeofenceRemover(context);
				}	        
		        
				LastPollGeoSurveyLocation = getLastBestLocation(context);
				if (LastPollGeoSurveyLocation != null) {
				    Log.d(TAG, "LastPollGeoSurveyLocation: (" + LastPollGeoSurveyLocation.getLatitude() + ", " + LastPollGeoSurveyLocation.getLongitude() + ")");
				    
					try {
						// Check if GeoSurveys have been polled and set LastPollGeoSurveyDate
						if (LastPollGeoSurveyDate == null) {
							LastPollGeoSurveyDate = new Date();
							LastPollGeoSurveyTimeMillis = System.currentTimeMillis();
						}
						
						// If GeoSurveys were polled more than 10 mins ago or never polled, then poll
						long PollGap = System.currentTimeMillis()-LastPollGeoSurveyTimeMillis;
						Log.d(TAG, "GeoSurveys PollGap: " + PollGap);			
						if (PollGap > ConstantData.GEO_POLL_CHECK_INTERVAL || 
							geoSurveys == null || 
							forceRefresh == true)
						{
							LastPollGeoSurveyDate = new Date();
							LastPollGeoSurveyTimeMillis = System.currentTimeMillis();					
							geoSurveys = RestClient.pollGeoSurveys(userToken, LastPollGeoSurveyLocation.getLatitude(), LastPollGeoSurveyLocation.getLongitude());
							Log.d(TAG, "GeoSurveys polled on: " + LastPollGeoSurveyDate);					
						}
						
						if (geoSurveys != null)				    
						{	
							Log.d(TAG, "Refreshing Geofences on: " + LastPollGeoSurveyDate);
							
							// Remove previous Geofences -> Refresh Geofences
							if (!GeofenceIdsToRemove.isEmpty()) {
								removeGeofences(context);
							}
							
							Log.d(TAG, "geoSurveys: " + geoSurveys.toString());
							for (int i = 0; i < geoSurveys.length(); i++) {
							    JSONObject row = geoSurveys.getJSONObject(i);
							    String survey_id = row.getString("_id");
							    String title = row.getString("title");
							    Log.d(TAG, "GeoSurvey with ID: " + survey_id + " And title: " + title);
							    JSONArray triggers  = row.getJSONArray("geo_triggers");
							    Log.d(TAG, "Triggers: " + triggers.toString());
							    
							    for (int l = 0; l < triggers.length(); l++) {
							    	JSONObject trigger = triggers.getJSONObject(l);
							    	double latitude = trigger.getDouble("latitude");
							    	double longitude = trigger.getDouble("longitude");
							    	Integer radius = trigger.getInt("radius");
							    	String geo_trigger_id = trigger.getString("_id");
							    	float distance = distanceFromCurrent(latitude, longitude, context);
							    	GeoTrigger geotrigger = new GeoTrigger(latitude, longitude, radius, survey_id, title, geo_trigger_id, distance);
							    	geotriggers.add(geotrigger);
							    }
							}
							
							// Sort the list in descending order by distance
							Collections.sort(geotriggers);
							
							int k = 0;
						    for (GeoTrigger g : geotriggers) {
						    	// exit loop when predefined Max limit reached
						    	if (k >= ConstantData.MAX_NUM_GEOTRIGGERS) 
						    		break;
						    	
						    	addGeofence(g.getLatitude(), g.getLongitude(), g.getRadius(), context, g.getGeoTriggerId(), userToken);
						    	k++;
						    }
						    
					        // Start the request. Fail if there's already a request in progress
					        try {
					            // Try to add geofences
					        	if (!currentGeofences.isEmpty()) {
						        	Log.d(TAG, "Geotriggers found, registering geofences");				        		
					        		if (!GeofenceRequester.getInProgressFlag()) {
						        		GeofenceRequester.addGeofences(currentGeofences);
							        	Log.d(TAG, "geofences successfully submitted");					        		
					        		} else {
					        			Log.d(TAG, context.getString(R.string.add_geofences_already_requested_error));				        		
					        		}
					        	}
					        } catch (UnsupportedOperationException e) {
					            // Notify user that previous request hasn't finished.
					        	Log.e(TAG, context.getString(R.string.add_geofences_already_requested_error));
					        }					    
						}
					} catch (JSONException e) {
						Log.e(TAG, "JSONExc: " + e);
					} catch (IOException e) {
						Log.e(TAG, "IOException: " + e);
					}
				}
		   	}
	   	} else {
	   		Log.i(TAG, "Geopushes are not Enabled");
	   	}
	
	}
	
	private static void addGeofence(double latitude, double longitude, Integer radius, Context context, String geo_trigger_id, String userToken) {
	    Log.d(TAG, "Adding GeoTriggerID: " + geo_trigger_id + " to Geofence");
	    Log.d(TAG, "Location: (" + latitude + ", " + longitude + ", " + radius + ")");
	    try {
	    	// add geofence for entry and exit transitions
	    	SimpleGeofence objGeofence = new SimpleGeofence(
	    		geo_trigger_id,
                latitude,
                longitude,
                Float.valueOf(radius),
                ConstantData.GEOFENCE_EXPIRATION_IN_MILLISECONDS,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
	    	
	    	if (!currentGeofences.contains(objGeofence)) {
	    		currentGeofences.add(objGeofence.toGeofence());
	    	}
	    	
        	if (!GeofenceIdsToRemove.contains(objGeofence.getId())) {
        		GeofenceIdsToRemove.add(objGeofence.getId());
        	}
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            Log.e(TAG, context.getString(R.string.remove_geofences_already_requested_error));
        }
	}
	
	private static void removeGeofences(Context context) {
	    Log.d(TAG, "Removing Geofences");	
	    
        try {
        	if (GeofenceRemover != null && GeofenceIdsToRemove != null && !GeofenceIdsToRemove.isEmpty()) {
        		GeofenceRemover.removeGeofencesById(GeofenceIdsToRemove);
        	}
        	
        	// clear all previous geofences from the list
        	if (currentGeofences != null && !currentGeofences.isEmpty()) {
        		currentGeofences.clear();
        	}
        	
        	// clear all previous geofence Ids from the list
        	if (GeofenceIdsToRemove != null && !GeofenceIdsToRemove.isEmpty()) {
        		GeofenceIdsToRemove.clear();
        	}        	
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	private static float distanceFromCurrent(double latitude, double longitude, Context context) {
		Location geotriggerLocation = new Location("point A");
		geotriggerLocation.setLatitude(latitude);
		geotriggerLocation.setLongitude(longitude);
		Location currentLocation = getLastBestLocation(context);
		float distance = geotriggerLocation.distanceTo(currentLocation);
		return distance;
	}
	
	public static Location getLastBestLocation(Context context) {
    	Location bestResult = null;
    	float bestAccuracy = Float.MAX_VALUE;
    	long bestTime = Long.MIN_VALUE;
    	long minTime = System.currentTimeMillis()-AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    		
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);		
		List<String> matchingProviders = locationManager.getAllProviders();
		for (String provider: matchingProviders) {
		  Location location = locationManager.getLastKnownLocation(provider);
		  if (location != null) {
		    float accuracy = location.getAccuracy();
		    long time = location.getTime();
		        
		    if ((time > minTime && accuracy < bestAccuracy)) {
		      bestResult = location;
		      bestAccuracy = accuracy;
		      bestTime = time;
		    }
		    else if (time < minTime && 
		             bestAccuracy == Float.MAX_VALUE && time > bestTime) {
		      bestResult = location;
		      bestTime = time;
		    }
		  }
		}	
			
		return bestResult;
	}

    @SuppressWarnings("unused")
	private static JSONArray parseGeoSurveysResult(JSONArray geoSurveys) {
		JSONArray geoSurveyNotifications = new JSONArray();
		try {
			for (int i = 0; i <geoSurveys.length(); i++) {
				JSONObject surveyJSON = geoSurveys.getJSONObject(i);
				JSONObject geoSurveyNotification = new JSONObject();
				geoSurveyNotification.put("survey_id", surveyJSON.optString("_id"));
				geoSurveyNotification.put("geo_triggers", surveyJSON.optJSONArray("geo_triggers"));
				geoSurveyNotifications.put(geoSurveyNotification);
			}
		} catch (JSONException exc) {
			Log.e(TAG, "JSONexc: " + exc);
		}
		return geoSurveyNotifications;
	}

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private static boolean checkPlayServices(Context ctx) {
    	boolean ret = true;
    	
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(ctx);
        
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		Editor editor = prefs.edit();
		
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d(TAG, "google play services IS available");   
  			editor.putBoolean("google_play_services_available", true);
  			ret = true;
  			
        // Google Play services was not available for some reason
        } else {
            Log.d(TAG, "google play services NOT available");            
  			editor.putBoolean("google_play_services_available", false);
  			ret = false;
        }
        
		editor.commit();  
		return ret;
    }
    
    /**
    * Used to encrypt sensitive data with pre-configured symmetric key
    * @param text
    * @return String
    */
    public static String Symmetric_Encrypt(String text) {
    	try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] keyBytes = new byte[16];
            String key = ConstantData.PUBLIC_KEY;
            byte[] b = key.getBytes("UTF-8");
            int len = b.length;
            if (len > keyBytes.length) len = keyBytes.length;
            System.arraycopy(b, 0, keyBytes, 0, len);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            byte[] results = cipher.doFinal(text.getBytes("UTF-8"));
            String result = Base64.encodeBytes(results);
            return result;    		
    	} catch (Exception e) {
    		return "";
    	}
    }
    
    /**
    * Used to encrypt sensitive data with specified symmetric key
    * @param key
    * @param text
    * @return String
    */
    public static String Encrypt(String key, String text) {
    	try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] keyBytes = new byte[16];
            
            byte[] b = key.getBytes("UTF-8");
            int len = b.length;
            if (len > keyBytes.length) len = keyBytes.length;
            System.arraycopy(b, 0, keyBytes, 0, len);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            byte[] results = cipher.doFinal(text.getBytes("UTF-8"));
            String result = Base64.encodeBytes(results);
            return result;    		
    	} catch (Exception e) {
    		return "";
    	}
    }    

    /**
    * Used to decrypt sensitive data with pre-configured symmetric key
    * @param text
    * @return String
    */    
    public static String Symmetric_Decrypt(String text) {
    	try {
        	byte[] encrypted = Base64.decode(text);
        	byte[] pwdBytes = ConstantData.PUBLIC_KEY.getBytes("UTF-8");
        	byte[] keyBytes = new byte[16];
        	int len = pwdBytes.length;
        	if (len > keyBytes.length) {
        		len = keyBytes.length;
        	}
        	System.arraycopy(pwdBytes, 0, keyBytes, 0, len);
        	SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        	Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        	IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
        	cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        	
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, "UTF-8");    		
    	} catch (Exception e) {
    		return "";
    	}
    }
    
    /**
    * Used to decrypt sensitive data with calculated key
    * @param text
    * @return String
    */
    public static String Keyphrase_Decrypt(String text) {
    	try {
            String tempkey = 
            		com.survey.surveydata.SecureData.PHRASE + 
            		com.survey.surveydata.SecureData.salt(ConstantData.SALT);
        	byte[] encrypted = Base64.decode(text);
        	byte[] pwdBytes = tempkey.getBytes("UTF-8");
        	byte[] keyBytes = new byte[16];
        	int len = pwdBytes.length;
        	if (len > keyBytes.length) {
        		len = keyBytes.length;
        	}
        	System.arraycopy(pwdBytes, 0, keyBytes, 0, len);
        	SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        	Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        	IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
        	cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        	
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, "UTF-8");    		
    	} catch (Exception e) {
    		return "";
    	}
    }     
    
}
