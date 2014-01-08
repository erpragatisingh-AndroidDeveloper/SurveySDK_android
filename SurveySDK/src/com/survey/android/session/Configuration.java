package com.survey.android.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import com.survey.android.model.Prefs;
import com.survey.android.util.ConstantData;
import com.survey.android.util.Log;

public class Configuration {
	private static final String TAG = "Configuration";
	//Key names
	public static final String ORGANIZATION_ID="organizationId";
	public static final String SURVEY_ENVIRONMENT="surveyEnvironment";
	public static final String EXIT_OPTION="exitOption";
	public static final String ENABLE_DASHBOARD="enableDashBoard";
	public static final String SHOW_LOGOUT_BUTTON="showLogoutButton";
	public static final String CUSTOM_AS3="customAS3";
	public static final String AS3_ACCESS_KEY_ID="as3AccessKeyID";
	public static final String AS3_SECRET_KEY="as3SecretKey";
	public static final String AS3_BUCKET="as3Bucket";
	public static final String GCM_ENABLE="gcmEnable";
	public static final String GCM_PROJECT_ID="gcmProjectID";
	public static final String GCM_NOTIFICATIONS_ENABLE="gcmNotificationsEnable";
	public static final String GEOPUSHES_ENABLE="geoPushesEnable";
	
	//Key values
	public static final String ENV_DEV="dev";
	public static final String ENV_PROD="prod";	
	public static final String LOGOUT="logout";
	public static final String GO_BACK_TO_SURVEY="BackToSurvey";
	public static final String ENABLE="enable";
	public static final String DISABLE="disable";
	
	public enum SurveyEnvironmentOption{DEV,PROD};	
	public enum ExitOption{LOGOUT,GO_BACK_TO_SURVEY};
	public enum ShowPoints{TRUE,FALSE};
	public enum ShowLogoutButton{TRUE,FALSE};
	public enum CustomAS3{TRUE,FALSE};
	public enum EnableGCM{TRUE,FALSE};
	public enum EnableGCMNotifications{TRUE,FALSE};
	public enum EnableGeoPushes{TRUE,FALSE};
	
	public static Context context;
	
	public Configuration(Context context) {
		Configuration.context = context;
	}

	public void init(
			String organization_id,
			SurveyEnvironmentOption survey_environment,
			boolean display_points,
			boolean display_logout_button,
			String on_submission,
			boolean aws_s3_default_configuration,
			String access_key_id,
			String secret_key,
			String bucket,
			boolean enable_gcm,
			boolean enable_push_notifications,
			boolean enable_geopushes,
			String google_gcm_project_id) {
		
		Log.i(TAG, "SDK Configuration settings: ");
		Log.i(TAG, "organization_id: " + organization_id);
		Log.i(TAG, "survey_environment: " + survey_environment);
		Log.i(TAG, "display_points: " + display_points);
		Log.i(TAG, "display_logout_button: " + display_logout_button);
		Log.i(TAG, "on_submission: " + on_submission);
		Log.i(TAG, "aws_s3_default_configuration: " + aws_s3_default_configuration);
		Log.i(TAG, "access_key_id: " + access_key_id);
		Log.i(TAG, "secret_key: " + secret_key);
		Log.i(TAG, "bucket: " + bucket);
		Log.i(TAG, "enable_gcm: " + enable_gcm);	
		Log.i(TAG, "enable_push_notifications: " + enable_push_notifications);
		Log.i(TAG, "enable_geopushes: " + enable_geopushes);
		Log.i(TAG, "google_gcm_project_id: " + google_gcm_project_id);			
		
		// Set Organization used by App
		Configuration.configureOrganizationId(organization_id);
		
		// Set environment
		Configuration.configureSurveyEnvironment(survey_environment);
		
		// Display POINTS option
		if (display_points == true) {
			Configuration.configureDashBoard(ShowPoints.TRUE);
		} else {
			Configuration.configureDashBoard(ShowPoints.FALSE);
		}

		// Display LOGOUT button option on Dashboard
		if (display_logout_button == true) {
			Configuration.showLogoutButtonInDashBoard(ShowLogoutButton.TRUE);
		} else {
			Configuration.showLogoutButtonInDashBoard(ShowLogoutButton.FALSE);
		}
		
		// Behavior option of Survey completion
		if(on_submission.contentEquals("BackToSurveyList")) {
			Configuration.configureFinalSubmission(ExitOption.GO_BACK_TO_SURVEY);
		} else if(on_submission.contentEquals("Logout")){
			Configuration.configureFinalSubmission(ExitOption.LOGOUT);
		}
		
		// Amazon S3 option for phone/audio/video uploads
		if(aws_s3_default_configuration == false &&
			access_key_id != null && access_key_id != "" &&
			secret_key != null && secret_key != "" &&
			bucket != null && bucket != "") {
			Configuration.configureAS3(CustomAS3.TRUE, access_key_id, secret_key, bucket);
		} else {
			Configuration.configureAS3(CustomAS3.FALSE, null, null, null);
		}

		// Initially disable GCM, notifications and geopushes
		Configuration.configureGCM(EnableGCM.FALSE, null);
		Configuration.configureGCMNotifications(EnableGCMNotifications.FALSE);
		Configuration.configureGeoPushes(EnableGeoPushes.FALSE);
		
		// Now set GCM options based on user SDK settings
		if (enable_gcm == true && 
			google_gcm_project_id != null && google_gcm_project_id != "") {
			Configuration.configureGCM(EnableGCM.TRUE, google_gcm_project_id);
			
			if (enable_push_notifications == true) {
				Configuration.configureGCMNotifications(EnableGCMNotifications.TRUE);
			} else {
				Configuration.configureGCMNotifications(EnableGCMNotifications.FALSE);
			}
			
			if (enable_geopushes == true) {
				Configuration.configureGeoPushes(EnableGeoPushes.TRUE);
			} else {
				Configuration.configureGeoPushes(EnableGeoPushes.FALSE);
			}			
			
		} 		
	}
	
