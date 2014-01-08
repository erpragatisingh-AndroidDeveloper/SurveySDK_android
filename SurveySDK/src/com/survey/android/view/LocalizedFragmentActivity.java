package com.survey.android.view;

import java.util.Locale;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;

import com.survey.android.util.ConstantData;
import com.survey.android.util.Log;
import com.survey.android.util.WhiteLabel;


public abstract class LocalizedFragmentActivity extends FragmentActivity {
	
	private static String TAG = "LocalizedFragmentActivity";
	protected Locale locale;
	
	protected void onCreate(Bundle instance, String tag) {
		super.onCreate(instance);
		
		if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY)) {
			TAG = tag;
			String currLocale = getBaseContext().getResources().getConfiguration().locale.getLanguage();
			Log.d(TAG, "onCreate uiLanguage: " + currLocale);
			if (locale != null && !currLocale.equals(locale)) {
				locale = new Locale(currLocale);
			} 
		}
	}
	
	protected void onCreate(Bundle instance) {
		super.onCreate(instance);
		
		if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY)) {
			String currLocale = getBaseContext().getResources().getConfiguration().locale.getLanguage();
			Log.d(TAG, "onCreate uiLanguage: " + currLocale);
			if (locale != null && !currLocale.equals(locale)) {
				locale = new Locale(currLocale);
			} 
		} else {
			SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(LocalizedFragmentActivity.this);		
			String currLocale = prefs.getString("ui_language", "en");
			locale = new Locale(currLocale);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(LocalizedFragmentActivity.this);
		
		if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY)) {
			String currLocale = prefs.getString("ui_language", "");
			Log.d(TAG, "onResume currLocale: " + currLocale + ", locale: " + locale);
			if (currLocale.equals("") || locale == null || !locale.getLanguage().equals(currLocale)) {
				
				initUI();
				locale = new Locale(currLocale);
			}
		} else {
			String currLocale = prefs.getString("ui_language", "en");
			Log.d(TAG, "onResume currLocale: " + currLocale + ", locale: " + locale.getLanguage());
			if (!locale.getLanguage().equals(currLocale)) {
				initUI();
				locale = new Locale(currLocale);
			}
		}
		
	}

	protected abstract void initUI();

}
