package com.survey.android.model;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;

import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.survey.android.R;
import com.survey.android.webclient.RestClient;

@SuppressWarnings("serial")
public class CurrentSectionModel implements Serializable{

	// ********** holding data fetched from web *********
	private String _id;
	private String name;
	private String type;
	private String key;
	private String surveyId;
	private List<QuestionModel> questions;

	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return _id;
	}

	public String getName() {
		return name;
	}

	public List<QuestionModel> getQuestions() {
		return questions;
	}

	public String getSurveyId() {
		return surveyId;
	}

	// **************************************************

	// *********** helper functions *******************************
	// holding position of current question at the screen
	private int cursor = 0;

	public int getCursor() {
		return this.cursor;
	}

	public void setCursos(int cursor) {
		this.cursor = cursor;
	}

	public QuestionModel getCurrentQuestion() {
		return this.questions.get(this.cursor);
	}

	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	// *********** static methods for filling model **************************
	public static CurrentSectionModel remote(Context context, String objectId)
			throws JSONException, IOException {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String token = prefs.getString(context.getString(R.string.token),
				Prefs.TOKEN);
		String jsonCurrentSectionModel = RestClient.getCurrentSection(token,
				objectId);
		Type type = new TypeToken<CurrentSectionModel>() {
		}.getType();

		try {
			Gson gson = new Gson();
			return (CurrentSectionModel) gson.fromJson(jsonCurrentSectionModel,
					type);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new JSONException("Error in communication");
		}
	}
}