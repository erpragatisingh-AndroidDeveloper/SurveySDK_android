package com.survey.android.containers;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.survey.android.R;
import com.survey.android.db.SerializationHelper;
import com.survey.android.model.AnswerModel;
import com.survey.android.model.CurrentSectionModel;
import com.survey.android.model.Prefs;
import com.survey.android.model.QuestionModel;
import com.survey.android.services.BackgroundUploader;
import com.survey.android.session.Configuration;
import com.survey.android.util.ConstantData;
import com.survey.android.util.Log;
import com.survey.android.util.Toiler;
import com.survey.android.util.WhiteLabel;
import com.survey.android.webclient.RestClient;

@SuppressWarnings("serial")
public class PollContainer implements Serializable {
	public static String TAG = "POLL_CONTAINER";
	public static int FORWARD_ANIMATION=1;
	public static int BACKWARD_ANIMATION=0;

	private CurrentSectionModel section; // holds current section
	private List<AnswerModel> answers; // holds list of answered questions in
										// current section
	private int cursor; // index of current question inside section
	private int directionAnimation;
	private transient Context context;
	private int question_count; // predicted value of questions in survey (not
								// precise because branching but used for set
								// progress bar value )
	private int fake_index = 1; // used for painting ProgressBar - holds number
								// of answered question from the beginning of
								// the survey
	private String responseId; // holds response id, used for uploading
	private String surveyId; // holds id of current survey
	private String token; // holds user's token
	private String surveyTitle; // holds title of current survey - used only on
								// top bar
	private int numberOfAttempts; // holds number of attempts to upload answers
									// limited on MAX_NUMBER_ATTEMPTS_FOR_UPLOAD

	/**
	 * Returns title of the survey
	 *
	 * @return - String title
	 */
	public String getSurveyTitle() {
		return this.surveyTitle;
	}

	/**
	 * Sets title on passed value
	 *
	 * @param surveyTitle
	 */
	public void setSurveyTitle(String surveyTitle) {
		this.surveyTitle = surveyTitle;
	}

	public String getToken() {
		return this.token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public int getFakeIndex() {
		return this.fake_index;
	}

	public void setFakeIndex(int fakeIndex) {
		this.fake_index = fakeIndex;
	}

	public int getQuestionCount() {
		return this.question_count;
	}

	public void setQuestionCount(int question_count) {
		this.question_count = question_count;
	}

	public PollContainer(String responseId, Context context, String surveyTitle) {
		this.responseId = responseId;
		this.context = context;
		this.surveyTitle = surveyTitle;
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this.context);
		token = prefs.getString(this.context.getString(R.string.token), null);
		numberOfAttempts = 0;
	}

