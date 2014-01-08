package com.survey.androiddemo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.survey.android.view.Main;

public class MainActivity extends Main {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		boolean displayEarning = settings.getBoolean("display_earnings", false);

		Log.i("Display earnings preference", String.valueOf(displayEarning));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
