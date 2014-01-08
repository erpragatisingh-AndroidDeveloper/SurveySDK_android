package com.survey.android.view.themed;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.survey.android.R;
import com.survey.android.model.Prefs;
import com.survey.android.model.ResponseModel;
import com.survey.android.session.Configuration;
import com.survey.android.util.ConstantData;
import com.survey.android.util.Log;
import com.survey.android.util.Toiler;
import com.survey.android.util.WhiteLabel;
import com.survey.android.view.LocalizedFragmentActivity;
import com.survey.android.view.Question;
import com.survey.android.webclient.RestClient;
import com.survey.android.widget.Widget;

//*************************************************************************************************

public abstract class NotificationThemed extends LocalizedFragmentActivity {
	public static final String TAG = "Notification";

	private static int NOT_VIA_C2DM = -1; // indicate that user get here from
											// survey list, not by notification

	protected Button imbStartSurvey; // click on button loads first section
	protected TextView txtTitle; // shows title of survey
	protected Button btnBack;
	protected TextView txtReward; // shows reward for survey
	protected TextView txtSurveyTitle;
	private WebView wvDescription; // shows description for survey
	private String surveyTitle; // holds value of survey title
	private String surveyId; // holds value of survey id
	private int question_count; // holds value of question count
	private String token; // holds value of user token
	private JSONObject surveyInfo;
	private LinearLayout reward;			

	// ********************************************************************
	@SuppressLint("ValidFragment")
	private static class NotificationDialogFragment extends DialogFragment{
		
		public static final int DIALOG_NO_INTERNET_CONNECTION = 0;
		public static final int DIALOG_LOADING = 1;
		protected static final int DIALOG_START_SURVEY_LOG_IN = 2;
		
		public static NotificationDialogFragment newInstance(String message, int tag) {
			NotificationDialogFragment frag = new NotificationDialogFragment();
	        Bundle args = new Bundle();
	        args.putString("message", message);
	        args.putInt("tag", tag);
	        frag.setArguments(args);
	        return frag;
	    }
		
