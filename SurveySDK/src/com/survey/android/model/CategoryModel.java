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

public class CategoryModel {
	public static List<CategoryModel> allRemote(Context context)
		
			throws JSONException, IOException {
		
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String token=prefs.getString(context.getString(R.string.token),
				Prefs.TOKEN);
		return JSONArrayToCategories(RestClient.getCategories(token));
	}

	private static List<CategoryModel> JSONArrayToCategories(JSONArray ar)
			throws JSONException {
		List<CategoryModel> categories = new ArrayList<CategoryModel>();
		for (int i = 0; i < ar.length(); i++) {
			categories.add(new CategoryModel(ar.getJSONObject(i)));
		}
		return categories;
	}

	String id;

	String description;

	String name;

	public CategoryModel(JSONObject jsonObject) throws JSONException {
		id = jsonObject.getString("_id");
		description = jsonObject.getString("description");
		name = jsonObject.getString("name");
	}

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
