package com.survey.android.session;

import java.io.IOException;
import java.util.Locale;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.survey.android.R;
import com.survey.android.c2dm.GcmRegistrationService;
import com.survey.android.model.UserModel;
import com.survey.android.util.Log;
import com.survey.android.util.Toiler;
import com.survey.android.webclient.SurveySSLSocketFactory;

public class Session extends Application implements OnSharedPreferenceChangeListener{
	private static final String DEFAULT_UI_LANGUAGE = "en"; // not used in Survey
	private static final String TAG = "Session";
	private Locale locale = null;
	public static Context context;

	/** unique device id **/
	public static String device_id;
	public static String device_os_version;
	public static String device_type;
	
	/** user token accessible globally **/
	private static String userToken;
	public static String getUserToken() {
		if (userToken == null || userToken.length() == 0) {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			userToken = prefs.getString(context.getString(R.string.token), null);
		}
		return userToken;
	}

	public Session(Context context) {
		Session.context = context;
        SurveySSLSocketFactory.appcontext = context;
        init();
	}
	
//	//Pass email, firstname, lastname, organizationid and get back userid and retain it for the session
//	public String logIn(String email, 
//			String password, 
//			String firstName, 
//			String lastName, 
//			String organizationId){
//		String response="";
//        try {
//            UserModel user = UserModel.getInstance();
//			user.setEmail(email);
//			user.setPassword(password);
//			user.setName(firstName);
//			user.setLastName(lastName);
//			user.setOrganizationId(organizationId);				
//			user.setVersion(context.getString(R.string.v1));
//			user.signIn();
//			response = user.getUserId();
//			
//			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//			Editor editor = prefs.edit();
//			editor.putString(context.getString(R.string.token),user.getAuthenticationToken());
//			editor.putString(context.getString(R.string.user_id),user.getUserId());
//			try {
//				editor.putString(context.getString(R.string.email), user.getEmail());
//				editor.putString(context.getString(R.string.username),user.getName() + " " + user.getLastName());
//				editor.putString(context.getString(R.string.gender), user.getGender() == true 
//				? context.getString(R.string.male): context.getString(R.string.female));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			editor.commit();
//			
//			// Register GCM token
//			GcmRegistration(context);			
//		}catch (IOException e) {
//			Log.e(TAG, "IOException: " + e);
//			response = "Error " + e.toString();
//		} catch (final Exception e) {
//			Log.e(TAG, "Exception: " + e);
//			response= "Error "  + e.toString();
//		}
//        return response;
//	}
	
	//Pass email, firstname, lastname, organizationid and get back userid and retain it for the session
	/**
	 * Use email, firstname, lastname, organizationid to login or register. If user with email
	 * and organizationId is found, that suer is returned. Otherwise a new user account is
	 * created and the new user account is returned.
	 * 
	 * @param email 
	 * 			  String
	 * @param firstName
	 *            String
	 * @param lastName
	 *            String   
	 * @param organizationId
	 *            String 
	 * @return boolean - Logged In (true) or Not logged In (false)
	 */	
	public boolean logIn(String email, 
			String firstName, 
			String lastName,
			String organizationId) {
		boolean logIn = logInOrRegister(email, firstName, lastName, "", false, organizationId);
		return logIn;
	}
	
	/**
	 * Use email, firstname, lastname, organizationid to login or register. If user with email
	 * and organizationId is found, that suer is returned. Otherwise a new user account is
	 * created and the new user account is returned.
	 * 
	 * @param email 
	 * 			  String
	 * @param firstName
	 *            String
	 * @param lastName
	 *            String   
	 * @param birthDate
	 *            String   
	 * @param gender
	 *            boolean   
	 * @param organizationId
	 *            String 
	 * @return boolean - Logged In (true) or Not logged In (false)
	 */		
	public boolean logIn(String email, 
			String firstName, 
			String lastName,
			String birthDate, 
			boolean gender, 			
			String organizationId) {
		boolean logIn = logInOrRegister(email, firstName, lastName, birthDate, gender, organizationId);
		return logIn;
	}	
	
