package com.survey.androiddemo;

import android.app.Application;
import android.util.Log;

public class AppContainer extends Application {
	
	private static final String TAG = "AppContainer";
	
	@Override
    public void onCreate()
    {
        super.onCreate();
        SDKConfigSettings.init(getApplicationContext());
        Log.i(TAG, "SDKConfigSettings.init() called");
    }
    
}

