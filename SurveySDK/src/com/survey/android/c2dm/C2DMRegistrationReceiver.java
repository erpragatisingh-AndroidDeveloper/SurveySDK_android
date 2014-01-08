package com.survey.android.c2dm;

import java.io.IOException;

import org.json.JSONException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.survey.android.containers.AppContainer;
import com.survey.android.util.ConstantData;
import com.survey.android.util.Log;
import com.survey.android.util.WhiteLabel;
import com.survey.android.webclient.RestClient;

public class C2DMRegistrationReceiver extends BroadcastReceiver {

	private static final String TAG = "C2DMRegistrationReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.d(TAG, "onReceive action: " + action);
		
		if ("com.google.android.c2dm.intent.REGISTRATION".equals(action)) {

			final String registrationId = intent
					.getStringExtra("registration_id");	
			Log.d(TAG, "registration_id: " + registrationId);
			
			String error = intent.getStringExtra("error");
			String unregistered = intent.getStringExtra("unregistered");
			
			if (ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY)) {
				Log.d(TAG, "error: " + error);
				Log.d(TAG, "unregistered: " + unregistered);
			}

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			String token = prefs.getString("token", null);
			if (token != null && token.length() > 0) {
				String userId;
				
				try {
					userId = RestClient.getUserIdByToken(token).getString("user_id");
	
					Intent i = new Intent(context, RegService.class);
					i.putExtra("registration_id", registrationId);
					i.putExtra("token", token);
					i.putExtra("user_id", userId);
					i.putExtra("device_id", AppContainer.device_id);
					i.setAction(android.content.Intent.ACTION_VIEW);
					context.startService(i);
				} catch (JSONException e) {
					if (ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY))
						Log.e(TAG, "JSONexc: " + e);
					else
						e.printStackTrace();
				} catch (IOException e) {
					if (ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY))
						Log.e(TAG, "IOException: " + e);
					else
						e.printStackTrace();
				}
	
				saveRegistrationId(context, registrationId);
			}

		} else {
			Log.d("REG_3", "Unused Intent action");
		}
	}

	private void saveRegistrationId(Context context, String registrationId) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor edit = prefs.edit();
		Log.d(TAG, "saving registraion_id: " + registrationId);
		edit.putString("registration_id", registrationId);
		edit.commit();
	}
}