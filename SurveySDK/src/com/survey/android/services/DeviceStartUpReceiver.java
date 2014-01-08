package com.survey.android.services;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * A receiver that register device start-up event and start-up our GeoSurveyPollService.
 * 
 * @author Milos Pesic
 * 
 */

public class DeviceStartUpReceiver extends BroadcastReceiver {

	private static final String TAG = "DeviceStartUpReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Received: " + intent.getAction());
		
		Intent serviceIntent = new Intent(context, GeoSurveyPollService.class);
		context.startService(serviceIntent);
	}

}
