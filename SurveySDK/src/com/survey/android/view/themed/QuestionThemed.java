package com.survey.android.view.themed;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.json.JSONException;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.Media;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.method.DigitsKeyListener;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.survey.android.R;
import com.survey.android.common.PlacesAutoCompleteAdapter;
import com.survey.android.containers.AppContainer;
import com.survey.android.containers.PollContainer;
import com.survey.android.custom_widgets.PollRatings;
import com.survey.android.custom_widgets.PollRatingsScaleLabeled;
import com.survey.android.custom_widgets.PollSelectionTable;
import com.survey.android.db.SerializationHelper;
import com.survey.android.db.SerializationManager;
import com.survey.android.fragment.AudioRecorderFragment;
import com.survey.android.model.AnswerModel;
import com.survey.android.model.CurrentSectionModel;
import com.survey.android.model.Prefs;
import com.survey.android.model.QuestionModel;
import com.survey.android.session.Configuration;
import com.survey.android.session.Session;
import com.survey.android.session.Configuration.ExitOption;
import com.survey.android.util.ConstantData;
import com.survey.android.util.Log;
import com.survey.android.util.Toiler;
import com.survey.android.util.WhiteLabel;
import com.survey.android.view.Gallery;
import com.survey.android.view.LocalizedFragmentActivity;
import com.survey.android.view.Question;

//*************************************************************************************************

