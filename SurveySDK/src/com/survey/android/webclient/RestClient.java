package com.survey.android.webclient;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.survey.android.model.AnswerModel;
import com.survey.android.util.ConstantData;
import com.survey.android.util.Log;
import com.survey.android.util.WhiteLabel;

public class RestClient {
	public static final String STACK_TRACE_EP = new SurveyRequest("stacktrace",
			new HashMap<String, String>()).toString();
	private static final String TAG = "RestClient";

	public static JSONArray getCategories(String token) throws JSONException,
			IOException {
		Map<String, String> hm = new HashMap<String, String>();
		hm.put("token", token);
		return wrapJSONArray(new SurveyRequest("categories.json", hm).get());
	}

	public static String getCurrentSection(String token, String id)
			throws JSONException, IOException {
		Map<String, String> hm = new HashMap<String, String>();
		hm.put("token", token);
		hm.put("model_name", "responses");
		hm.put("id", id);
		return (new SurveyRequest("current_section", hm).get());
	}

	public static JSONObject getResponses(String token, String id)
			throws JSONException, IOException {
		Map<String, String> hm = new HashMap<String, String>();
		hm.put("token", token);
		hm.put("model_name", "surveys");
		hm.put("id", id);
		return wrapJSONObject(new SurveyRequest("responses.json", hm).post());
	}

	public static JSONArray getSurveys(String token, String id)
			throws JSONException, IOException {
		Map<String, String> hm = new HashMap<String, String>();
		hm.put("token", token);
		hm.put("model_name", "categories");
		hm.put("id", id);
		return wrapJSONArray(new SurveyRequest("surveys.json", hm).get());
	}
	
	public static JSONObject getSurveyById(String token, String surveyId) throws JSONException, IOException{
		Map<String, String> hm = new HashMap<String, String>();
		hm.put("token", token);
		hm.put("model_name", "surveys");
		hm.put("id", surveyId);
		return wrapJSONObject(new SurveyRequest("", hm).get());
	}

	public static boolean RemoveSurveyById(String token, String surveyId) throws JSONException, IOException{
		Map<String, String> hm = new HashMap<String, String>();
		hm.put("token", token);
		hm.put("model_name", "surveys");
		hm.put("id", surveyId);
		String result = new SurveyRequest("", hm).delete();
		if (result.equals("true")) {
			return true;
		} else {
			return false;
		}	
	}

	public static boolean setAnswers(String token, String id,
			Map<String, String> hm) throws JSONException, IOException {
		hm.put("token", token);
		hm.put("model_name", "responses");
		hm.put("id", id);
		String result = new SurveyRequest("answers.json", hm).post();
		if (result.equals("true")) {
			return true;
		} else {
			return false;
		}
	}

	public static JSONObject signIn(JSONObject userJSONObject)
			throws IOException, Exception {
		String result = new SurveyRequest("sessions", userJSONObject.toString())
				.post();
		try {
			return wrapJSONObject(result);
		} catch (JSONException e) {
			throw new Exception(result);
		}
	}
	
	public static JSONObject signInOrRegister(JSONObject userJSONObject)
	throws IOException, Exception {
		String result = new SurveyRequest("sessionSdk", userJSONObject.toString())
		.post();
		try {
			return wrapJSONObject(result);
		} catch (JSONException e) {
			throw new Exception(result);
		}
	}
	
	public static JSONObject cashOut(String token,String bodyJSON)
			throws IOException, Exception {
		Map<String,String> hm=new HashMap<String,String>();
		hm.put("token",token);
		String result = new SurveyRequest("users/payouts",hm,bodyJSON )
				.post();
		try {
			return wrapJSONObject(result);
		} catch (JSONException e) {
			throw new Exception(result);
		}
	}

	public static JSONObject signUp(JSONObject userJSONObject)
			throws JSONException, IOException {
		return wrapJSONObject(new SurveyRequest("users",
				userJSONObject.toString()).post());
	}
	
	public static JSONObject editProfile(JSONObject userJSONObject,String token,String id)throws JSONException, IOException {
		Map<String, String> hm = new HashMap<String, String>();
		hm.put("token", token);
		hm.put("model_name", "users");
		hm.put("id", id);
		return wrapJSONObject(new SurveyRequest("",hm,
				userJSONObject.toString()).post());
	}

	/**
	 * 
	 * @param the string to wrap
	 * @return the JSONarray encapsulating the string data
	 * @throws JSONException
	 * @throws IOException
	 */
	public static JSONArray wrapJSONArray(String result) throws JSONException,
			IOException {
		return new JSONArray(result);
	}

	/**
	 * 
	 * @param the string to wrap
	 * @return the JSONObject encapsulating the string data
	 * @throws JSONException
	 * @throws IOException
	 */
	public static JSONObject wrapJSONObject(String result)
			throws JSONException, IOException {
		return new JSONObject(result);
	}

