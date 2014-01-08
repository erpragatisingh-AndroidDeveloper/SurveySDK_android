package com.survey.android.containers;

import java.util.Locale;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.location.Location;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;


import com.survey.android.R;
import com.survey.android.util.ConstantData;
import com.survey.android.util.Log;
import com.survey.android.util.WhiteLabel;
import com.survey.android.webclient.SurveySSLSocketFactory;

public class AppContainer extends Application implements OnSharedPreferenceChangeListener {
	private static final String DEFAULT_UI_LANGUAGE = "en"; // not used in Survey
	
	private static final String TAG = "Application";
	private Locale locale = null;
	public Location location;
	public Long timeLocationFetced;
	
	/** current application context */
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
  
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
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
	
	

    public AppContainer(Context context) {
		super();
		AppContainer.context = context;
        SurveySSLSocketFactory.appcontext = context;
        init();
	}

	@Override
    public void onCreate()
    {
        super.onCreate();
        context = getApplicationContext();
        SurveySSLSocketFactory.appcontext = context;
        init();
    }
    
    private void init() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        
    	if (ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY)) {
    		
            Configuration config = context.getResources().getConfiguration();

            String lang = settings.getString("chosen_language", "");
            if (! "".equals(lang) && ! config.locale.getLanguage().equals(lang))
            {
                locale = new Locale(lang);
                Locale.setDefault(locale);
                config.locale = locale;
                context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
            }

    	} else {
    		
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
            		context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
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
	
}
