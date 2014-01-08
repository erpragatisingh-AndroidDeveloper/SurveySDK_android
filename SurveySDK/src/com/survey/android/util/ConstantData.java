package com.survey.android.util;

import android.os.Environment;
import android.text.format.DateUtils;

public class ConstantData {

    // *********************** white label constants *****************************************
	public static final WhiteLabel WHITE_LABEL_APP = WhiteLabel.SURVEY;
    // ****************************************************************************************    
    
	// *********************** question type constants ***************************************
	public static final Object RESPONSE_TYPE_AUDIO = "audio";
	public static final String RESPONSE_TYPE_FREE_TEXT = "free_text";
	public static final String RESPONSE_TYPE_OPEN_ENDED_TEXT = "open_ended_text";
	public static final String RESPONSE_TYPE_RATING_SCALE = "rating_scale";
	public static final String RESPONSE_TYPE_SINGLE_SELECT = "single_select";
	public static final String RESPONSE_TYPE_MULTIPLE_SELECT = "multiple_select";
	public static final String RESPONSE_TYPE_PHOTO = "photo";
	public static final String RESPONSE_TYPE_VIDEO = "video";
	public static final String RESPONSE_TYPE_NUMERIC_INTEGER = "integer";
	public static final String RESPONSE_TYPE_NUMERIC_DECIMAL = "decimal";
	public static final String RESPONSE_TYPE_READ_ONLY="readonly";
	public static final String RESPONSE_TYPE_RATING_SCALE_LABELED="rating_scale_with_labels";
	public static final String RESPONSE_TYPE_COMPLETED = "completed";
	public static final String RESPONSE_TYPE_FINISHED = "finished";
	public static final String RESPONSE_TYPE_DISQUALIFIED = "disqualified";
	public static final String RESPONSE_TYPE_NULL_SECTION = "empty";
	public static final String RESPONSE_TYPE_CONNECTION_FAILED = "connection_failed";
	public static final String RESPONSE_TYPE_UPLOADING_FAILED = "uploading failed";
	public static final String RESPONSE_TYPE_INCOMPLETE = "incomplete";
	// ****************************************************************************************

	public static final int MB = 1024;
	public static final int MAX_TIME = 1000 * 60 * 10; // 10 mins in milliseconds
	public static final int MAX_NUMBER_ATTEMPTS_FOR_UPLOAD = 5;
	public static final int SECOND = 1000; // 1000ms
	public static final int HALF_MINUTE = 30000; // 30s=30000ms
	public static final int FIVE_SECONDS = 5000;
	public static final int MAX_NUM_GEOTRIGGERS = 100;
	public static final long GEO_POLL_CHECK_INTERVAL = 1000 * 60 * 10; // 10 mins in milliseconds	
	public static final long GEO_POLL_SERVICE_INTERVAL = 1000 * 60 * 20; // 20 mins in milliseconds
	public static final long PUSH_CHECK_INTERVAL = 1000 * 60 * 30; // 30 mins in milliseconds
	public static final long LOCATIONLOG_INTERVAL = 1000 * 60 * 10; // 10 mins in milliseconds
	
	public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
	public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * DateUtils.HOUR_IN_MILLIS;	

	public final static int SINGLE_PHOTO_REQUEST = 1337;
	public final static int GALLERY_PHOTO_REQUEST = 1338;
	public final static int VIDEO_REQUEST = 1339;
	public final static int EMAIL_REQUEST = 1340;
	
	public static final String IMAGE_FOLDER_ROOT = Environment.getExternalStorageDirectory() + "/SurveyImages/";
	public static final String VIDEO_FOLDER_ROOT = Environment.getExternalStorageDirectory()+ "/SurveyVideos/";
	public static final String DEFAULT_IMAGE_URI="";
	
	public static final String INTERNAL_STORAGE_FOLDER_NAME = "capturedPhotos";

	public static final String CERTSTORE_KEY = Toiler.Keyphrase_Decrypt(com.survey.surveydata.SecureData.CERTSTORE_KEY);
	// ******************** GCM **************************************************************
	public static final int PENDINGINTENT_REQUEST_CODE = 192837;
	
	// ******************** S3 ********************************************************************
	public static final String ACCESS_KEY_ID = Toiler.Keyphrase_Decrypt(com.survey.surveydata.SecureData.ACCESS_KEY_ID);
	public static final String SECRET_KEY = Toiler.Keyphrase_Decrypt(com.survey.surveydata.SecureData.SECRET_KEY);
	public static final String BUCKET = Toiler.Keyphrase_Decrypt(com.survey.surveydata.SecureData.BUCKET);
	public static String getPictureBucket() {
		return Toiler.Keyphrase_Decrypt(com.survey.surveydata.SecureData.BUCKET) + ".s3";
	}
	
	public static final String TAG = "AndroidWidgetSample";
	public static final String INTENT_EXTRA_WIDGET_TEXT = "INTENT_EXTRA_WIDGET TEXT";
	public static final String ACTION_WIDGET_UPDATE_FROM_ACTIVITY = "ACTION_WIDGET_UPDATE_FROM_ACTIVITY";
	public static final String ACTION_WIDGET_UPDATE_FROM_ALARM = "ACTION_WIDGET_UPDATE_FROM_ALARM";
	public static final String ACTION_WIDGET_UPDATE_FROM_WIDGET = "ACTION_WIDGET_UPDATE_FROM_WIDGET";
	public static final String ALARM_STATUS = "ALARM_STATUS";
	// *********************************************************************************************

	public static final String AUDIO_QUESTION_FILE_EXTENSION = "amr";
	public static final String AUDIO_QUESTION_CONTENT_TYPE = "audio/amr";
	
	public static final String PUBLIC_KEY = Toiler.Keyphrase_Decrypt(com.survey.surveydata.SecureData.SYMMETRIC_KEY);
	public static final String SALT = "afacf@!92de&0a69^1694a";	
}
