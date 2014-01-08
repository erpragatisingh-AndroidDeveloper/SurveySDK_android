package com.survey.android.webclient;

import java.util.List;
import java.util.Map;

import com.survey.android.model.AnswerModel;
import com.survey.android.model.Prefs;

/**
 * Utility class for Survey-related requests
 * 
 */
public class SurveyRequest extends HttpRequest {
	// Change this to a local server for testing.

	static String createCorrectUrl(String endpoint,
			Map<String, String> arguments) {
		String ret = "";
		if (arguments == null) {
			ret = Prefs.API_URL + endpoint;
		} else {

			String modelName = arguments.get("model_name");
			String objectId = arguments.get("id");
			if (modelName == null || objectId == null) {
				ret = Prefs.API_URL + endpoint;
			} else {
				ret = Prefs.API_URL + "/" + modelName + "/" + objectId + "/"
						+ endpoint;
			}
		}
		return ret;
	}

	static String createCorrectUrl(String endpoint, String objectId,
			String modelName) {
		String ret = "";
		if (modelName == null || objectId == null) {
			ret = Prefs.API_URL + endpoint + ".json";
		} else {
			ret = Prefs.API_URL + "/" + modelName + "/" + objectId + "/"
					+ endpoint + ".json";
		}
		return ret;
	}

	public static String getServerUrl() {
		return Prefs.API_URL;
	}

	public SurveyRequest(String endpoint, Map<String, String> arguments) {
		super(createCorrectUrl(endpoint, arguments), arguments);
	}
	
	public SurveyRequest(String endpoint, Map<String, String> arguments,
			String body) {
		super(createCorrectUrl(endpoint, arguments), arguments, body);
	}

	public SurveyRequest(String endpoint, String body) {
		super(createCorrectUrl(endpoint, null), body);
	}

	public SurveyRequest(String endpoint, String token, String id,
			String model_name, List<AnswerModel> answers) {
		super(createCorrectUrl(endpoint, id, model_name), answers);
	}
}
