package com.survey.android.c2dm;

import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;

import com.survey.android.containers.AppContainer;
import com.survey.android.util.ConstantData;
import com.survey.android.util.Log;
import com.survey.android.util.WhiteLabel;
import com.survey.android.webclient.RestClient;

public class RegService extends IntentService {

	private static final String TAG = "RegService";

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public RegService(String name) {
		super(name);	
	}
	
	public RegService(){
		super("RegService");	
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String registration_id = intent
				.getStringExtra("registration_id");
		String token = intent.getStringExtra("token");
		String userId = intent.getStringExtra("user_id");

		try {
			JSONObject object = RestClient.sendC2DMRegistrationId(
					registration_id, AppContainer.device_type, AppContainer.device_id, 
					token, userId);
		} catch (Exception e) {
			if (ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY))
				Log.e(TAG, "Exc: " + e);
			else
				e.printStackTrace();
		}
	}
}
