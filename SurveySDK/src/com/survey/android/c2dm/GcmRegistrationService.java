package com.survey.android.c2dm;

import java.io.IOException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.survey.android.R;
import com.survey.android.session.Configuration;
import com.survey.android.session.Session;
import com.survey.android.util.Log;
import com.survey.android.util.Toiler;
import com.survey.android.webclient.RestClient;

//import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class GcmRegistrationService extends IntentService {

	GoogleCloudMessaging gcm;	
	private String regid;
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
	private Context context;
	
    public GcmRegistrationService() {
        super("GcmRegistrationService");
        context = this;
    }
    public static final String TAG = "GcmRegistrationService";
    
	@Override
	protected void onHandleIntent(Intent intent) {
		if(Configuration.isGCMEnabled()){
			if (checkPlayServices()) {
				registerInBackground();
			}
		}
	}

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(String regId) {
        final SharedPreferences prefs = getGcmPreferences(this);
        Log.d(TAG, "Saving regId to preferences ");
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.commit();
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getBaseContext());
                    }
                    regid = gcm.register(Configuration.getGcmProjectId());
                    Log.d(TAG,"Device registered, registration ID=" + regid);
                    msg = "Device registered, registration ID=" + regid;

                    // Persist GCM regId to database
                    sendRegistrationIdToBackend();

                    // Persist GCM regId to preferences
                    storeRegistrationId(regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    Log.e(TAG, ex.getMessage());
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                //Android 2.2 gcm bug http://stackoverflow.com/questions/19269607/google-cloud-messaging-register-authentication-failed
            	if(msg.contains("AUTHENTICATION_FAILED")) {
                    Log.i(TAG,"Received AUTHENTICATION_FAILED from GCM, fallback to deprecated library");            		
                	try {
	                    //fall back to deprecated GCM client library
                		Toiler.registerWithGCMUsingOldLib(getBaseContext());
                	} catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                    }
                }
            }
        }.execute(null, null, null);
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     */
	private void sendRegistrationIdToBackend() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);   
		try {
			String registrationId = prefs.getString(PROPERTY_REG_ID, "");
			Log.d(TAG, "registrationId: " + registrationId);

			String userToken = prefs.getString(getString(R.string.token),"");
			Log.d(TAG, "userToken: " + userToken);
			
			String userId = prefs.getString(getString(R.string.user_id),"");
			Log.d(TAG, "userId: " + userId);			
			
			JSONObject object = RestClient.sendC2DMRegistrationId(
					registrationId, Session.device_type, Session.device_id, userToken, userId);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }	
	
}
