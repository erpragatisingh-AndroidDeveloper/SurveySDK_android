package com.survey.android.model;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.survey.android.R;
import com.survey.android.webclient.RestClient;

public class ResponseModel {
	private static ResponseModel JSONObjectToResponse(JSONObject ob)
			throws JSONException {
		ResponseModel response = new ResponseModel(ob);
		return response;
	}

	public static ResponseModel remote(Context context, String objectId)
			throws JSONException, IOException {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String token=prefs.getString(context.getString(R.string.token),Prefs.TOKEN);
		
		return JSONObjectToResponse(RestClient.getResponses(token, objectId));
	}

	String id;

	String userId;

	String surveyId;

	public ResponseModel(JSONObject jsonObject) throws JSONException {
		id = jsonObject.getString("_id");
		userId = jsonObject.getString("user_id");
		surveyId = jsonObject.getString("survey_id");
	}

	public String getId() {
		return id;
	}

	public String getSurveyId() {
		return surveyId;
	}

	public String getUserId() {
		return userId;
	}

}