	/**
	 * Removes user token and other session variables from shared preferences
	 * 
	 * @return void 
	 */		
	public void logOut() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		editor.remove(context.getString(R.string.token));
		editor.remove("registration_id");
		editor.remove(context.getString(R.string.current_balance_stored));
		editor.remove(context.getString(R.string.career_earnings_stored));
		editor.remove(context.getString(R.string.surveys_available_stored));
		editor.remove(context.getString(R.string.surveys_completed_stored));
		// *************************************
		editor.remove(context.getString(R.string.user_id));
		editor.remove(context.getString(R.string.username));
		editor.remove(context.getString(R.string.gender));
		// *************************************
		boolean commit = editor.commit();
		Log.i(TAG, String.valueOf(commit));
	}
	
	/**
	 * Checks for user token and indicates if session is valid or not
	 * 
	 * @return boolean - Active (true) or Inactive (false)
	 */	
	public boolean isValidSession() {
		String token = getUserToken();
		if (token != null && token == "") {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Called by public methods to register a new user or authenticate an existing user.
	 */	
	private boolean logInOrRegister(String email, 
			String firstName, 
			String lastName, 
			String birthDate, 
			boolean gender, 
			String organizationId) {
		try {
			String passwordEncrypted = Toiler.Symmetric_Encrypt(email);
            UserModel user = UserModel.getInstance();
			user.setEmail(email);
			user.setPassword(passwordEncrypted);
			user.setName(firstName);
			user.setLastName(lastName);
			user.setBirthDate(birthDate);
			user.setGender(gender);
			user.setOrganizationId(organizationId);
			user.setDeviceId(device_id);
			user.setDeviceType(device_type);
			user.setDeviceOSVersion(device_os_version);
			user.signInOrRegister();

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			Editor editor = prefs.edit();
			editor.putString(context.getString(R.string.token),user.getAuthenticationToken());
			editor.putString(context.getString(R.string.user_id),user.getUserId());
			try {
				editor.putString(context.getString(R.string.email), user.getEmail());
				editor.putString(context.getString(R.string.username),user.getName() + " " + user.getLastName());
				editor.putString(context.getString(R.string.gender), user.getGender() == true 
				? context.getString(R.string.male): context.getString(R.string.female));
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			editor.commit();
			Log.i(TAG,"Survey authentication successful!");
			
			// Register GCM token
			GcmRegistration(context);
			
			return true;
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e);
			e.printStackTrace();			
			return false;
		} catch (final Exception e) {
			Log.e(TAG, "Exception: " + e);
			e.printStackTrace();
			return false;
		}
	}
	
    private void init() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        
        boolean firstTime = settings.getBoolean("first_time_opening_app", true);
        Log.d(TAG, "firstTime: " + firstTime);
    	Editor editor = settings.edit();
        
        if (!firstTime) {
        	editor.putBoolean("first_time_opening_app", false);
        	editor.commit();
        	
        	Configuration config = context.getResources().getConfiguration();
        	String lang = settings.getString("ui_language", "");
        	if (! "".equals(lang) && ! config.locale.getLanguage().equals(lang))
        	{
        		locale = new Locale(lang);
        		Locale loc = new Locale(lang);
        		Locale.setDefault(loc);
        		config.locale = loc;
        		context.getResources().updateConfiguration(config,context.getResources().getDisplayMetrics());
        	}
        } else {
        	editor.putBoolean("first_time_opening_app", false);
        	editor.putString("ui_language", DEFAULT_UI_LANGUAGE);
        	editor.putString("chosen_language", DEFAULT_UI_LANGUAGE);
        	editor.commit();
        	
        	Configuration config = context.getResources().getConfiguration();

        	String lang = DEFAULT_UI_LANGUAGE;
        	if (! "".equals(lang) && ! config.locale.getLanguage().equals(lang))
        	{        		
        		locale = new Locale(lang);
        		Locale.setDefault(locale);
        		config.locale = locale;
        		context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        	} 
        }

    	// set unique device_id to be used later
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        device_id = telephonyManager.getDeviceId();
        if (device_id == null)
        	device_id = "";
        
        // set android os version to be used in registration/profile
        device_os_version = android.os.Build.VERSION.RELEASE;      
        if (device_os_version == null)
        	device_os_version = "";
        
        // set device type to be used in registration/profile
        device_type = "android";
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig)
    {
		Log.d(TAG, "Application onConfigurationChange called, locale: " + locale + ", newConfigLocale: " + newConfig.locale.getLanguage());
        super.onConfigurationChanged(newConfig);
        if (locale != null) {
        	newConfig.locale = locale;
        	Locale.setDefault(locale);
        	getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
        }
    }
	
	public static void GcmRegistration(Context context) {
		// Register GCM token
		context.startService(new Intent(context, GcmRegistrationService.class));		
	}
}