@SuppressLint("SetJavaScriptEnabled")
public abstract class QuestionThemed extends LocalizedFragmentActivity
		implements LocationListener {

	public static String TAG = Question.class.getSimpleName();
	private VideoView videoView = null;
	private Uri imageUri = null; // holds uri of captured image when question
									// requires photo for answer
	private Uri videoUri = null; // holds uri of recorded video when question
									// requires video for answer
	private String videoPath = null; // holds path of to recorded video ( sets
										// to null on every click on RECORD
										// button )
	private PollContainer pollContainer = null; // holds all necessary
												// informations, current
												// section, answers, upload
												// answer, fetch new section
	protected Button btnNextQuestionBottom = null;
	protected Button btnBack = null;
	private ProgressBar pbQuestionNumber = null;
	private TextView txtQuestionNumber = null;
	private TextView txtQuestion = null;
	private ProgressBar pbLoading = null;
	private PollRatingsScaleLabeled rsRatingScale = null;
	private ProgressDialog pd = null;
	private WebView wvMultimediaQuestion = null;
	private String responseId = null;
	private SerializationHelper dbHelper = null;
	private TextView txtRemark = null;
	protected TextView txtTitle = null;
	private String capturedImageFilePath = null;
	private String surveyTitle = null;
	private AlertDialog alertDialog = null;
	private LocationManager lm = null;
	private long activationTime;
	private boolean isPaused;
	@SuppressWarnings("deprecation")
	private final GestureDetector gdt = new GestureDetector(new GestureListener());

	// **************************************************************************
	private AutoCompleteTextView etContent;
	private PollRatings rlRatings;
	private ImageView ivGallery;
	@SuppressWarnings("unused")
	private PopupWindow pw;
	private PollSelectionTable whChoices;

	protected boolean pollCompletedScreenShowing;
	private AudioRecorderFragment mAudioFragment;

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// *********************************************************************************	
		// This is to bypass android check if the internet request is done on main UI thread.
		// The check was introduced in android JB. 
		// See below for more details
		// http://stackoverflow.com/questions/12742001/android-android-os-networkonmainthreadexception-when-acess-http-client
		// http://stackoverflow.com/questions/11926990/networkonmainthreadexception-error-in-jellybean
		// Move network/internet oriented logic to AsyncTask
		// http://developer.android.com/reference/android/os/AsyncTask.html
		// 
		if( Build.VERSION.SDK_INT >= 9){
		    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

		    StrictMode.setThreadPolicy(policy); 
		}
		// *********************************************************************************
		
		this.surveyTitle = getIntent().getStringExtra("survey_title");
		this.responseId = getIntent().getStringExtra("response_id");
		isPaused = getIntent().getBooleanExtra("is_paused", false);
		
		this.pollContainer = new PollContainer(responseId, getIntent()
				.getStringExtra("survey_id"), getIntent().getIntExtra(
				"question_count", -1), QuestionThemed.this, surveyTitle);

		// *************************** If survey was paused
		// **************************
		dbHelper = new SerializationHelper(QuestionThemed.this);
		if (isPaused) {
			byte[] temp = dbHelper.reloadPauseSurvey();
			pollContainer = SerializationManager.deserializePollContainer(temp,
					QuestionThemed.this);
			if (dbHelper.getSQLiteDatabase().isOpen()) {
				dbHelper.getSQLiteDatabase().close();
			}
		} else {
			initCurrentSection();
		}
		initialize();

		// (new FirstInitTask()).execute();

	}

	@Override
	public void onStart() {
		super.onStart();
	}
	  
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	protected void initUI() {
		// Everything is layout dynamically, so nothing to do here
	}

	@Override
	public void onResume() {
		super.onResume();
		activationTime = System.currentTimeMillis();
		AppContainer container = new AppContainer(QuestionThemed.this);
		Location location = container.location;
		if (location == null || (activationTime - location.getTime()) > ConstantData.MAX_TIME) {
			container.location = null;
			lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_COARSE);
			criteria.setSpeedRequired(true);
			String provider = lm.getBestProvider(criteria, true);

			if (provider != null && provider.length() > 0) {
				lm.requestLocationUpdates(provider, 5, 5, this);
			} else {
				AlertDialog dialog = new AlertDialog.Builder(
						QuestionThemed.this)
						.setTitle(R.string.provider_title)
						.setPositiveButton(R.string.provider_enable,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int whichButton) {

										Intent gpsOptionsIntent = new Intent(
												android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
										startActivity(gpsOptionsIntent);
									}
								})
						.setNegativeButton(R.string.provider_disable,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int whichButton) {
									}
								}).create();
				dialog.show();
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (dbHelper != null && dbHelper.getSQLiteDatabase().isOpen()) {
			dbHelper.getSQLiteDatabase().close();
		}
		super.onDestroy();
	}

	/**
	 * Fetches section from server and put in PollContainer
	 */
	private void initCurrentSection() {
		try {
			pollContainer.setCurrentSectionModel(CurrentSectionModel.remote(
					QuestionThemed.this, pollContainer.getResponseId()));
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Calls appropriate function to initialize screen, fill fields and connect
	 * events with actions for current question based on question type
	 */
	private void initialize() {
		try {
			String responseType = pollContainer.getResponseType();
			if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY))
				Log.d(TAG, "responseType: " + responseType);
			if (responseType.equals(ConstantData.RESPONSE_TYPE_SINGLE_SELECT)) {
				init_single_select_question();
			} else if (responseType.equals(ConstantData.RESPONSE_TYPE_READ_ONLY)) {
				init_readonly_question();
			} else if (responseType.equals(ConstantData.RESPONSE_TYPE_RATING_SCALE_LABELED)) {
				init_rating_scale_labeled();
			} else if (responseType.equals(ConstantData.RESPONSE_TYPE_MULTIPLE_SELECT)) {
				init_multiple_select_question();
			} else if (responseType
					.equals(ConstantData.RESPONSE_TYPE_FREE_TEXT)
					|| responseType
							.equals(ConstantData.RESPONSE_TYPE_NUMERIC_INTEGER)
					|| responseType
							.equals(ConstantData.RESPONSE_TYPE_NUMERIC_DECIMAL)) {
				init_free_text_question();
			} else if (responseType
					.equals(ConstantData.RESPONSE_TYPE_OPEN_ENDED_TEXT)) {
				init_open_ended_text_question();
			} else if (responseType.equals(ConstantData.RESPONSE_TYPE_RATING_SCALE)) {
				init_ratings_question();
			} else if (responseType.equals(ConstantData.RESPONSE_TYPE_PHOTO)) {
				init_single_photo_question();
				if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY))
					customizeQuestionUI(responseType);
			} else if (responseType.equals(ConstantData.RESPONSE_TYPE_VIDEO)) {
				init_video_question();
				if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY))
					customizeQuestionUI(responseType);
			} else if (responseType.equals(ConstantData.RESPONSE_TYPE_AUDIO)) {
				init_audio_question();
				if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY))
					customizeQuestionUI(responseType);
			} else if (responseType
					.equals(ConstantData.RESPONSE_TYPE_COMPLETED)
					|| responseType.equals(ConstantData.RESPONSE_TYPE_FINISHED)) {
				init_poll_completed(R.string.completed_question);
				if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY))
					customizeQuestionUI(responseType);
			} else if (responseType
					.equals(ConstantData.RESPONSE_TYPE_DISQUALIFIED)) {
				init_poll_completed(R.string.disqualified);
			} else if (responseType
					.equals(ConstantData.RESPONSE_TYPE_NULL_SECTION)) {
				finish();
				// init_uploading();
			} else if (responseType
					.equals(ConstantData.RESPONSE_TYPE_CONNECTION_FAILED)) {
				// init_poll_completed(R.string.uploading_failed);
				init_uploading_failed();
			} else {
				init_unknown_type();
			}

			if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY)) {
				customizeTheme();
				pollCompletedScreenShowing = false;
			}

		} catch (Exception e) {
			finish();
			e.printStackTrace();
		}
	}

	private void customizeQuestionUI(String responseType) {
		if (responseType.equals(ConstantData.RESPONSE_TYPE_SINGLE_SELECT)) {

		} else if (responseType.equals(ConstantData.RESPONSE_TYPE_READ_ONLY)) {

		} else if (responseType
				.equals(ConstantData.RESPONSE_TYPE_RATING_SCALE_LABELED)) {

		} else if (responseType
				.equals(ConstantData.RESPONSE_TYPE_MULTIPLE_SELECT)) {

		} else if (responseType.equals(ConstantData.RESPONSE_TYPE_FREE_TEXT)
				|| responseType
						.equals(ConstantData.RESPONSE_TYPE_NUMERIC_INTEGER)
				|| responseType
						.equals(ConstantData.RESPONSE_TYPE_NUMERIC_DECIMAL)) {

		} else if (responseType
				.equals(ConstantData.RESPONSE_TYPE_OPEN_ENDED_TEXT)) {

		} else if (responseType.equals(ConstantData.RESPONSE_TYPE_RATING_SCALE)) {

		} else if (responseType.equals(ConstantData.RESPONSE_TYPE_PHOTO)) {
			Button btnCapture = (Button) this.findViewById(R.id.btnCapture);
			Button btnSelect = (Button) this.findViewById(R.id.btnSelect);

			if (ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.KORINTHOS)) {
				btnCapture
						.setBackgroundResource(R.drawable.selector_button_korinthos);
				btnSelect
						.setBackgroundResource(R.drawable.selector_button_korinthos);
			} else {
				btnCapture.setBackgroundResource(R.drawable.selector_button);
				btnSelect.setBackgroundResource(R.drawable.selector_button);
			}
			btnCapture.setTextAppearance(getApplicationContext(),
					R.style.sdk_button_text_default_button_text);
			btnSelect.setTextAppearance(getApplicationContext(),
					R.style.sdk_button_text_default_button_text);
		} else if (responseType.equals(ConstantData.RESPONSE_TYPE_VIDEO)) {
			Button btnCapture = (Button) this.findViewById(R.id.btnCapture);
			Button btnPlay = (Button) this.findViewById(R.id.btnPlay);

			if (ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.KORINTHOS)) {
				btnCapture
						.setBackgroundResource(R.drawable.selector_button_korinthos);
				btnPlay.setBackgroundResource(R.drawable.selector_button_korinthos);
			} else {
				btnCapture.setBackgroundResource(R.drawable.selector_button);
				btnPlay.setBackgroundResource(R.drawable.selector_button);
			}
			btnCapture.setTextAppearance(getApplicationContext(),
					R.style.sdk_button_text_default_button_text);
			btnPlay.setTextAppearance(getApplicationContext(),
					R.style.sdk_button_text_default_button_text);
		} else if (responseType.equals(ConstantData.RESPONSE_TYPE_COMPLETED)
				|| responseType.equals(ConstantData.RESPONSE_TYPE_FINISHED)) {
			Button btnSubmit = (Button) this.findViewById(R.id.btnSubmit);
			if (ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.KORINTHOS)) {
				btnSubmit
						.setBackgroundResource(R.drawable.selector_button_korinthos);
			} else {
				btnSubmit.setBackgroundResource(R.drawable.selector_button);
			}
			btnSubmit.setTextAppearance(getApplicationContext(),
					R.style.sdk_button_text_default_button_text);
		} else if (responseType.equals(ConstantData.RESPONSE_TYPE_DISQUALIFIED)) {

		} else if (responseType.equals(ConstantData.RESPONSE_TYPE_NULL_SECTION)) {

		} else if (responseType
				.equals(ConstantData.RESPONSE_TYPE_CONNECTION_FAILED)) {

		} else {

		}
	}

	protected abstract void customizeTheme();

	// ************************* Callback functions
	// ******************************
	/**
	 * Initializes screen for question without any need of answer - just info
	 * part
	 */
	private void init_readonly_question() {
		try {
			final LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			final Animation a = AnimationUtils.loadAnimation(this,
					pollContainer.isForwardAnimation() ? R.anim.alpha
							: R.anim.alpha_backward);
			LinearLayout layout = (LinearLayout) li.inflate(
					R.layout.q_read_only, null);
			layout.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					gdt.onTouchEvent(event);
					return true;
				}
			});
			layout.setAnimation(a);
			this.setContentView(layout);
			this.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

			txtTitle = (TextView) findViewById(R.id.txtTitle);
			btnNextQuestionBottom = (Button) this
					.findViewById(R.id.btnNextQuestionBottom);

			btnBack = (Button) this.findViewById(R.id.btnBack);

			pbQuestionNumber = (ProgressBar) this
					.findViewById(R.id.pbQuestionNumber);

			txtQuestion = (TextView) this.findViewById(R.id.txtQuestion);
			wvMultimediaQuestion = (WebView) this
					.findViewById(R.id.wvMultimediaQuestion);
			wvMultimediaQuestion.setBackgroundColor(0);
			WebSettings webSettings = wvMultimediaQuestion.getSettings();
			webSettings.setJavaScriptEnabled(true);
			webSettings.setDefaultTextEncodingName("UTF-8");
			txtQuestionNumber = (TextView) this
					.findViewById(R.id.txtQuestionNumber);
			btnNextQuestionBottom.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					pollContainer.addCurrentAnswer(new AnswerModel(
							pollContainer.getCurrentQuestion().getId(),
							pollContainer.getCurrentQuestion()
									.getResponseType(), ""));
					next();
				}
			});

			btnBack.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					previous();
				}
			});

			enableControls();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes screen for question with single select choice ( wheel )
	 */
	private void init_single_select_question() {

		try {
			final LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			final Animation a = AnimationUtils.loadAnimation(this,
					pollContainer.isForwardAnimation() ? R.anim.alpha
							: R.anim.alpha_backward);
			final LinearLayout layout = (LinearLayout) li.inflate(
					R.layout.q_single_select, null);
			layout.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					gdt.onTouchEvent(event);
					return true;
				}
			});
			layout.setAnimation(a);
			this.setContentView(layout);
			this.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

			whChoices = (PollSelectionTable) layout
					.findViewById(R.id.whChoices);
			QuestionModel tempQ = pollContainer.getCurrentQuestion();
			whChoices.initializeItems(PollSelectionTable.SINGLE_SELECT,
					tempQ.getChoiceId(), tempQ.getChoiceLabel(),
					tempQ.getChoiceValue());
			txtTitle = (TextView) findViewById(R.id.txtTitle);
			btnNextQuestionBottom = (Button) this
					.findViewById(R.id.btnNextQuestionBottom);
			btnBack = (Button) this.findViewById(R.id.btnBack);
			pbQuestionNumber = (ProgressBar) this
					.findViewById(R.id.pbQuestionNumber);

			txtQuestion = (TextView) this.findViewById(R.id.txtQuestion);
			wvMultimediaQuestion = (WebView) this
					.findViewById(R.id.wvMultimediaQuestion);
			wvMultimediaQuestion.setBackgroundColor(0);
			WebSettings webSettings = wvMultimediaQuestion.getSettings();
			webSettings.setJavaScriptEnabled(true);
			webSettings.setDefaultTextEncodingName("UTF-8");
			pbLoading = (ProgressBar) this.findViewById(R.id.pbLoading);
			wvMultimediaQuestion.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url,
						Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
					pbLoading.setVisibility(View.VISIBLE);
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					pbLoading.setVisibility(View.GONE);
				}
			});
			txtQuestionNumber = (TextView) this
					.findViewById(R.id.txtQuestionNumber);
			txtRemark = (TextView) this.findViewById(R.id.txtRemark);
			if (tempQ.getHint() != null && tempQ.getHint().length() > 0) {
				txtRemark.setText(tempQ.getHint());
			}
			btnNextQuestionBottom.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!whChoices.isAnswered()) {
						toastForceAnswer();
					} else {
						pollContainer.addCurrentAnswer(new AnswerModel(
								pollContainer.getCurrentQuestion().getId(),
								pollContainer.getCurrentQuestion()
										.getResponseType(), whChoices
										.getSelectedAnswer()));
						next();
					}
				}
			});

			btnBack.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					previous();
				}
			});

			Button btnClear = (Button) layout.findViewById(R.id.btnClear);

			// clears checked item in wheel - first popup to confirm your
			// intention
			btnClear.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							QuestionThemed.this);
					builder.setMessage(
							getResources().getString(R.string.are_you_sure))
							.setPositiveButton(
									getResources().getString(R.string.ok),
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog, int id) {
											whChoices.clearAll();
										}
									})
							.setNegativeButton(
									getResources().getString(R.string.Cancel),
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog, int id) {
										}
									}).create().show();
				}
			});

			enableControls();
			(new Collector()).restoreLastAnswer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes screen for question with multiple select choice ( wheel )
	 */
	private void init_multiple_select_question() {
		final LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final Animation a = AnimationUtils.loadAnimation(this, pollContainer
				.isForwardAnimation() ? R.anim.alpha : R.anim.alpha_backward);
		LinearLayout layout = (LinearLayout) li.inflate(
				R.layout.q_multiple_select, null);
		layout.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				gdt.onTouchEvent(event);
				return true;
			}
		});
		layout.setAnimation(a);
		this.setContentView(layout);
		whChoices = (PollSelectionTable) layout.findViewById(R.id.whChoices);
		final QuestionModel tempQ = pollContainer.getCurrentQuestion();
		whChoices.initializeItems(tempQ.getId(),
				PollSelectionTable.MULTIPLE_SELECT, tempQ.getChoiceId(),
				tempQ.getChoiceLabel(), tempQ.getChoiceValue(),
				tempQ.getMutuallyExclusive());

		txtTitle = (TextView) findViewById(R.id.txtTitle);
		btnNextQuestionBottom = (Button) this
				.findViewById(R.id.btnNextQuestionBottom);
		btnBack = (Button) this.findViewById(R.id.btnBack);
		pbQuestionNumber = (ProgressBar) this
				.findViewById(R.id.pbQuestionNumber);
		txtQuestion = (TextView) this.findViewById(R.id.txtQuestion);
		wvMultimediaQuestion = (WebView) this
				.findViewById(R.id.wvMultimediaQuestion);
		wvMultimediaQuestion.setBackgroundColor(0);
		wvMultimediaQuestion.getSettings().setJavaScriptEnabled(true);
		pbLoading = (ProgressBar) this.findViewById(R.id.pbLoading);
		wvMultimediaQuestion.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				pbLoading.setVisibility(View.VISIBLE);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				pbLoading.setVisibility(View.GONE);
			}
		});
		txtQuestionNumber = (TextView) this
				.findViewById(R.id.txtQuestionNumber);
		txtRemark = (TextView) this.findViewById(R.id.txtRemark);
		if (tempQ.getHint() != null && tempQ.getHint().length() > 0) {
			txtRemark.setText(tempQ.getHint());
		}
		Button btnClearAll = (Button) layout.findViewById(R.id.btnClearAll);
		btnClearAll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						QuestionThemed.this);
				builder.setMessage(
						getResources().getString(R.string.are_you_sure))
						.setPositiveButton(
								getResources().getString(R.string.ok),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										whChoices.clearAll();
									}
								})
						.setNegativeButton(
								getResources().getString(R.string.Cancel),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
									}
								}).create().show();
			}
		});
		btnNextQuestionBottom.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!whChoices.isAnswered()) {
					toastForceAnswer();
				} else {
					pollContainer.addAnswers(whChoices.getSelectedAnswers());
					next();
				}
			}
		});

		btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				previous();
			}
		});
		enableControls();
		(new Collector()).restoreLastAnswer();
	}

	/**
	 * Initializes screen for question with lot off text
	 */
	private void init_open_ended_text_question() {
		final LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final Animation a = AnimationUtils.loadAnimation(this, pollContainer
				.isForwardAnimation() ? R.anim.alpha : R.anim.alpha_backward);
		LinearLayout layout = (LinearLayout) li.inflate(R.layout.q_text_area,
				null);
		layout.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				gdt.onTouchEvent(event);
				return true;
			}
		});
		layout.setAnimation(a);
		this.setContentView(layout);
		try {
			/* final EditText */
			etContent = (AutoCompleteTextView) this.findViewById(R.id.etContent);

			if (pollContainer.isGooglePlaceQuestion()) {
				etContent.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));
				etContent.setLines(5);
				etContent.requestLayout();
			}

			String hint = pollContainer.getCurrentQuestion().getHint();
			etContent.setHint(hint != null ? hint : "");
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(etContent.getWindowToken(), 0);

			txtTitle = (TextView) findViewById(R.id.txtTitle);
			btnNextQuestionBottom = (Button) this
					.findViewById(R.id.btnNextQuestionBottom);
			btnBack = (Button) this.findViewById(R.id.btnBack);
			pbQuestionNumber = (ProgressBar) this
					.findViewById(R.id.pbQuestionNumber);
			txtQuestion = (TextView) this.findViewById(R.id.txtQuestion);
			wvMultimediaQuestion = (WebView) this
					.findViewById(R.id.wvMultimediaQuestion);
			wvMultimediaQuestion.setBackgroundColor(0);
			wvMultimediaQuestion.getSettings().setJavaScriptEnabled(true);
			pbLoading = (ProgressBar) this.findViewById(R.id.pbLoading);
			wvMultimediaQuestion.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url,
						Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
					pbLoading.setVisibility(View.VISIBLE);
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					pbLoading.setVisibility(View.GONE);
				}
			});
			txtQuestionNumber = (TextView) this
					.findViewById(R.id.txtQuestionNumber);
			btnNextQuestionBottom.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String text = etContent.getText().toString();
					if (text == null || text.equals("")) {
						toastForceAnswer();
					} else {

						if (pollContainer.isGooglePlaceQuestion()) {
							String json = ((PlacesAutoCompleteAdapter)etContent.getAdapter()).getJSONReponse(text);

							pollContainer.addCurrentAnswer(new AnswerModel(
									pollContainer.getCurrentQuestion().getId(),
									pollContainer.getCurrentQuestion().getResponseType(), text, json));
						} else {

							pollContainer.addCurrentAnswer(new AnswerModel(
									pollContainer.getCurrentQuestion().getId(),
									pollContainer.getCurrentQuestion().getResponseType(), text));
						}

						((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
								.hideSoftInputFromWindow(
										etContent.getWindowToken(), 0);
						next();
					}
				}
			});

			btnBack.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					previous();
				}
			});
			enableControls();
			(new Collector()).restoreLastAnswer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes screen for question with sliders and labels above and/or
	 * bellow
	 */
	private void init_rating_scale_labeled() {
		try {
			final LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			final Animation a = AnimationUtils.loadAnimation(this,
					pollContainer.isForwardAnimation() ? R.anim.alpha
							: R.anim.alpha_backward);
			LinearLayout layout = (LinearLayout) li.inflate(
					R.layout.q_ratings_scale_labeled, null);
			layout.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					gdt.onTouchEvent(event);
					return true;
				}
			});
			layout.setAnimation(a);
			this.setContentView(layout);
			txtTitle = (TextView) findViewById(R.id.txtTitle);
			btnNextQuestionBottom = (Button) this
					.findViewById(R.id.btnNextQuestionBottom);
			btnBack = (Button) this.findViewById(R.id.btnBack);
			pbQuestionNumber = (ProgressBar) this
					.findViewById(R.id.pbQuestionNumber);
			txtQuestion = (TextView) this.findViewById(R.id.txtQuestion);
			txtQuestionNumber = (TextView) this
					.findViewById(R.id.txtQuestionNumber);
			rsRatingScale = (PollRatingsScaleLabeled) this
					.findViewById(R.id.rsRatingScale);
			wvMultimediaQuestion = (WebView) this
					.findViewById(R.id.wvMultimediaQuestion);
			wvMultimediaQuestion.setBackgroundColor(0);
			WebSettings webSettings = wvMultimediaQuestion.getSettings();
			webSettings.setDefaultTextEncodingName("UTF-8");
			pbLoading = (ProgressBar) this.findViewById(R.id.pbLoading);
			wvMultimediaQuestion.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url,
						Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
					pbLoading.setVisibility(View.VISIBLE);
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					pbLoading.setVisibility(View.GONE);
				}
			});
			btnNextQuestionBottom.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!rsRatingScale.isAnswered()) {
						toastForceAnswer();
					} else {
						pollContainer.addAnswers(rsRatingScale.getAnswers());
						next();
					}
				}
			});

			btnBack.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					previous();
				}
			});
			enableControls();

			QuestionModel tempQ = pollContainer.getCurrentQuestion();
			rsRatingScale.initiliaze(tempQ.getSubquestions(),
					tempQ.getChoiceLabel(), tempQ.getChoiceValue(),
					tempQ.getText(), tempQ.getId());
			(new Collector()).restoreLastAnswer();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes screen for question with sliders with labels on right side
	 */
	private void init_ratings_question() {
		try {
			final LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			final Animation a = AnimationUtils.loadAnimation(this,
					pollContainer.isForwardAnimation() ? R.anim.alpha
							: R.anim.alpha_backward);
			LinearLayout layout = (LinearLayout) li.inflate(R.layout.q_ratings,
					null);
			layout.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					gdt.onTouchEvent(event);
					return true;
				}
			});
			layout.setAnimation(a);
			this.setContentView(layout);
			txtTitle = (TextView) findViewById(R.id.txtTitle);
			btnNextQuestionBottom = (Button) this
					.findViewById(R.id.btnNextQuestionBottom);
			btnBack = (Button) this.findViewById(R.id.btnBack);
			pbQuestionNumber = (ProgressBar) this
					.findViewById(R.id.pbQuestionNumber);
			txtQuestion = (TextView) this.findViewById(R.id.txtQuestion);
			wvMultimediaQuestion = (WebView) this
					.findViewById(R.id.wvMultimediaQuestion);
			wvMultimediaQuestion.setBackgroundColor(0);
			WebSettings webSettings = wvMultimediaQuestion.getSettings();
			webSettings.setJavaScriptEnabled(true);
			webSettings.setDefaultTextEncodingName("UTF-8");
			pbLoading = (ProgressBar) this.findViewById(R.id.pbLoading);
			wvMultimediaQuestion.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url,
						Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
					pbLoading.setVisibility(View.VISIBLE);
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					pbLoading.setVisibility(View.GONE);
				}
			});
			txtQuestionNumber = (TextView) this
					.findViewById(R.id.txtQuestionNumber);
			/* final PollRatings */rlRatings = (PollRatings) this
					.findViewById(R.id.rlRatings);
			btnNextQuestionBottom.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!rlRatings.isAnswered()) {
						toastForceAnswer();
					} else {
						pollContainer.addAnswers(rlRatings.getAnswers());
						next();
					}
				}
			});
			btnBack.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					previous();
				}
			});
			enableControls();

			QuestionModel tempQ = pollContainer.getCurrentQuestion();
			android.util.Log.i("TEMPQ", tempQ.toJson());
			rlRatings.initiliaze(tempQ.getSubquestions(), tempQ.getMin(),
					tempQ.getMax(), tempQ.getMinLabel(), tempQ.getMaxLabel(),
					tempQ.getId());
			(new Collector()).restoreLastAnswer();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes screen when survey is completed or when user is disqualified
	 *
	 * @param message_id
	 *            - message to show ( one if survey is completed successfully,
	 *            user disqualified )
	 */
	private void init_poll_completed(int message_id) {
		if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY))
			pollCompletedScreenShowing = true;
		final LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final Animation a = AnimationUtils.loadAnimation(this, pollContainer
				.isForwardAnimation() ? R.anim.alpha : R.anim.alpha_backward);
		LinearLayout layout = (LinearLayout) li.inflate(
				R.layout.q_poll_completed, null);
		layout.setAnimation(a);
		this.setContentView(layout);
		TextView txtMessage = (TextView) this.findViewById(R.id.textMessage);
		Resources res = getResources();
		String text = res.getString(message_id);
		CharSequence styledText = Html.fromHtml(text);
		txtMessage.setText(styledText);
		txtTitle = (TextView) findViewById(R.id.txtTitle);
		txtTitle.setText(surveyTitle);
		pbQuestionNumber = (ProgressBar) this
				.findViewById(R.id.pbQuestionNumber);
		pbQuestionNumber.setMax(10);
		pbQuestionNumber.setProgress(10);
		txtQuestionNumber = (TextView) this
				.findViewById(R.id.txtQuestionNumber);
		txtQuestionNumber.setText("COMPLETED");
		txtQuestion = (TextView) this.findViewById(R.id.txtQuestion);
		txtQuestion.setVisibility(View.GONE);

		Button btnSubmit = (Button) this.findViewById(R.id.btnSubmit);
		btnSubmit.setOnClickListener(new OnClickListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				try {
					SerializationHelper dbHelper = new SerializationHelper(QuestionThemed.this);
					dbHelper.deleteAll();
					if (dbHelper.getSQLiteDatabase().isOpen()) {
						dbHelper.getSQLiteDatabase().close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				try {
					Log.i(TAG, "Polling for geo surveys initated at: " + new Date().toGMTString());
					String userToken = prefs.getString(getString(R.string.token), null);
					Log.d(TAG, "userToken: " + userToken);
					Toiler.retrieveAndRunGeoSurveys(getApplicationContext(), userToken, true); // force refresh geotriggers
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
				//Configuration configuration = new Configuration(getApplicationContext());
				ExitOption exitOption = Configuration.getExitOption();
				if(exitOption == ExitOption.GO_BACK_TO_SURVEY){
					Log.i(TAG, "You are going back to survey list");
				}else if (exitOption ==  ExitOption.LOGOUT){
					Log.i(TAG, "You are logOut");
					Session session = new Session(getApplicationContext());
					session.logOut();
				}
				
				finish();
			}
		});
	}

	private void init_unknown_type() {
		final LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final Animation a = AnimationUtils.loadAnimation(this, pollContainer
				.isForwardAnimation() ? R.anim.alpha : R.anim.alpha_backward);
		LinearLayout layout = (LinearLayout) li.inflate(R.layout.q_uploading,
				null);
		layout.setAnimation(a);
		this.setContentView(layout);
		LinearLayout llCompleted = (LinearLayout) layout
				.findViewById(R.id.llCompleted);
		llCompleted.setVisibility(View.GONE);

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Resources res = getResources();
				String text = res.getString(R.string.unknown_question_type);
				CharSequence styledText = Html.fromHtml(text);
				AlertDialog alert = new AlertDialog.Builder(QuestionThemed.this)
						.setMessage(styledText)
						.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int whichButton) {
										finish();
									}
								}).create();
				alert.show();
			}
		}, 750);
	}

	/**
	 * Initializes screen for question with text answer ( expects small amount
	 * of text ), can be text, numeric integer and numeric decimal ( different
	 * keyboards and hints based on type )
	 */
	private void init_free_text_question() {
		final LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final Animation a = AnimationUtils.loadAnimation(this, pollContainer
				.isForwardAnimation() ? R.anim.alpha : R.anim.alpha_backward);
		LinearLayout layout = (LinearLayout) li.inflate(R.layout.q_text_box,
				null);
		layout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				gdt.onTouchEvent(event);
				return true;
			}
		});
		layout.setAnimation(a);

		this.setContentView(layout);
		try {
			/* final EditText */etContent = (AutoCompleteTextView) this.findViewById(R.id.etContent);

			if (pollContainer.isGooglePlaceQuestion()) {
				etContent.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));