		public static NotificationDialogFragment newInstance(String title, String message, int tag) {
			NotificationDialogFragment frag = newInstance(message, tag);
			frag.getArguments().putString("title", title);
	        return frag;
	    }

	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	    	Dialog dialog = null;
	    	Bundle args = getArguments();
	    	String message = args.getString("message");
	    	int tag = args.getInt("tag");
	    	AlertDialog.Builder builder = new AlertDialog.Builder(
	    			getActivity());
	    	switch (tag) {
	    	case DIALOG_NO_INTERNET_CONNECTION:
	    		builder
	    		.setMessage(message)
	    		.setPositiveButton(R.string.ok,
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int id) {
												((NotificationThemed)getActivity()).retrieveSurveyInfoFromServer();
												return;
											}
										})
				.setNegativeButton(R.string.Cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(
									DialogInterface dialog,
									int id) {
								getActivity().finish();
								return;
							}
						});
	    		dialog = builder.create();
	    		break;
	    		case DIALOG_START_SURVEY_LOG_IN:
		    		builder
		    		.setTitle(args.getString("title"))
		    		.setPositiveButton(
		    				R.string.ok,
		    				new DialogInterface.OnClickListener() {
		    					@Override
		    					public void onClick(
		    							DialogInterface dialog,
		    							int id) {
		    						return;
		    					}
		    				});
		    		dialog = builder.create();
		    		break;
	    	case DIALOG_LOADING:
	    		 dialog = new ProgressDialog(getActivity());
	             ((ProgressDialog) dialog).setMessage(message);
	             ((ProgressDialog) dialog).setTitle(args.getString("title"));
	             break;
	    	default:
	    		dialog = null;
	        }
	        return dialog;
	    }
	}
	
		@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notification);
		
		if (Prefs.HIDE_MONEY_ELEMENTS)
			findViewById(R.id.reward).setVisibility(View.GONE);
		
		if(!Configuration.isDashBoardAvailable()){
			//Earnings layout
			findViewById(R.id.reward).setVisibility(View.GONE);
		}

		int notification_id = getIntent().getIntExtra("notification_id",NOT_VIA_C2DM);
		
		if (notification_id != NOT_VIA_C2DM) {
			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
			mNotificationManager.cancel(notification_id);
			
			try {
				Intent i = new Intent(this, Widget.class);
				i.setAction("android.appwidget.action.APPWIDGET_UPDATE");
				this.sendBroadcast(i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		initUI();
		if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY))
			customizeTheme();
	}
		
	protected abstract void customizeTheme();

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
		imbStartSurvey = (Button) findViewById(R.id.imbStartSurvey);
		txtTitle = (TextView) findViewById(R.id.txtTitle);
		btnBack = (Button) findViewById(R.id.btnBack);
		wvDescription = (WebView) findViewById(R.id.wvDescription);
		wvDescription.setBackgroundColor(0);
		WebSettings settings = wvDescription.getSettings();
		settings.setDefaultTextEncodingName("UTF-8");
		txtReward = (TextView) findViewById(R.id.txtReward);
		txtSurveyTitle = (TextView) findViewById(R.id.txtSurveyTitle);
		txtTitle.setText(R.string.survey_info);
		txtSurveyTitle.setTextColor(Color.BLACK);
		// Find LinearLayout and make it clickable
		reward = (LinearLayout) findViewById(R.id.reward);
		reward.setClickable(true);				
	
		btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});


		try {
			surveyId = getIntent()
					.getStringExtra("survey_id");
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(NotificationThemed.this);
			token = prefs.getString("token", null);
			imbStartSurvey.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						// for c2dm
						if (token == null) {
							DialogFragment newFragment = NotificationDialogFragment.newInstance(
									getString(R.string.start_survey_log_in), "",NotificationDialogFragment.DIALOG_START_SURVEY_LOG_IN);
							if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY)) {
								newFragment.setCancelable(true);
							} else {
								newFragment.setCancelable(false);
							}
						    newFragment.show(getSupportFragmentManager(), "dialog");
						} else if (!Toiler.isNetworkAvailable(NotificationThemed.this)) {
							DialogFragment newFragment = NotificationDialogFragment.newInstance(
						            getString(R.string.no_internet_connection_detected), NotificationDialogFragment.DIALOG_NO_INTERNET_CONNECTION);
							if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY)) {
								newFragment.setCancelable(true);
							} else {
								newFragment.setCancelable(false);
							}
						    newFragment.show(getSupportFragmentManager(), "dialog");
						} else {
							(new ResponseIdTask()).execute(surveyId);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			// handle click for LinearLayout so that user clicking on this
			// can still start the survey
			reward.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						// for c2dm
						if (token == null) {
							DialogFragment newFragment = NotificationDialogFragment.newInstance(
									getString(R.string.start_survey_log_in), "",NotificationDialogFragment.DIALOG_START_SURVEY_LOG_IN);
							if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY)) {
								newFragment.setCancelable(true);
							} else {
								newFragment.setCancelable(false);
							}
						    newFragment.show(getSupportFragmentManager(), "dialog");
						} else if (!Toiler
								.isNetworkAvailable(NotificationThemed.this)) {
							DialogFragment newFragment = NotificationDialogFragment.newInstance(
						            getString(R.string.no_internet_connection_detected), NotificationDialogFragment.DIALOG_NO_INTERNET_CONNECTION);
							if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY)) {
								newFragment.setCancelable(true);
							} else {
								newFragment.setCancelable(false);
							}
						    newFragment.show(getSupportFragmentManager(), "dialog");
						} else {
							(new ResponseIdTask()).execute(surveyId);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
						
			retrieveSurveyInfoFromServer();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

		
	protected void retrieveSurveyInfoFromServer() {
		(new LoadingDataTask()).execute();
	}

	@SuppressLint("Recycle")
	@Override
	protected void onPause() {
		super.onPause();

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}

		ft.addToBackStack(null);
	}
		
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * Loads necessary data for showing
	 */
	private class LoadingDataTask extends AsyncTask<Void, Void, JSONObject> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			DialogFragment newFragment = NotificationDialogFragment.newInstance(
		            getString(R.string.loading), getString(R.string.please_wait), NotificationDialogFragment.DIALOG_LOADING);
			if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY)) {
				newFragment.setCancelable(true);
			} else {
				newFragment.setCancelable(false);
			}
		    newFragment.show(getSupportFragmentManager(), "dialog");
		}

		@Override
		protected JSONObject doInBackground(Void... params) {

			try {
				surveyInfo = RestClient.getSurveyById(token, surveyId);
			} catch (Exception e) {
				surveyInfo = null;
				if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY)) {
					Log.e(TAG, "Exception: " + e);
				} else {
					e.printStackTrace();
				}
			}
			
			return surveyInfo;
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(JSONObject result) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
			if (prev != null) {
				ft.remove(prev);
			}

			if (result != null) {
				try {
					if (surveyInfo.has("title")) {
						surveyTitle = surveyInfo.getString("title");
						txtSurveyTitle.setText(surveyTitle);
					}

					if (surveyInfo.has("description")) {
						String tempDescription = "<span style='"
								+ "#" + getResources().getString(R.color.text).substring(3)
								+ ";'>"
								+ surveyInfo.getString("description")
								+ "</span>";
						wvDescription.loadData(tempDescription, "text/html",
								"UTF-8");
					}

					if (surveyInfo.has("reward_cents")) {
						int reward = ((int) surveyInfo.getInt("reward_cents")); 
						txtReward.setText(String.valueOf((Integer.valueOf(reward))));
						question_count = surveyInfo.getInt("question_count");
					}
					
					// check if this survey is "message" type
					if (surveyInfo.has("is_msg") && surveyInfo.getBoolean("is_msg")) {
						// hide Start button
						imbStartSurvey.setVisibility(View.GONE);
						// create survey response to record survey as viewed
						(new RecordViewTask()).execute(surveyId);
					}					
				} catch (Exception e) {
					Log.e(TAG, "Exception: " + e);
				}
				
				//remove dialog fragment
				ft.commit();
				
			} else {
				DialogFragment newFragment;
				newFragment = NotificationDialogFragment.newInstance(getString(R.string.no_internet_connection_detected), NotificationDialogFragment.DIALOG_NO_INTERNET_CONNECTION);				
				if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY)) {
					newFragment.setCancelable(true);
				} else {
					newFragment.setCancelable(false);
				}
				newFragment.show(ft, "dialog");
			}

			super.onPostExecute(result);
		}

	}

	private class ResponseIdTask extends AsyncTask<String, Void, String> {
		@Override
		protected void onPreExecute() {
			DialogFragment newFragment = NotificationDialogFragment.newInstance(
		            getString(R.string.starting), getString(R.string.please_wait), NotificationDialogFragment.DIALOG_LOADING);
			newFragment.setCancelable(false);
		    newFragment.show(getSupportFragmentManager(), "dialog");
		}

		@Override
		protected String doInBackground(String... surveyId) {
			String responseId = null;
			try {
				responseId = ResponseModel.remote(NotificationThemed.this,
						surveyId[0]).getId();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return responseId;
		}

		@Override
		protected void onPostExecute(String responseId) {
			
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
			if (prev != null) {
				ft.remove(prev);
			}
			
			ft.commit();
			
			if (responseId != null) {

				NotificationThemed.this.startActivity(new Intent(NotificationThemed.this,
						Question.class).putExtra("response_id", responseId)
						.putExtra("question_count", question_count)
						.putExtra("survey_title", surveyTitle)
						.putExtra("survey_id", surveyId));
				NotificationThemed.this.finish();
			}
		}
	}

	private class RecordViewTask extends AsyncTask<String, Void, String> {
		@Override
		protected void onPreExecute() {
		}

		@Override
		protected String doInBackground(String... surveyId) {
			String responseId = null;
			try {
				responseId = ResponseModel.remote(NotificationThemed.this,
						surveyId[0]).getId();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return responseId;
		}

		@Override
		protected void onPostExecute(String responseId) {
			if (responseId != null) {

//				try {
//					Map<String, String> map = MapBuilder.createEvent(
//							ConstantData.GOOGLE_ANALYTICS_SURVEY, // Category
//							ConstantData.CLIENT_ACTION_START_SURVEY, // Action
//							ConstantData.GOOGLE_ANALYTICS_SURVEY, // Label
//							(long)0 // Value
//						).build();
//					tracker.send(map);						
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
			}
		}
	}	
}
