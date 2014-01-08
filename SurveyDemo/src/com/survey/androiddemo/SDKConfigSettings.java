package com.survey.androiddemo;

import android.content.Context;

import com.survey.android.session.Configuration;
import com.survey.android.session.Configuration.SurveyEnvironmentOption;

public class SDKConfigSettings  {
	
	public static final String ORGANIZATION_ID = "5294f5153966bd22881cffa0";
	public static final SurveyEnvironmentOption SURVEY_ENVIRONMENT = SurveyEnvironmentOption.DEV;
	public static boolean DISPLAY_POINTS = false;
	public static boolean DISPLAY_LOGOUT_BUTTON = true;
	public static String ON_SUBMISSION = "BackToSurveyList"; // OR Logout
	
	public static boolean AWS_S3_DEFAULT_CONFIGURATION = true; 
	public static final String ACCESS_KEY_ID = "";
	public static final String SECRET_KEY = "";
	public static final String BUCKET = "";	
	
	public static boolean ENABLE_GCM = false;
	public static boolean ENABLE_PUSH_NOTIFICATIONS = false;
	public static boolean ENABLE_GEOPUSHES = false;
	public static final String GOOGLE_GCM_PROJECT_ID = "";
	
	public static Configuration configuration;
	
	public static void init(Context context) {
		configuration = new Configuration(context);
		configuration.init(
				ORGANIZATION_ID, 
				SURVEY_ENVIRONMENT, 
				DISPLAY_POINTS, 
				DISPLAY_LOGOUT_BUTTON, 
				ON_SUBMISSION, 
				AWS_S3_DEFAULT_CONFIGURATION, 
				ACCESS_KEY_ID, 
				SECRET_KEY, 
				BUCKET, 
				ENABLE_GCM, 
				ENABLE_PUSH_NOTIFICATIONS, 
				ENABLE_GEOPUSHES, 
				GOOGLE_GCM_PROJECT_ID);
	}
	
}