//				etContent.setLines(5);
				etContent.requestLayout();
			}

			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(etContent.getWindowToken(), 0);

			String responseType = pollContainer.getResponseType();
			String hint = Toiler.getHintForNumbers(pollContainer
					.getCurrentQuestion());
			if (responseType
					.equals(ConstantData.RESPONSE_TYPE_NUMERIC_INTEGER)) {
				etContent.setKeyListener(DigitsKeyListener.getInstance(
						false, false));
				etContent.setHint(hint);
			} else if (responseType
					.equals(ConstantData.RESPONSE_TYPE_NUMERIC_DECIMAL)) {
				etContent.setKeyListener(DigitsKeyListener.getInstance(
						false, true));
				etContent.setHint(hint);
			}

			etContent.requestFocus();

			txtTitle = (TextView) findViewById(R.id.txtTitle);
			btnNextQuestionBottom = (Button) this
					.findViewById(R.id.btnNextQuestionBottom);
			btnBack = (Button) this.findViewById(R.id.btnBack);
			pbQuestionNumber = (ProgressBar) this
					.findViewById(R.id.pbQuestionNumber);
			txtQuestion = (TextView) this.findViewById(R.id.txtQuestion);
			wvMultimediaQuestion = (WebView) this
					.findViewById(R.id.wvMultimediaQuestion);
			wvMultimediaQuestion.setBackgroundColor(0);
			WebSettings webSettings = wvMultimediaQuestion.getSettings();
			webSettings.setJavaScriptEnabled(true);
			webSettings.setDefaultTextEncodingName("UTF-8");
			pbLoading = (ProgressBar) this.findViewById(R.id.pbLoading);
			wvMultimediaQuestion.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url,
						Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
					pbLoading.setVisibility(View.VISIBLE);
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					pbLoading.setVisibility(View.GONE);
				}
			});
			txtQuestionNumber = (TextView) this
					.findViewById(R.id.txtQuestionNumber);
			btnNextQuestionBottom.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String text = etContent.getText().toString();
					boolean isValid = Toiler.isValidText(text,
							pollContainer.getCurrentQuestion());
					if (!isValid) {
						toastForceAnswer();
					} else {

						((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
						.hideSoftInputFromWindow(
								etContent.getWindowToken(), 0);

						if (pollContainer.isGooglePlaceQuestion()) {
							String json = ((PlacesAutoCompleteAdapter)etContent.getAdapter()).getJSONReponse(text);

							pollContainer.addCurrentAnswer(new AnswerModel(
									pollContainer.getCurrentQuestion().getId(),
									pollContainer.getCurrentQuestion().getResponseType(), text, json));
						} else {

							pollContainer.addCurrentAnswer(new AnswerModel(
									pollContainer.getCurrentQuestion().getId(),
									pollContainer.getCurrentQuestion().getResponseType(), text));
						}

						next();
					}
				}
			});
			btnBack.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					previous();
				}
			});
			enableControls();
			(new Collector()).restoreLastAnswer();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes screen for question of audio type
	 */
	private void init_audio_question() {
		try {
			this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			final LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			final Animation a = AnimationUtils.loadAnimation(this,
					pollContainer.isForwardAnimation() ? R.anim.alpha
							: R.anim.alpha_backward);
			final LinearLayout layout = (LinearLayout) li.inflate(R.layout.q_audio,
							null);
			layout.setAnimation(a);
			this.setContentView(layout);

			/*
			 * Remove the existing audio-record fragment if it's there (eg from navigation between
			 *  multiple audio-recording questions
			 */
			final AudioRecorderFragment newFragment = new AudioRecorderFragment();
			FragmentManager fragmentManager = getSupportFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.add(R.id.customGalleryFooter, newFragment);
			fragmentTransaction.commit();

			mAudioFragment = newFragment;
			final AudioRecorderFragment fragment = mAudioFragment;

			layout.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					gdt.onTouchEvent(event);
					return true;
				}
			});

			Integer length = pollContainer.getCurrentQuestion().getLength();

			if (null != length && length > 0) {
				fragment.setDurationLimitSeconds(length);
			}

			txtTitle = (TextView) findViewById(R.id.txtTitle);
			btnNextQuestionBottom = (Button) this
					.findViewById(R.id.btnNextQuestionBottom);
			btnBack = (Button) this.findViewById(R.id.btnBack);
			pbQuestionNumber = (ProgressBar) this
					.findViewById(R.id.pbQuestionNumber);
			txtQuestion = (TextView) this.findViewById(R.id.txtQuestion);
			pbLoading = (ProgressBar) this.findViewById(R.id.pbLoading);
						txtQuestionNumber = (TextView) this
					.findViewById(R.id.txtQuestionNumber);

			btnNextQuestionBottom.setOnClickListener(new OnClickListener() {
				@SuppressLint("ValidFragment")
				@Override
				public void onClick(View v) {

					if (fragment.isRecording()) {
						DialogFragment df = new DialogFragment(){
							 @Override
							    public Dialog onCreateDialog(Bundle savedInstanceState) {
							        // Use the Builder class for convenient dialog construction
							        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
							        builder.setMessage(R.string.audio_currently_recording_dialog_message)
							               .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							                   @Override
											public void onClick(DialogInterface dialog, int id) {
							                	   fragment.stopRecording();

							                	   String audioFilePath = fragment.getRecordedAudioPath();
													if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY))
														Log.d(TAG, "audio file path: " + audioFilePath);
													pollContainer.addCurrentAnswer(new AnswerModel(pollContainer
																	.getCurrentQuestion().getId(), pollContainer
																	.getCurrentQuestion().getResponseType(),
																	audioFilePath));
													next();
							                   }
							               })
							               .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							                   @Override
											public void onClick(DialogInterface dialog, int id) {
							                   }
							               });
							        return builder.create();
							    }
						};
						df.show(getSupportFragmentManager(), "audioRecordingDialogFragment");
					} else if (!fragment.hasRecorded()) {
							DialogFragment df = new DialogFragment(){
								 @Override
								    public Dialog onCreateDialog(Bundle savedInstanceState) {
								        // Use the Builder class for convenient dialog construction
								        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
								        builder.setMessage(R.string.audio_please_record_audio_answer)
								               .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								                   @Override
												public void onClick(DialogInterface dialog, int id) {
								                	   dialog.dismiss();
								                }
								               });
								        return builder.create();
								    }
							};
							df.show(getSupportFragmentManager(), "audioRecordingPleaseRecordDialogFragment");
					} else {
						String audioFilePath = fragment.getRecordedAudioPath();
						if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY))
							Log.d(TAG, "audio file path: " + audioFilePath);
						pollContainer.addCurrentAnswer(new AnswerModel(pollContainer
										.getCurrentQuestion().getId(), pollContainer
										.getCurrentQuestion().getResponseType(),
										audioFilePath));
						next();
					}
				}
			});

			btnBack.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					previous();
				}
			});

			enableControls();
			(new Collector()).restoreLastAnswer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Initializes screen for question of video type
	 */
	private void init_video_question() {
		try {
			this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			final LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			final Animation a = AnimationUtils.loadAnimation(this,
					pollContainer.isForwardAnimation() ? R.anim.alpha
							: R.anim.alpha_backward);
			LinearLayout layout = (LinearLayout) li.inflate(R.layout.q_video,
					null);
			layout.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					gdt.onTouchEvent(event);
					return true;
				}
			});
			layout.setAnimation(a);
			this.setContentView(layout);
			// videoView = (VideoView) this.findViewById(R.id.VideoView);
			txtTitle = (TextView) findViewById(R.id.txtTitle);
			btnNextQuestionBottom = (Button) this
					.findViewById(R.id.btnNextQuestionBottom);
			btnBack = (Button) this.findViewById(R.id.btnBack);
			pbQuestionNumber = (ProgressBar) this
					.findViewById(R.id.pbQuestionNumber);
			txtQuestion = (TextView) this.findViewById(R.id.txtQuestion);
			wvMultimediaQuestion = (WebView) this
					.findViewById(R.id.wvMultimediaQuestion);
			wvMultimediaQuestion.setBackgroundColor(0);
			WebSettings webSettings = wvMultimediaQuestion.getSettings();
			webSettings.setJavaScriptEnabled(true);
			webSettings.setDefaultTextEncodingName("UTF-8");
			pbLoading = (ProgressBar) this.findViewById(R.id.pbLoading);
			wvMultimediaQuestion.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url,
						Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
					pbLoading.setVisibility(View.VISIBLE);
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					pbLoading.setVisibility(View.GONE);
				}
			});
			txtQuestionNumber = (TextView) this
					.findViewById(R.id.txtQuestionNumber);

			videoUri = null;
			videoPath = null;
			videoView = (VideoView) this.findViewById(R.id.VideoView);

			btnNextQuestionBottom.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (videoPath == null) {
						toastForceAnswer();
					} else {
						pollContainer.addCurrentAnswer(new AnswerModel(
								pollContainer.getCurrentQuestion().getId(),
								pollContainer.getCurrentQuestion()
										.getResponseType(), videoPath));
						next();
					}
				}
			});

			btnBack.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					previous();
				}
			});

			Button btnCapture = (Button) this.findViewById(R.id.btnCapture);
			Button btnPlay = (Button) this.findViewById(R.id.btnPlay);
			btnCapture.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent captureVideoIntent = new Intent(
							android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
					captureVideoIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION,
									ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    captureVideoIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					/*
					 * Video quality of 0 gives MMS-quality video and 1 (default) gives
					 * high-quality. On modern 1080p devices, 1 can take an extremely long time to
					 * upload, so until Google adds more quality options 0 is probably a better
					 * option. Other values are ignored.
					 *
					 * See
					 * http://developer.android.com/reference/android/provider/MediaStore.html#EXTRA_VIDEO_QUALITY
					 * for more details.
					 *
					 * Also note some SenseUI and Samsung devices will always ignore the extras
					 * here; unfortunately there's no known solution short of clientside transcoding
					 * or a separate camcorder activity; see
					 * http://stackoverflow.com/questions/4746570/htc-aria-2-2-ignores-extra-video-quality-intent
					 */
					captureVideoIntent.putExtra(android.provider.MediaStore.EXTRA_VIDEO_QUALITY, 0);
					Integer length = pollContainer.getCurrentQuestion().getLength();

					if (length != null && length > 0) {
						captureVideoIntent.putExtra(
										android.provider.MediaStore.EXTRA_DURATION_LIMIT, length);
					}

					startActivityForResult(captureVideoIntent, ConstantData.VIDEO_REQUEST);
				}
			});

			btnPlay.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (videoPath == null) {
						Toast toast = Toast.makeText(QuestionThemed.this,
								"No recorded video", Toast.LENGTH_LONG);
						toast.show();
					} else {
						videoView.refreshDrawableState();
						videoView.requestFocus();
						final MediaController mediaController = new MediaController(
								QuestionThemed.this);
						mediaController.setAnchorView(videoView);
						videoUri = Uri.withAppendedPath(
								Media.EXTERNAL_CONTENT_URI, videoPath);
						videoView.setMediaController(mediaController);
						videoView.setVideoPath(videoPath);
						videoView.start();

						// videoView.setMediaController(new
						// MediaController(QuestionThemed.this));
						// videoView.setVideoURI(videoUri);
						// videoView.requestFocus();
						// videoView.start();

						// videoView.setVideoURI(Uri.parse(videoPath));
						// videoView.start();
					}
				}
			});

			enableControls();
			(new Collector()).restoreLastAnswer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes screen for question of photo type
	 */
	private void init_single_photo_question() {
		try {
			final LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			final Animation a = AnimationUtils.loadAnimation(this,
					pollContainer.isForwardAnimation() ? R.anim.alpha
							: R.anim.alpha_backward);
			LinearLayout layout = (LinearLayout) li.inflate(
					R.layout.q_single_photo, null);
			layout.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					gdt.onTouchEvent(event);
					return true;
				}
			});
			layout.setAnimation(a);
			this.setContentView(layout);
			txtTitle = (TextView) findViewById(R.id.txtTitle);
			btnNextQuestionBottom = (Button) this
					.findViewById(R.id.btnNextQuestionBottom);
			btnBack = (Button) this.findViewById(R.id.btnBack);
			pbQuestionNumber = (ProgressBar) this
					.findViewById(R.id.pbQuestionNumber);
			txtQuestion = (TextView) this.findViewById(R.id.txtQuestion);
			wvMultimediaQuestion = (WebView) this
					.findViewById(R.id.wvMultimediaQuestion);
			wvMultimediaQuestion.setBackgroundColor(0);
			WebSettings webSettings = wvMultimediaQuestion.getSettings();
			webSettings.setJavaScriptEnabled(true);
			webSettings.setDefaultTextEncodingName("UTF-8");
			pbLoading = (ProgressBar) this.findViewById(R.id.pbLoading);
			ivGallery = (ImageView) this.findViewById(R.id.ivGallery);
			wvMultimediaQuestion.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url,
						Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
					pbLoading.setVisibility(View.VISIBLE);
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					pbLoading.setVisibility(View.GONE);
				}
			});
			txtQuestionNumber = (TextView) this
					.findViewById(R.id.txtQuestionNumber);

			Button btnCapture = (Button) this.findViewById(R.id.btnCapture);
			Button btnSelect = (Button) this.findViewById(R.id.btnSelect);

			capturedImageFilePath = null;
			btnCapture.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					String fileName = getNextName(ConstantData.RESPONSE_TYPE_PHOTO);
					ContentValues values = new ContentValues();
					values.put(MediaStore.Images.Media.TITLE, fileName);
					String state = Environment.getExternalStorageState();

					if (state.equals(Environment.MEDIA_MOUNTED)) {
						Log.d(TAG, "Storing image on external storage");
						imageUri = getContentResolver().insert(
								MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
								values);
					} else {
						Log.d(TAG, "Storing image on internal storage");
						imageUri = Toiler.storeImageAndGetUri(null, fileName,
								getApplicationContext());
					}

					if (!ConstantData.WHITE_LABEL_APP
							.isWhiteLabel(WhiteLabel.SURVEY))
						Log.d(TAG, "imageURI: " + imageUri);

					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
					startActivityForResult(intent,
							ConstantData.SINGLE_PHOTO_REQUEST);
				}
			});

			btnSelect.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(QuestionThemed.this,
							Gallery.class);
					startActivityForResult(intent,
							ConstantData.GALLERY_PHOTO_REQUEST);
				}
			});

			btnNextQuestionBottom.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!ConstantData.WHITE_LABEL_APP
							.isWhiteLabel(WhiteLabel.SURVEY))
						Log.d(TAG, "capturedImagePath: "
								+ capturedImageFilePath);
					// if (imageUri == null) {
					// toastForceAnswer();
					//
					// } else {
					pollContainer
							.addCurrentAnswer(new AnswerModel(
									pollContainer.getCurrentQuestion().getId(),
									pollContainer.getCurrentQuestion()
											.getResponseType(),
									/* imageUri */capturedImageFilePath == null ? ""
											: /* imageUri.getPath() */capturedImageFilePath));
					next();
					// }
				}
			});

			btnBack.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					previous();
				}
			});

			enableControls();
			(new Collector()).restoreLastAnswer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Shows popup if uploading was unsuccessful - different popup if number of
	 * attempts is smaller than MAX_NUMBER_ATTEMPTS_FOR_UPLOAD otherwise popup
	 * for which forces pausing survey
	 */
	private void init_uploading_failed() {
		// if (pollContainer.isOverflownumberAttempts()) {
		// AlertDialog.Builder builder = new
		// AlertDialog.Builder(QuestionThemed.this);
		// builder.setMessage(
		// getResources().getString(
		// R.string.network_connectivity_problem))
		// .setCancelable(false)
		// .setPositiveButton(R.string.ok,
		// new DialogInterface.OnClickListener() {
		// @Override
		// public void onClick(DialogInterface dialog,
		// int id) {
		// try {
		// // (new
		// // Collector()).collectLastAnswer();
		// // byte[] temp = SerializationManager
		// // .serializeObject(pollContainer);
		// // pollContainer.pause(temp);
		// moveTaskToBack(true);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// return;
		// }
		// });
		// alertDialog = builder.create();
		// alertDialog.show();
		// } else {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				QuestionThemed.this);
		builder.setMessage(
				getResources().getString(R.string.uploading_unsuccessfully))
				.setCancelable(false)
				.setPositiveButton(R.string.retry,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								if (pollContainer.isTimeForUpload()) {
									AlertDialog.Builder builder = new AlertDialog.Builder(
											QuestionThemed.this);
									builder.setMessage(
											getResources().getString(
													R.string.retry_in))
											.setCancelable(false);
									final AlertDialog alert = builder.create();
									alert.show();
									CountDownTimer counterTemp = new CountDownTimer(
											ConstantData.FIVE_SECONDS,
											ConstantData.SECOND) {
										@Override
										public void onTick(
												long millisUntilFinished) {
											alert.setMessage(getResources()
													.getString(
															R.string.retry_in)
													+ " "
													+ millisUntilFinished
													/ 1000
													+ " "
													+ getResources().getString(
															R.string.seconds));
										}

										@Override
										public void onFinish() {
											if (alert.isShowing()) {
												alert.dismiss();
												new UploadAnswerTask()
														.execute();
											}
										}
									};
									counterTemp.start();
									// new UploadAnswerTask().execute(1);
								}
								return;
							}
						})
				.setNeutralButton(R.string.Cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								pollContainer.resetNumberAttempts();
								return;
							}
						})
				.setNegativeButton(R.string.pause,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								pollContainer.resetNumberAttempts();

								try {
									(new Collector()).collectLastAnswer();
									byte[] temp = SerializationManager
											.serializeObject(pollContainer);

									pollContainer.pause(temp);
									moveTaskToBack(true);
								} catch (Exception e) {
									e.printStackTrace();
								}

								return;
							}
						});
		alertDialog = builder.create();
		alertDialog.show();
		// }
	}

	// ********************************************************************************************************

	/**
	 * Enables controls, makes visible/invisible, show description if is
	 * available
	 */
	private void enableControls() {
		try {
			txtTitle.setText(surveyTitle);
			pbQuestionNumber.setMax(pollContainer.getProgressMax());
			pbQuestionNumber.setProgress(pollContainer.getProgresPosition());
			txtQuestionNumber.setText(pollContainer.getProgressText());
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean isLast = pollContainer.isLast();
		if (pollContainer.getResponseType().equals(
				ConstantData.RESPONSE_TYPE_RATING_SCALE_LABELED)) {
			txtQuestion.setVisibility(View.GONE);
			rsRatingScale.setVisibility(View.VISIBLE);
		} else {
			txtQuestion.setText(Html.fromHtml(pollContainer
					.getCurrentQuestion().getText()));
			txtQuestion.setVisibility(isLast ? View.INVISIBLE : View.VISIBLE);
		}
		String htmlDescription = pollContainer.getCurrentQuestion()
				.getDescription();
		if (!(htmlDescription == null || htmlDescription.equals(""))) {
			String tempHtmlDescription = "<span style='"+Prefs.WV_DASHBOARD_TEXT_COLOR+";'>"
					+ htmlDescription + "</span>";
			wvMultimediaQuestion.setVisibility(View.VISIBLE);
			WebSettings webSettings = wvMultimediaQuestion.getSettings();
			webSettings.setJavaScriptEnabled(true);
			webSettings.setDefaultTextEncodingName("UTF-8");
			wvMultimediaQuestion.loadData(tempHtmlDescription, "text/html",
					"UTF-8");
		}
	}

	/**
	 * Shows toast with notification that question need to be answered if it is
	 * not already answered
	 */
	private void toastForceAnswer() {
		Toast toast = Toast.makeText(this,
				getResources().getString(R.string.prevent_next),
				Toast.LENGTH_SHORT);
		toast.show();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ConstantData.SINGLE_PHOTO_REQUEST:
			if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY))
				Log.d(TAG, "photo activity result returned");
			if (resultCode == RESULT_OK) {
				capturedImageFilePath = null;
				try {
					Log.d(TAG, "onActivityResult imageURI: " + imageUri);
					Log.d(TAG, "onActivityResult data: " + data);
					if (data != null) {
						Log.d(TAG,
								"onActivityResult data.getData(): "
										+ data.getData());
						Log.d(TAG, "onActivityResult data.getDataString(): "
								+ data.getDataString());
					}
					String[] projection = { MediaStore.Images.Media.DATA };
					Uri imageLocation = imageUri == null ? (data == null ? null
							: data.getData()) : imageUri;

					if (!ConstantData.WHITE_LABEL_APP
							.isWhiteLabel(WhiteLabel.SURVEY) && data != null) {
						Log.d(TAG,
								"onActivityResult data.getData(): "
										+ data.getData());
						Log.d(TAG, "onActivityResult data.getDataString(): "
								+ data.getDataString());
					}

					Log.d(TAG, "imagePath: " + imageLocation);

					Cursor cursor = managedQuery(imageLocation, projection,
							null, null, null);
					Log.d(TAG, "cursor: " + cursor);
					int column_index_data = cursor
							.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					cursor.moveToFirst();
					capturedImageFilePath = cursor.getString(column_index_data);
					// Log.d(TAG, "capturedImageFilePath: " +
					// capturedImageFilePath);
					File imgFile = new File(capturedImageFilePath);
					if (imgFile.exists()) {
						Log.d(TAG, "imageFile exists");
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inSampleSize = 16;
						Log.d(TAG, "imgFile.getAbsolutePath: " + imgFile.getAbsolutePath());						
						Bitmap myBitmap = BitmapFactory.decodeFile(
								imgFile.getAbsolutePath(), options);
						ivGallery.setImageBitmap(myBitmap);							
					} else {
						Log.d(TAG, "imageFile don't exist");
						btnNextQuestionBottom.performClick();
					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.w(TAG, "Failed to get image from library: " + e);

					imageUri = null;
					capturedImageFilePath = null;
					//
					// imageUri = data.getData();
					// BitmapFactory.Options options = new
					// BitmapFactory.Options();
					// options.inSampleSize = 16;
					// Bitmap myBitmap = BitmapFactory.decodeFile(
					// getRealPathFromURI(imageUri), options);
					//
					// ivGallery.setImageBitmap(myBitmap);
					// } else {
					// btnNextQuestionBottom.performClick();
					// }
					//
					// capturedImageFilePath = data.getData().toString();
					// Log.d(TAG, "image path on internal storage: " +
					// capturedImageFilePath);
				}
			} else {
				imageUri = null;
				capturedImageFilePath = null;
				Toast toast = Toast.makeText(QuestionThemed.this,
						R.string.image_capturing_failed, Toast.LENGTH_LONG);
				toast.show();
			}
			break;
		case ConstantData.VIDEO_REQUEST:
			if (resultCode == RESULT_OK) {
				try {
					videoUri = data.getData();
					videoPath = getRealPathFromURI(videoUri);
					// videoView.setVisibility(View.VISIBLE);

					// ***************************************************************************************
					// Bitmap thumb = ThumbnailUtils.createVideoThumbnail(
					// videoPath, MediaStore.Images.Thumbnails.MINI_KIND);
					// videoView.setBackgroundDrawable(new
					// BitmapDrawable(getResources(), thumb));
					// ***************************************************************************************

				} catch (/* IO */Exception e) {
					e.printStackTrace();
				}
			} else {
				videoPath = null;
				videoUri = null;
				AlertDialog.Builder builder = new AlertDialog.Builder(
						QuestionThemed.this);
				builder.setMessage("Video capturing failed.")
						.setCancelable(false)
						.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										return;
									}
								});
				AlertDialog alert = builder.create();
				alert.show();
			}
			break;

		case ConstantData.GALLERY_PHOTO_REQUEST:
			if (resultCode == RESULT_OK) {
				capturedImageFilePath = data.getStringExtra("image_path");
				File imgFile = new File(capturedImageFilePath);
				if (imgFile.exists()) {
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inSampleSize = 16;
					Bitmap myBitmap = BitmapFactory.decodeFile(
							imgFile.getAbsolutePath(), options);
					ivGallery.setImageBitmap(myBitmap);
				}
			}
			break;
		}
	}

	/**
	 * Shows popup on pressed back button with option to pause survey - save
	 * answers and other necessary informations in db or to abandon survey -
	 * just left and it is not available anymore if user is not unrestricted
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					QuestionThemed.this);
			builder.setMessage(R.string.pause_abandon_continue)
					.setCancelable(false)
					.setPositiveButton(R.string.pause,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									try {
										(new Collector()).collectLastAnswer();
										byte[] temp = SerializationManager
												.serializeObject(pollContainer);

										pollContainer.pause(temp);
										moveTaskToBack(true);
									} catch (Exception e) {
										e.printStackTrace();
									}

									// finish();
									return;
								}
							})
					.setNegativeButton(R.string.abandon,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									(new AbandonTask()).execute();
									return;
								}
							})
					.setNeutralButton(R.string.continue_survey,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									return;
								}
							});
			alertDialog = builder.create();
			alertDialog.show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Generates appropriate name based on type
	 *
	 * @param type
	 * @return generated name - String
	 */
	private String getNextName(String type) {
		// Check is this safe way to get unique names for images inside Survey
		// folders
		if (type.equals(ConstantData.RESPONSE_TYPE_VIDEO)) {
			return "survey_dot_com_video_" + System.currentTimeMillis()
					+ ".3gp";
		} else {
			return "survey_dot_com_image_" + System.currentTimeMillis()
					+ ".jpeg";
		}
	}

	/**
	 *
	 */
	public void next() {
		pollContainer.nextQuestion();
		if (pollContainer.isTimeForUpload()) {
			new UploadAnswerTask().execute();
		} else {
			initialize();
		}
	}

	public void previous() {
		if (pollContainer.getCursor() > 0) {
			(new Collector()).collectLastAnswer();
			pollContainer.previousQuestion();
			initialize();
		} else {
			Toast toast = Toast.makeText(QuestionThemed.this,
					getString(R.string.cannot_access_prevoius_section),
					Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	// ************************************** AsyncTasks
	// ************************************************************************

	/**
	 * UploadAnswerTask uploads in background answers to server if it fails call
	 * of function initialize(); in postExecute will take care of that
	 *
	 * @author dominum
	 */
	private class UploadAnswerTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			try {
				pd = ProgressDialog.show(QuestionThemed.this, getResources()
						.getString(R.string.uploading_answers), getResources()
						.getString(R.string.please_wait), true, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onProgressUpdate(Void... progress) {
		}

		@Override
		protected void onPostExecute(Void result) {
			try {
				initialize();
				if (pd != null && pd.isShowing()) {
					pd.dismiss();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			pollContainer.upload();
			return null;
		}
	}

	/**
	 * AbandonTask in background delete all records from database ( just one
	 * survey can be in paused state so it is ok to delete all )
	 *
	 * @author dominum
	 */
	private class AbandonTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			try {
				pd = ProgressDialog.show(QuestionThemed.this, getResources()
						.getString(R.string.abandoning_survey), getResources()
						.getString(R.string.please_wait), true, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Boolean result = true;
			try {
				SerializationHelper dbHelper = new SerializationHelper(
						QuestionThemed.this);
				dbHelper.deleteAll();
				if (dbHelper.getSQLiteDatabase().isOpen()) {
					dbHelper.getSQLiteDatabase().close();
				}
			} catch (Exception e) {
				result = false;
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (pd != null && pd.isShowing()) {
				pd.dismiss();
			}
			QuestionThemed.this.finish();
		}
	}

	// ***********************************************************************************************************************

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			AppContainer app = new AppContainer(QuestionThemed.this);
			app.location = location;
			lm.removeUpdates(this);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
		try {
			lm.removeUpdates(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		criteria.setSpeedRequired(true);
		provider = lm.getBestProvider(criteria, true);
		if (provider != null && provider.length() > 0) {
			lm.requestLocationUpdates(provider, 5, 5, this);
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	private class GestureListener extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				btnNextQuestionBottom.performClick();
				return true; // Right to left
			}
			return false;
		}
	}

	/**
	 * Generates path to file based on URI
	 *
	 * @param contentUri
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	/**
	 * Collector class used for collecting last answer ( when user wanto to
	 * pause app so in next app boot it can restore user inputs on screen
	 *
	 * @author dominum
	 */
	private class Collector {

		/**
		 * In a case when you want to pause app, last answer is collected and
		 * pushed to pollContainer no matter if it is valid or no, so when you
		 * start app next time you get that answer filled into filed or ratings
		 * scale populated
		 */
		public void collectLastAnswer() {
			try {
				String responseType = pollContainer
						.getResponseTypeLastQuestion();
				String questionId = pollContainer.getQuestionIdLastQuestion();

				if (questionId != null && responseType != null) {
					if (responseType
							.equals(ConstantData.RESPONSE_TYPE_FREE_TEXT)
							|| responseType
									.equals(ConstantData.RESPONSE_TYPE_NUMERIC_DECIMAL)
							|| responseType
									.equals(ConstantData.RESPONSE_TYPE_NUMERIC_INTEGER)
							|| responseType
									.equals(ConstantData.RESPONSE_TYPE_OPEN_ENDED_TEXT)) {
						String text = etContent.getText().toString();
						pollContainer.addCurrentAnswer(new AnswerModel(
								questionId, responseType, text));

					} else if (responseType
							.equals(ConstantData.RESPONSE_TYPE_SINGLE_SELECT)
							&& whChoices.isAnswered()) {
						pollContainer.addCurrentAnswer(new AnswerModel(
								questionId, responseType, whChoices
										.getSelectedAnswer()));

					} else if (responseType
							.equals(ConstantData.RESPONSE_TYPE_MULTIPLE_SELECT)
							&& whChoices.isAnswered()) {
						pollContainer
								.addAnswers(whChoices.getSelectedAnswers());

					} else if (responseType
							.equals(ConstantData.RESPONSE_TYPE_PHOTO)
							&& (capturedImageFilePath != null)) {

						pollContainer.addCurrentAnswer(new AnswerModel(
								questionId, responseType,
								capturedImageFilePath == null ? ""
										: capturedImageFilePath));

					} else if (responseType
							.equals(ConstantData.RESPONSE_TYPE_VIDEO)
							&& (videoPath != null)) {

						pollContainer.addCurrentAnswer(new AnswerModel(
								questionId, responseType, videoPath));

					} else if (responseType.equals(ConstantData.RESPONSE_TYPE_AUDIO)) {
						if (mAudioFragment != null && mAudioFragment.getRecordedAudioPath() != null) {
							String audioPath = mAudioFragment.getRecordedAudioPath();
							pollContainer.addCurrentAnswer(new AnswerModel(questionId, responseType,
											audioPath));
						}

					} else if (responseType
							.equals(ConstantData.RESPONSE_TYPE_RATING_SCALE)) {
						pollContainer.addAnswers(rlRatings.getAnswers());

					} else if (responseType
							.equals(ConstantData.RESPONSE_TYPE_RATING_SCALE_LABELED)) {
						pollContainer.addAnswers(rsRatingScale.getAnswers());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * In a case if app was paused restore last answer to screen
		 */
		public void restoreLastAnswer() {
			try {
				String responseType = pollContainer.getResponseType();
				String questionId = pollContainer.getCurrentQuestion().getId();
				List<AnswerModel> temp = pollContainer.getAnswerLastQuestion();
				if (questionId != null && responseType != null && temp != null
						&& temp.size() > 0) {
					if (responseType
							.equals(ConstantData.RESPONSE_TYPE_FREE_TEXT)
							|| responseType
									.equals(ConstantData.RESPONSE_TYPE_NUMERIC_DECIMAL)
							|| responseType
									.equals(ConstantData.RESPONSE_TYPE_NUMERIC_INTEGER)
							|| responseType
									.equals(ConstantData.RESPONSE_TYPE_OPEN_ENDED_TEXT)) {

						if (temp.size() > 0) {
							etContent.setText(temp.get(0).getAnswer());
						}
					} else if (responseType
							.equals(ConstantData.RESPONSE_TYPE_SINGLE_SELECT)) {
						whChoices.setSelectedAnswers(pollContainer
								.getAnswerLastQuestion());
					} else if (responseType
							.equals(ConstantData.RESPONSE_TYPE_MULTIPLE_SELECT)) {
						whChoices.setSelectedAnswers(pollContainer
								.getAnswerLastQuestion());
					} else if (responseType
							.equals(ConstantData.RESPONSE_TYPE_PHOTO)) {
						String path = temp.get(0).getAnswer();
						File imgFile = new File(path);
						if (imgFile.exists()) {
							try {
								BitmapFactory.Options options = new BitmapFactory.Options();
								options.inSampleSize = 16;
								Bitmap myBitmap = BitmapFactory.decodeFile(
										imgFile.getAbsolutePath(), options);
								ivGallery.setImageBitmap(myBitmap);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} else if (responseType
							.equals(ConstantData.RESPONSE_TYPE_VIDEO)) {
						try {
							videoPath = temp.get(0).getAnswer();
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (responseType.equals(ConstantData.RESPONSE_TYPE_AUDIO)) {
						try {
							String audioPath = temp.get(0).getAnswer();
							if (mAudioFragment != null && audioPath != null && audioPath != "") {
								Log.d(TAG, "Setting audio path back to "+audioPath);
								mAudioFragment.setExistingRecordingFileName(audioPath);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (responseType
							.equals(ConstantData.RESPONSE_TYPE_RATING_SCALE)) {
						// pollContainer.addAnswers(rlRatings.getAnswers());
						rlRatings.setAnswers(temp);
					} else if (responseType
							.equals(ConstantData.RESPONSE_TYPE_RATING_SCALE_LABELED)) {
						// pollContainer.addAnswers(rsRatingScale.getAnswers());
						rsRatingScale.setAnswers(temp);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.
		Log.i(TAG, "onSaveInstanceState imageUri: " + imageUri);
		if (imageUri != null) {
			savedInstanceState.putString("imageUri", imageUri.toString());
		}
		// etc.
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Restore UI state from the savedInstanceState.
		// This bundle has also been passed to onCreate.
		Log.i(TAG,"onRestoreInstanceState imageUri: "+ savedInstanceState.getString("imageUri"));
		String imageUriString = savedInstanceState.getString("imageUri");
		if (imageUriString != null) {
			imageUri = Uri.parse(imageUriString);
		}
	}

}
