package com.survey.android.services;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class ReferrerCatcher extends BroadcastReceiver {

	 private static String referrer = "";

	 @Override
	 public void onReceive(Context context, Intent intent) {
	   referrer = "";
	   Bundle extras = intent.getExtras();
	   if(extras != null){
	      referrer = extras.getString("referrer");
	
	   }
	   SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor edit = prefs.edit();

		// final Resources resource = Resources.getSystem();
		// resource.getString(android.R.string.untitled);
		// edit.putString(resource.getString(R.string.c2dm_registration_id),
		// registrationId);
		Log.d("INSTALL TRACKER", "Setting Referrer " + referrer);
		edit.putString("referrer", referrer);
		edit.commit();
		//Map<String, String> params = Toolbox.getQueryMap(referrer);
	 }
	 
	 public void onReceive1(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			
		}
	 //...
}