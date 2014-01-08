package com.survey.android.view.themed;

import android.os.Bundle;

public abstract class MainThemed extends DashboardThemed {

	// private static final String TAG = "Main";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	protected abstract void customizeTheme();

}