	public static boolean sendAnswerResults(String token, String id,
			List<AnswerModel> answers) throws JSONException, IOException {
		String model_name = "responses";
		AnswerModel a = new AnswerModel("token", "TOKEN", token);
		answers.add(0, a);
		boolean result = new SurveyRequest("answers", token, id, model_name,
				answers).post(true);
		return result;
	}
	
	public static JSONObject getUserByToken(String token,String id)throws JSONException, IOException {
		Map<String, String> hm = new HashMap<String, String>();
		hm.put("token", token);
		hm.put("model_name", "users");
		hm.put("id", id);
		return wrapJSONObject(new SurveyRequest("", hm).get());
	}
	
	public static JSONObject getUserIdByToken(String token)throws JSONException, IOException {
		Map<String, String> hm = new HashMap<String, String>();
		hm.put("token", token);
		hm.put("model_name", "users");
		hm.put("id","id");
		JSONObject object = wrapJSONObject(new SurveyRequest("", hm).get()); 
		return object;
	}
	
	public static JSONObject getEarnings(String token, String id) throws JSONException, IOException {
		Map<String, String> hm=new HashMap<String, String>();
		hm.put("token", token);
		hm.put("model_name", "users");
		hm.put("id", id);
		return wrapJSONObject(new SurveyRequest("earnings", hm).get());
	}
	
	public static JSONObject sendC2DMRegistrationId(String registrationId, String type, String deviceid, String token, String userId) throws JSONException, IOException {
		JSONObject object = new JSONObject();
		object.put("gcm_notification_token", registrationId);
		object.put("device_type", type);
		object.put("device_id", deviceid);		
		JSONObject temp=new JSONObject();
		temp.put("user", object);
		
		if (ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY)) {
			Log.d(TAG, "gcm_notification_token: " + registrationId);
			Log.d(TAG, "device_type: " + type);
			Log.d(TAG, "device_id: " + deviceid);			
			Log.d(TAG, "userId: " + userId);
			Log.d(TAG, "token: " + token);
		}

		return wrapJSONObject(new SurveyRequest("users/"+userId+"?token="+token,temp.toString()).post());
	}

	public static JSONArray pollGeoSurveys(String token, double latitude, double longitude) throws JSONException, IOException{
		Map<String, String> hm = new HashMap<String, String>();
		hm.put("token", token);
		hm.put("u_lat", Double.toString(latitude));
		hm.put("u_long", Double.toString(longitude));		
		return wrapJSONArray(new SurveyRequest("geosurveys/surveys", hm).get());		
	}
	
	public static String getGeoPush(String token, String geo_trigger_id) throws JSONException, IOException{
		Map<String, String> hm = new HashMap<String, String>();
		hm.put("token", token);
		hm.put("geo_trigger_id", geo_trigger_id);
		return new SurveyRequest("push.json", hm).get();		
	}
	
	public static String setLocationLog(String token, String geo_trigger_id, double latitude, double longitude, String reason) throws JSONException, IOException{
		Map<String, String> hm = new HashMap<String, String>();
		hm.put("token", token);
		hm.put("latitude", Double.toString(latitude));
		hm.put("longitude", Double.toString(longitude));
		hm.put("reason", reason);
		hm.put("geo_trigger_id", geo_trigger_id);		
		return new SurveyRequest("log", hm).get();		
	}
	
	public static JSONObject pollGeoSurvey(String token, double latitude, double longitude) throws JSONException, IOException{
		Map<String, String> hm = new HashMap<String, String>();
		hm.put("token", token);
		hm.put("u_lat", Double.toString(latitude));
		hm.put("u_long", Double.toString(longitude));			
		return wrapJSONObject(new SurveyRequest("geosurveys/surveys", hm).get());		
	}
	
	/**
	 * fetching resources for AutoCompleteTextViews - country, state
	 * @return
	 * @throws IOException 
	 * @throws JSONException 
	 */
	public static JSONObject getRegistrationResources() throws JSONException, IOException{
		Map<String,String> temp=null;
		return wrapJSONObject(new SurveyRequest("users",temp).get());
	}
	
	public static JSONObject getAppVersionInfo() throws JSONException, IOException{
		Map<String, String> hm=null;
		return wrapJSONObject(new SurveyRequest("apps/android", hm).get());
	}
	
	@SuppressWarnings("unused")
	public static String changePassword(String email)throws IOException, JSONException{
//			Map<String,String>hm=new HashMap<String,String>();
//			hm.put("email", email);
	
		JSONObject result = wrapJSONObject(new SurveyRequest("users/reset_password", null,email).post());
		
		boolean success=false;
		boolean error_messages=false;
		
		String message="";

		try{
			if(result.has("success")){
				message+="Success.\nPlease, check your email.";
			}
			else if(result.has("error_messages")){
				JSONArray array=result.getJSONArray("error_messages");
				for(int i=0;i<array.length();i++){
					message+=array.getString(i)+"\n";
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return message;
	}
	
}