	public static void configureOrganizationId(String organizationid) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		if (organizationid != null && organizationid != "") {
			editor.putString(ORGANIZATION_ID, organizationid);
			Prefs.setOrganizationId(organizationid);
		} else {
			Prefs.setOrganizationId(""); // Set to Default SDK Demo organization	
		}
		editor.commit();		
	}
	
	public static String getOrganizationId() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (prefs.getString(ORGANIZATION_ID, "") != "") {
			return prefs.getString(ORGANIZATION_ID, "");
		} else {
			return Prefs.ORGANIZATION_ID;
		}
	}
	
	public static void configureSurveyEnvironment(SurveyEnvironmentOption surveyEnvironmentOption) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		if (surveyEnvironmentOption == SurveyEnvironmentOption.PROD) {
			editor.putString(SURVEY_ENVIRONMENT, ENV_PROD);
			Prefs.setApiUrl(SurveyEnvironmentOption.PROD);
		} else {
			editor.putString(SURVEY_ENVIRONMENT, ENV_DEV);
			Prefs.setApiUrl(SurveyEnvironmentOption.DEV);			
		}
		editor.commit();		
	}
	
	public static SurveyEnvironmentOption getSurveyEnvironmentOption() {
		SurveyEnvironmentOption surveyEnvironmentOption = null;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String sOption = prefs.getString(SURVEY_ENVIRONMENT, ENV_DEV);
		if (sOption.equals(ENV_PROD)) {
			surveyEnvironmentOption = SurveyEnvironmentOption.PROD; 
		} else {
			surveyEnvironmentOption = SurveyEnvironmentOption.DEV;
		}
		Log.i("Configuration", surveyEnvironmentOption.toString());
		return surveyEnvironmentOption;
	}
	
	public static void configureFinalSubmission(ExitOption exitOption) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		if (ExitOption.LOGOUT == exitOption) {
			editor.putString(EXIT_OPTION,LOGOUT);
		} else if (ExitOption.GO_BACK_TO_SURVEY == exitOption) {
			editor.putString(EXIT_OPTION,GO_BACK_TO_SURVEY);
		}
		editor.commit();
	}
	
	public static ExitOption getExitOption() {
		ExitOption exitOption = null;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String sOption = prefs.getString(EXIT_OPTION, GO_BACK_TO_SURVEY);
		if (sOption.equals(GO_BACK_TO_SURVEY)) {
			exitOption = ExitOption.GO_BACK_TO_SURVEY; 
		} else if (sOption.equals(LOGOUT)) {
			exitOption = ExitOption.LOGOUT;
		}
		Log.i("Configuration", exitOption.toString());
		return exitOption;
	}
	
	public static void configureDashBoard(ShowPoints showPoints) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		if (ShowPoints.TRUE == showPoints) {
			editor.putString(ENABLE_DASHBOARD,ENABLE);
		} else if (ShowPoints.FALSE == showPoints){
			editor.putString(ENABLE_DASHBOARD,DISABLE);
		}
		editor.commit();
	}
	
	public static boolean isDashBoardAvailable() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String dashBoardAvailable = prefs.getString(ENABLE_DASHBOARD, ENABLE);
		return dashBoardAvailable.contentEquals(ENABLE);
	}
	
	public static void showLogoutButtonInDashBoard(ShowLogoutButton showLogoutButton) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		if (ShowLogoutButton.TRUE == showLogoutButton) {
			editor.putBoolean(SHOW_LOGOUT_BUTTON, true);
		} else {
			editor.putBoolean(SHOW_LOGOUT_BUTTON, false);
		}
		editor.commit();
	}
	
	public static boolean isShowLogoutButton() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(SHOW_LOGOUT_BUTTON, false);
	}
	
	public static void configureAS3(CustomAS3 customAS3, String as3AccesKeyID, String as3SecretKey, String as3Bucket) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		if (CustomAS3.TRUE == customAS3) {
			editor.putBoolean(CUSTOM_AS3,true);
			editor.putString(AS3_ACCESS_KEY_ID,as3AccesKeyID);
			editor.putString(AS3_SECRET_KEY,as3SecretKey);
			editor.putString(AS3_BUCKET,as3Bucket);
		} else {
			editor.putBoolean(CUSTOM_AS3,false);
		}
		editor.commit();
	}
	
	public static String getAs3AccesKeyId() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		if (prefs.getBoolean(CUSTOM_AS3, false)) {
			return prefs.getString(AS3_ACCESS_KEY_ID, null);
		} else {
			return ConstantData.ACCESS_KEY_ID;
		}
	}
	
	public static String getAs3SecretKey() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		if (prefs.getBoolean(CUSTOM_AS3, false)) {
			return prefs.getString(AS3_SECRET_KEY, null);
		} else {
			return ConstantData.SECRET_KEY;
		}
	}
	
	public static String getAs3Bucket() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		if (prefs.getBoolean(CUSTOM_AS3, false)) {
			return prefs.getString(AS3_BUCKET, null);
		} else {
			return ConstantData.BUCKET;
		}
	}
	
	public static String getPictureBucket() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		if (prefs.getBoolean(CUSTOM_AS3, false)) {
			String pictureBucket = prefs.getString(AS3_BUCKET, null);
			if(pictureBucket != null)
			return pictureBucket + ".s3";
			else
				return null;
		} else {
			return ConstantData.getPictureBucket();
		}
	}
	
	public static void configureGCM(EnableGCM enableGCM, String gcmProjectId) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		if (EnableGCM.TRUE == enableGCM) {
			editor.putBoolean(GCM_ENABLE, true);
			editor.putString(GCM_PROJECT_ID, gcmProjectId);
		} else {
			editor.putBoolean(GCM_ENABLE, false);
		}
		editor.commit();
	}
	
	public static String getGcmProjectId() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return  prefs.getString(GCM_PROJECT_ID, null);
	}
	
	public static boolean isGCMEnabled() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(GCM_ENABLE, false);
	}
	
	public static void configureGCMNotifications(EnableGCMNotifications enableGCMNotifications) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		if (isGCMEnabled()) {
			if (EnableGCMNotifications.TRUE == enableGCMNotifications) {
				editor.putBoolean(GCM_NOTIFICATIONS_ENABLE, true);
			} else {	
				editor.putBoolean(GCM_NOTIFICATIONS_ENABLE, false);
			}
		} else {
			Log.i(TAG, "GCM must be enabled first");
			editor.putBoolean(GCM_NOTIFICATIONS_ENABLE, false);
		}
		editor.commit();
	}
	
	public static boolean isGCMNotificationsEnabled() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(GCM_NOTIFICATIONS_ENABLE, false);
	}
	
	public static void configureGeoPushes(EnableGeoPushes enableGeoPushes) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		if (isGCMEnabled()) {
			if (EnableGeoPushes.TRUE == enableGeoPushes) {
				editor.putBoolean(GEOPUSHES_ENABLE, true);
			} else {
				editor.putBoolean(GEOPUSHES_ENABLE, false);
			}
		} else {
			Log.i(TAG, "GCM must be enabled first");
			editor.putBoolean(GEOPUSHES_ENABLE, false);
		}
		editor.commit();
	}
	
	public static boolean isGeoPushEnabled() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(GEOPUSHES_ENABLE, false);
	}
	
}
