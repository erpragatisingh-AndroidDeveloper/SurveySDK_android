package com.survey.android.model;

import com.survey.android.session.Configuration;
import com.survey.android.session.Configuration.SurveyEnvironmentOption;
import com.survey.android.util.Toiler;

public class Prefs {
	public static final String PREFS_NAME = "PrefsFile";
	//public static final String BASIC_USER = "0Wn3r";
	public static final String BASIC_USER = Toiler.Keyphrase_Decrypt(com.survey.surveydata.SecureData.BASIC_USER);
	//public static final String BASIC_PASSWORD = "N5ph3r3";
	public static final String BASIC_PASSWORD = Toiler.Keyphrase_Decrypt(com.survey.surveydata.SecureData.BASIC_PASSWORD);
	//public static final String SERVER_URL_DEV = "https://surveyapistage.apphb.com";
	public static final String SERVER_URL_DEV = Toiler.Keyphrase_Decrypt(com.survey.surveydata.SecureData.SERVER_URL_DEV);
	//public static final String SERVER_URL_PROD = "https://api-v2.survey.com";
	public static final String SERVER_URL_PROD = Toiler.Keyphrase_Decrypt(com.survey.surveydata.SecureData.SERVER_URL_PROD);
	//public static final String API_VERSION = "/api/v1/";
	public static final String API_VERSION = Toiler.Keyphrase_Decrypt(com.survey.surveydata.SecureData.API_VERSION);
	//public static final String TOKEN = "kmXkFigNbhmnJ3RudBxh";
	public static final String TOKEN = Toiler.Keyphrase_Decrypt(com.survey.surveydata.SecureData.TOKEN);
	public static final String URL_TERMS_AND_CONDITIONS = "http://survey.com/mobile/toc";
	public static final String SUPPORT_EMAIL = "mobilesupport@survey.com";
	public static final String SURVEY_ORGANIZATION_ID = "4ead9d98dcb87a0001000003";
	public static final String PACKAGE = "com.survey.android";
	public static String API_URL; // Set via Configuration setters
	public static String ORGANIZATION_ID; // Set via Configuration setters	 
	
	public static void setApiUrl(Configuration.SurveyEnvironmentOption surveyEnvironmentOption) {
		if (surveyEnvironmentOption == SurveyEnvironmentOption.PROD) {
			API_URL = SERVER_URL_PROD + API_VERSION;
		} else {
			API_URL = SERVER_URL_DEV + API_VERSION;
		}
	}
	
	public static void setOrganizationId(String organizationid) {
		if (organizationid != null && organizationid != "") {
			ORGANIZATION_ID = organizationid;
		} else {
			ORGANIZATION_ID = "5294f5153966bd22881cffa0";
		}
	}	
	
	/**
	 * Default is <b>false</b>. Do not change manually.
	 * Set hide.money.elements to true in white.properties instead.
	 */
	public static final boolean HIDE_MONEY_ELEMENTS = false;
	/**
	 * Default is <b>false</b>. Do not change manually.
	 * Set hide.choose.language to true in white.properties instead.
	 */
	public static final boolean HIDE_CHOOSE_LANGUAGE = true;	
	/**
	 * Default is <b>color:white</b>. Do not change manually.
	 * Set dashboard.label.text.color in white.properties instead.
	 */	
	public static final String WV_DASHBOARD_TEXT_COLOR = "color:black";	
	/**
	 * Default is <b>United States</b>. Do not change manually.
	 * Set defaultcountry in white.properties instead.
	 */		
	public static final String DEFAULT_COUNTRY = "United States";	
}
