package com.survey.android.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.survey.android.R;
import com.survey.android.webclient.RestClient;

public class SurveyModel {
	public static List<SurveyModel> allRemote(Context context, String objectId)
			throws JSONException, IOException {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String token = prefs.getString(context.getString(R.string.token),
				Prefs.TOKEN);
		
		return JSONArrayToSurveys(RestClient.getSurveys(token, objectId));
	}

	private static List<SurveyModel> JSONArrayToSurveys(JSONArray ar)
			throws JSONException {
		List<SurveyModel> surveys = new ArrayList<SurveyModel>();
		for (int i = 0; i < ar.length(); i++) {
			surveys.add(new SurveyModel(ar.getJSONObject(i)));
		}
		return surveys;
	}

	QuestionModel[] questions;
	String id;

	String title;
	String description;
	int question_count;
	boolean is_msg;
	
	public SurveyModel() {
	}

	public SurveyModel(JSONObject jsonObject) throws JSONException {

		if (jsonObject.has("_id")) {
			id = jsonObject.getString("_id");
		}
		if (jsonObject.has("title")) {
			title = jsonObject.getString("title");
		}
		if (jsonObject.has("description")) {
			description = jsonObject.getString("description");
		}
		if (jsonObject.has("question_count")) {
			question_count = jsonObject.getInt("question_count");
		}
		if (jsonObject.has("is_msg")) {
			is_msg = jsonObject.getBoolean("is_msg");
		}
	}

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}

	public int getQuestionCount() {
		return this.question_count;
	}

	public void setQuestionCount(int question_count) {
		this.question_count = question_count;
	}

	public QuestionModel[] getQuestions() {
		return questions;
	}

	public String getTitle() {
		return title;
	}

	public boolean getIsMsg() {
		return is_msg;
	}
	
	public void setQuestions(QuestionModel[] questions) {
		this.questions = questions;
	}

}
