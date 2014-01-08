package com.survey.android.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.survey.android.util.Log;
import com.survey.android.util.GeoPush;

public class DataBroadcastReceiver extends BroadcastReceiver {
	
	private static final String TAG = "DataBroadcastReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		boolean enter_exit = bundle.getBoolean("entering");
		String geo_trigger_id = bundle.getString("geo_trigger_id");
		Log.d(TAG, "Geo Triggered for " + geo_trigger_id);
		String userToken = bundle.getString("user_token");
		
		// check if entered GeoFence
		if (enter_exit == true && 
			userToken != null && userToken.length() > 0 && 
			geo_trigger_id != null && geo_trigger_id.length() > 0) {
			GeoPush.SendPush(userToken, geo_trigger_id,context);
		}
	}
}