	public PollContainer(String responseId, String survey_id, Context context,
			String surveyTitle) {
		this.responseId = responseId;
		// this.question_count = question_count;
		this.context = context;
		this.surveyId = survey_id;
		this.surveyTitle = surveyTitle;

		// this.s3Client = new AmazonS3Client(new BasicAWSCredentials(
		// ConstantData.ACCESS_KEY_ID, ConstantData.SECRET_KEY));
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this.context);
		token = prefs.getString(this.context.getString(R.string.token), null);
		numberOfAttempts = 0;
	}

	public PollContainer(String responseId, String survey_id,
			int question_count, Context context, String surveyTitle) {
		this.responseId = responseId;
		this.surveyId = survey_id;
		this.context = context;
		this.question_count = question_count;
		this.surveyTitle = surveyTitle;
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this.context);
		token = prefs.getString(this.context.getString(R.string.token), null);
		numberOfAttempts = 0;
	}

	public Context getContext() {
		return this.context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public String getResponseId() {
		return this.responseId;
	}

	public void setResponseId(String responseId) {
		this.responseId = responseId;
	}

	public String getSurveyId() {
		return this.surveyId;
	}

	public void setSurveyId(String surveyId) {
		this.surveyId = surveyId;
	}

	/**
	 * Returns boolean value whcih indicates which animation to load
	 * (forward or backward - which button was pressed next or previous )
	 * @return
	 */
	public boolean isForwardAnimation(){
		return this.directionAnimation==FORWARD_ANIMATION;
	}

	/**
	 * Moves cursor to next position (increment) and increment fake_index
	 * (fake_index - number of answered questions since beginning of the survey)
	 */
	public void nextQuestion() {
		cursor++;
		fake_index++;
		directionAnimation=FORWARD_ANIMATION;
	}

	/**
	 * Moves cursor to previous position ( decrement ) and decrement fake_index
	 * (fake_index - number of answered questions since beginning of the survey)
	 */
	public void previousQuestion(){
		if(cursor>0){
			cursor--;
			fake_index--;
			directionAnimation=BACKWARD_ANIMATION;
		}
	}

	/**
	 * If uploading to amazon was successful then uploads answers ( for images
	 * and videos on amazon generates paths-names) to server
	 */
	public void upload() {
		try {
			numberOfAttempts++;

			// better to remember this if it was successful on amazon but fails
			// on server then no need again on amazon - change this
			boolean success = false;
			boolean s3Result = uploadS3All();
			Log.d(TAG, "s3Result: " + s3Result);
			if (s3Result) {
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(context);
				success = RestClient.sendAnswerResults(prefs.getString(
						context.getString(R.string.token), Prefs.TOKEN),
						responseId, answers);
				if (!Toiler.isNetworkAvailable(context) && !success) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} else {
				success = false;
			}
			if (success) {
				this.numberOfAttempts = 0;
				this.setCurrentSectionModel(CurrentSectionModel.remote(context,
						responseId));

				Log.d(TAG, "deleting photos....");

				java.io.File topicPhotoDir = context.getDir(ConstantData.INTERNAL_STORAGE_FOLDER_NAME,
						Context.MODE_PRIVATE);
				java.io.File[] photos = topicPhotoDir.listFiles();
				Log.i(TAG, "images in internal storage: " + photos.length);

				for (java.io.File photo : photos) {
					photo.delete();
				}

				if (!topicPhotoDir.delete()) {
					Log.i(TAG,
							"Not all installed app images are deleted from phone memory");
				}

			} else {
				this.getSection().setType(
						ConstantData.RESPONSE_TYPE_CONNECTION_FAILED);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, e.getMessage());
		} catch (NullPointerException e) {
			e.printStackTrace();
			Log.e(TAG, e.getMessage());
		}
	}

	public void setCurrentSectionModel(CurrentSectionModel section) {
		this.section = section;
		this.cursor = 0;
		this.directionAnimation=FORWARD_ANIMATION;
		this.answers = new ArrayList<AnswerModel>();
		this.numberOfAttempts = 0;
	}

	public CurrentSectionModel getSection() {
		return this.section;
	}

	public List<AnswerModel> getAnswers() {
		return this.answers;
	}

	public void setAnswers(List<AnswerModel> answers) {
		this.answers = answers;
	}

	public int getCursor() {
		return this.cursor;
	}

	public void setCursor(int cursor) {
		this.cursor = cursor;
	}

	public boolean isFirst() {
		return this.cursor == 0;
	}

	/**
	 * Checks if current question is last one in current section
	 *
	 * @return true - last, false - not last
	 */
	public boolean isLast() {
		return this.cursor == section.getQuestions().size();
	}

	/**
	 * Checks if this current question is last in section, if it is last then is
	 * time to upload and fetch new section, otherwise it is not itme for upload
	 *
	 * @return true - time for upload , false - it is not time for upload
	 */
	public boolean isTimeForUpload() {
		return this.cursor >= this.section.getQuestions().size();
	}

	public QuestionModel getCurrentQuestion() {

		QuestionModel result=null;
		try {
			if(cursor>=section.getQuestions().size()){
				result=section.getQuestions().get(section.getQuestions().size()-1);
			}
			else{
			result= section.getQuestions().get(this.cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Saves current answer to list answers for uploading
	 *
	 * @param answer
	 *            - AnswerModel
	 */
	public void addCurrentAnswer(AnswerModel answer) {
		try {
			if (containsAnswerWithId(answer.getQuestionId())) {
				overwriteAnswer(answer);
			} else {
				this.answers.add(answer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if in list with answers exists answers with same questionId,
	 * already answered
	 *
	 * @param qId
	 *            - String questionId
	 * @return treu - contains, false - doesn't contain
	 */
	private boolean containsAnswerWithId(String qId) {
		for (AnswerModel answer : answers) {
			if (answer.getQuestionId().equals(qId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Replace old answer with new in list of answers
	 *
	 * @param answer
	 *            - AnswerModel, new answer
	 */
	private void overwriteAnswer(AnswerModel answer) {
		boolean repeat = true;
		for (int i = 0; i < answers.size() && repeat; i++) {
			AnswerModel temp = answers.get(i);
			if (temp.getQuestionId().equals(answer.getQuestionId())) {
				answers.set(i, answer);
				repeat = false;
			}
		}
	}

	/**
	 * Adds list of answers in list for uploading
	 *
	 * @param answers
	 */
	public void addAnswers(List<AnswerModel> ans) {
		if (answers != null) {
			if (ans.get(0).getResponseType()
					.equals(ConstantData.RESPONSE_TYPE_MULTIPLE_SELECT)) {
				String questionId = ans.get(0).getQuestionId();
				removeAllWithQuestionId(questionId);
				for (AnswerModel a : ans) {
					this.answers.add(a);
				}
			} else {
				for (AnswerModel a : ans) {
					// this.answers.add(a);
					this.addCurrentAnswer(a);
				}
			}
		}
	}

	/**
	 * Removes all answers with question id passed like parameter used in case
	 * when PollContainer store multi select answers, all values are stored and
	 * sent with same question id (needed in nested Collector class in Question
	 * activity)
	 *
	 * @param questionId
	 */
	private void removeAllWithQuestionId(String questionId) {
		if (answers != null && answers.size() > 0) {
			List<AnswerModel> temp = new LinkedList<AnswerModel>();
			for (AnswerModel a : answers) {
				if (!a.getQuestionId().equals(questionId)) {
					temp.add(a);
				}
			}
			answers = temp;
		}
	}

	public String getResponseType() {

		try{
		if (this.section == null) {
			return ConstantData.RESPONSE_TYPE_NULL_SECTION;
		}

		else if (section.getType().trim()
				.equals(ConstantData.RESPONSE_TYPE_FINISHED)) {
			return ConstantData.RESPONSE_TYPE_FINISHED;
		}

		else if (section.getType().trim()
				.equals(ConstantData.RESPONSE_TYPE_DISQUALIFIED)) {
			return ConstantData.RESPONSE_TYPE_DISQUALIFIED;
		}

		else if (section.getType().trim()
				.equals(ConstantData.RESPONSE_TYPE_CONNECTION_FAILED)) {
			return ConstantData.RESPONSE_TYPE_CONNECTION_FAILED;
		}

		else if (this.section.getQuestions() == null
				|| this.section.getQuestions().size() == 0) {
			return ConstantData.RESPONSE_TYPE_FINISHED;
		} else if (cursor < this.section.getQuestions().size()) {
			return this.section.getQuestions().get(cursor).getResponseType();
		} else {
			return ConstantData.RESPONSE_TYPE_COMPLETED;
		}
		}
		catch(Exception e){
			return ConstantData.RESPONSE_TYPE_NULL_SECTION;
		}
	}

	public int getProgressMax() {
		return this.question_count;
	}

	public int getProgresPosition() {
		return fake_index;
	}

	public String getProgressText() {
		return "" + fake_index + "/" + getProgressMax();
	}

	/**
	 * Pauses survey - saves all answers and other necessary informations in two
	 * tables in db (only one survey by specification can be paused )
	 */
	public void pause(byte[] array) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		@SuppressWarnings("unused")
		String token = prefs.getString(context.getString(R.string.token),Prefs.TOKEN);
		try {
			SerializationHelper dbHelper = new SerializationHelper(context);
			dbHelper.pauseSurvey(array);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Does the actual work of putting a local file to S3; statically so we can call it from the
	 * PollContainer's lifecycle (traditional answer style) or separately from a background service.
	 * See {@link BackgroundUploader}.
	 *
	 * @param localUri which file to upload
	 * @param s3Path the path on S3 to upload to
	 * @return the AmazonS3Client's results of the put request
	 */
	public static final PutObjectResult putFileToS3(String localUri, String s3Path,Context context) {
		
		AmazonS3Client s3Client = new AmazonS3Client
		(new BasicAWSCredentials(Configuration.getAs3AccesKeyId(), Configuration.getAs3SecretKey()));
		s3Client.createBucket(Configuration.getPictureBucket());
		PutObjectRequest por = new PutObjectRequest(Configuration.getAs3Bucket(), s3Path,
						new java.io.File(localUri));
		por.setCannedAcl(CannedAccessControlList.PublicReadWrite);

		// .amr files require a content type in S3 to be played properly in-browser
		if (s3Path.endsWith(ConstantData.AUDIO_QUESTION_FILE_EXTENSION)) {
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType(ConstantData.AUDIO_QUESTION_CONTENT_TYPE);
			por.setMetadata(metadata);
		}

		return s3Client.putObject(por);
	}

	public String uploadS3Answer(String responseType, String uri, String token, String surveyId,
					String responseId) throws NoSuchAlgorithmException,
					UnsupportedEncodingException {
		String s3Path = null;
		Log.i(TAG, "answer responseType: " + responseType + ", answer: " + uri);
		if ((responseType.equals(ConstantData.RESPONSE_TYPE_VIDEO)
						|| responseType.equals(ConstantData.RESPONSE_TYPE_PHOTO) || responseType
							.equals(ConstantData.RESPONSE_TYPE_AUDIO))
						&& !uri.equals(ConstantData.DEFAULT_IMAGE_URI)) {
			Log.i(TAG, "trying to upload answer: " + uri + " via s3");
			s3Path = getS3Filename(responseType, uri, token, surveyId, responseId);

			/*
			 * Upload videos using the new background uploader service, so as not to block the user
			 * with a non-dismissable spinner for a long time. Upload other content right here,
			 * which will show the traditional dialog blocker.
			 */
			if (responseType.equals(ConstantData.RESPONSE_TYPE_VIDEO)) {
				Log.i(TAG, "Spawning service to upload video answer.");
				// Start the photo uploading in a background service; give back the URL it'll be at
				Intent intent = new Intent(getContext(), BackgroundUploader.class);
				intent.putExtra(BackgroundUploader.EXTRA_LOCAL_URI, uri);
				intent.putExtra(BackgroundUploader.EXTRA_S3_PATH, s3Path);
				getContext().startService(intent);
				
				// Turn the s3 path into a full URL where this file will end up
				s3Path = "https://" + Configuration.getAs3Bucket() + ".s3.amazonaws.com/" + s3Path;
			} else {
				Log.i(TAG, "Uploading non-video answer without service.");
				putFileToS3(uri, s3Path, getContext());
			}

			Log.i(TAG, "s3Path: " + s3Path);
		}

		return s3Path;
	}

	public static String getS3Filename(final String responseType, final String uri,
					final String token, final String surveyId, final String responseId)
					throws NoSuchAlgorithmException,
					UnsupportedEncodingException {
		String s3Path = null;

		Log.i(TAG, "answer responseType: " + responseType + ", answer: " + uri);
		if ((responseType.equals(ConstantData.RESPONSE_TYPE_VIDEO) || responseType
						.equals(ConstantData.RESPONSE_TYPE_PHOTO)
						|| responseType.equals(ConstantData.RESPONSE_TYPE_AUDIO))
						&& !uri.equals(ConstantData.DEFAULT_IMAGE_URI)) {

			String extension = getExtension(responseType);

			s3Path = Toiler.validNameS3(responseType, token, surveyId, responseId,
							"" + System.currentTimeMillis(), extension);

			if (responseType.equals(ConstantData.RESPONSE_TYPE_VIDEO)) {
				s3Path = "answers/videos/" + surveyId + "/" + s3Path;
			}
		}

		return s3Path;
	}

	private static String getExtension(String responseType) {
		if (ConstantData.RESPONSE_TYPE_AUDIO.equals(responseType)) {
			return ConstantData.AUDIO_QUESTION_FILE_EXTENSION;
		} else if (ConstantData.RESPONSE_TYPE_VIDEO.equals(responseType)){
			return "3gp";

		} else {
			return "jpg";
		}
	}

	/**
	 * For all answers in list which are type PHOTO, AUDIO, or VIDEO uploads to
	 * hardcoded amazon server
	 *
	 * @return true - all uploadings were successful , false - one or more was
	 *         unsuccessful
	 */
	private boolean uploadS3All() {
		boolean result = true;
		try {
			for (AnswerModel answer : answers) {
				// Upload, and replace the video/image URI with the S3 URL
				String uploadResult = uploadS3Answer(answer.getResponseType(), answer.getAnswer(), token,
								surveyId, responseId);

				if (uploadResult != null) {
					answer.setAnswer(uploadResult);
				}
			}
		} catch (Exception e) {
			result = false;
			if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY))
				Log.e(TAG, "s3 exc: " + e);
			else
				e.printStackTrace();
		}

		return result;
	}

	/**
	 * Checks if number of unsuccessful attempts to upload is bigger than
	 * MAX_NUMBER_ATTEMPTS_FOR_UPLOAD
	 *
	 * @return true - time to pause survey, false - can try again
	 */
	public boolean isOverflownumberAttempts() {
		return numberOfAttempts == ConstantData.MAX_NUMBER_ATTEMPTS_FOR_UPLOAD;
	}

	/**
	 * Resets number of attempts so user can try again
	 * MAX_NUMBER_ATTEMPTS_FOR_UPLOAD times to upload
	 */
	public void resetNumberAttempts() {
		this.numberOfAttempts = 0;
	}

	/**
	 * Return type of response for last question if answres are empty returns
	 * null (used when restore from paused survey)
	 *
	 * @return
	 */
	public String getResponseTypeLastQuestion() {
		String result = null;
		List<AnswerModel> temp = new LinkedList<AnswerModel>();

		if (section != null && section.getQuestions() != null
				&& cursor < section.getQuestions().size()) {
			result = this.section.getQuestions().get(cursor).getResponseType();
		}

		else if (answers != null && answers.size() > 0) {
			for (AnswerModel a : answers) {
				if (!a.getAnswer().equals("token")) {
					temp.add(a);
				}
			}
			if (temp.size() > 0) {
				result = temp.get(temp.size() - 1).getResponseType();
			}
		}
		return result;
	}

	/**
	 *
	 */
	public List<AnswerModel> getAnswerLastQuestion() {
		List<AnswerModel> result = new LinkedList<AnswerModel>();
		List<String> ids = getQuestionIds();
		for (String id : ids) {
			for (AnswerModel a : answers) {
				if (a.getQuestionId().equals(id)
						&& !a.getQuestionId().equals("token")) {
					result.add(a);
				}
			}
		}

		// String questionId = getQuestionIdLastQuestion();
		// if (questionId != null) {
		// for (AnswerModel a : answers) {
		// if (a.getQuestionId().equals(questionId)
		// && !a.getQuestionId().equals("token")) {
		// result.add(a);
		// }
		// }
		// }
		return result;
	}

	/**
	 * Returns question id of last question if answers is null or empty returns
	 * null
	 *
	 * @return - String question id
	 */
	public String getQuestionIdLastQuestion() {
		String result = null;
		// List<AnswerModel> temp = new LinkedList<AnswerModel>();

		if (section != null && section.getQuestions() != null
				&& cursor < section.getQuestions().size()) {
			result = this.section.getQuestions().get(cursor).getId();
		} else {
			result = section.getQuestions()
					.get(section.getQuestions().size() - 1).getId();
		}

		// else if (answers != null && answers.size() > 0) {
		// for (AnswerModel a : answers) {
		// if (!a.getAnswer().equals("token")) {
		// temp.add(a);
		// }
		// }
		// if (temp.size() > 0) {
		// result = temp.get(temp.size() - 1).getQuestionId();
		// }
		// }
		return result;
	}

	/**
	 * Returns question based on question id, if it cannot find question with
	 * the same id in section returns null (searchs only top level question - in
	 * case of ratings it doesn't search inside subquestions)
	 *
	 * @param questionId
	 * @return
	 */
	private QuestionModel getQuestionById(String questionId) {
		QuestionModel result = null;
		try {
			boolean found = false;
			for (int i = 0; i < section.getQuestions().size() && found == false; i++) {
				if (questionId.equals(section.getQuestions().get(i).getId())) {
					result = section.getQuestions().get(i);
					found = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Returns lists of question ids for current question, if question has type
	 * ratings it retiurns list of ids of subquestions otherwise it returns only
	 * single value id of current question
	 *
	 * @return
	 */
	private List<String> getQuestionIds() {
		List<String> result = new LinkedList<String>();
		String lastQuestionID = getQuestionIdLastQuestion();
		QuestionModel currentQUestion = getQuestionById(lastQuestionID);
		if (currentQUestion != null && currentQUestion.getQuestions() != null) {
			if (currentQUestion.getQuestions().size() > 0) {
				for (QuestionModel q : currentQUestion.getQuestions()) {
					result.add(q.getId());
				}
			} else {
				result.add(currentQUestion.getId());
			}
		}
		return result;
	}

	public boolean isGooglePlaceQuestion() {

		try {
			return getCurrentQuestion().getGooglePlaceQuestion();
		} catch (Exception e) {
		}
		return false;
	}
}
