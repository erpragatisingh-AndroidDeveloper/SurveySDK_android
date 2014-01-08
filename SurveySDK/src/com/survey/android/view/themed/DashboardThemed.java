package com.survey.android.view.themed;

import java.io.IOException;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.survey.android.R;
import com.survey.android.db.SerializationHelper;
import com.survey.android.model.Prefs;
import com.survey.android.services.GeoSurveyPollService;
import com.survey.android.session.Configuration;
import com.survey.android.session.Session;
import com.survey.android.util.ConstantData;
import com.survey.android.util.Toiler;
import com.survey.android.util.WhiteLabel;
import com.survey.android.view.LocalizedFragmentActivity;
import com.survey.android.view.Question;
import com.survey.android.view.Survey;
import com.survey.android.webclient.RestClient;

//*************************************************************************************************

public abstract class DashboardThemed extends LocalizedFragmentActivity {

	private static final String TAG = "DASHBOARD";
	private Button btnV;
	protected Button mySurveysButton;
	protected Button logOutButton;
	protected TextView txtCurrentBalance;
	protected TextView txtCurrentEarnings;
	protected TextView txtSurveysCompleted;
	protected TextView txtSurveyInProgress;
	private ProgressDialog pd;
	private JSONObject appInfo;
	private String currentBalanceStored;
	private String careerEarningsStored;
	private String pendingRewardsStored;
	private int surveysCompletedStored;
	private int surveysAvailableStored;
	@SuppressWarnings("unused")
	private Integer numberAttempts;
	private LinearLayout linearLayout1;	
	private LinearLayout LinearLayout03;	

	// ********************************************************************
	private AlertDialog alert;
	private Runnable alertRunnable;
	private Handler handler;

	// ********************************************************************

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);
		if(Configuration.isGeoPushEnabled()) {
			if (!Toiler.isServiceRunning(this, GeoSurveyPollService.class.getName())) { 
				startService(new Intent(this, GeoSurveyPollService.class));			
			}			
		}

		if (Prefs.HIDE_MONEY_ELEMENTS)
			findViewById(R.id.linearLayout1).setVisibility(View.GONE);
		
		//Configuration configuration = new Configuration(getApplicationContext());
		
		if(!Configuration.isDashBoardAvailable()){
			//Earnings layout
			findViewById(R.id.linearLayout1).setVisibility(View.GONE);
		}
		
		if(Configuration.isShowLogoutButton()){
			findViewById(R.id.button2).setVisibility(View.VISIBLE);
		}else{
			findViewById(R.id.button2).setVisibility(View.GONE);
		}

		handler = new Handler();
		alertRunnable = null;

		numberAttempts = 0;
		(new DBTask()).execute(0);
		
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
		txtCurrentBalance = (TextView) findViewById(R.id.txtCurrentBalance);
		txtCurrentEarnings = (TextView) findViewById(R.id.txtCurrentEarnings);
		txtSurveysCompleted = (TextView) findViewById(R.id.txtSurveysCompleted);
		txtSurveyInProgress = (TextView) findViewById(R.id.txtSurveyInProgress);
		btnV = (Button) findViewById(R.id.btnV);
		mySurveysButton = (Button) findViewById(R.id.button1);
		logOutButton = (Button) findViewById(R.id.button2);
		logOutButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Session session = new Session(DashboardThemed.this);
				session.logOut();
				finish();
			}
		});
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(DashboardThemed.this);
		currentBalanceStored = prefs.getString(
				DashboardThemed.this.getString(R.string.current_balance_stored), "0");
		careerEarningsStored = prefs.getString(
				DashboardThemed.this.getString(R.string.career_earnings_stored), "0");
		pendingRewardsStored = prefs.getString(
				DashboardThemed.this.getString(R.string.pending_rewards_stored), "0");
		surveysCompletedStored = prefs.getInt(
				DashboardThemed.this.getString(R.string.surveys_completed_stored), 0);
		surveysAvailableStored = prefs.getInt(
				DashboardThemed.this.getString(R.string.surveys_available_stored), 0);
		// Find LinearLayout and make it clickable		
		linearLayout1 = (LinearLayout) findViewById(R.id.linearLayout1);
		linearLayout1.setClickable(true);
		// Find LinearLayout and make it clickable		
		LinearLayout03 = (LinearLayout) findViewById(R.id.LinearLayout03);
		LinearLayout03.setClickable(true);
		
		updateUI();

		// handle click for LinearLayout so that user clicking on this
		// can still get to survey list	
		linearLayout1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(DashboardThemed.this, Survey.class));
			}
		});
		
		// handle click for LinearLayout so that user clicking on this
		// can still get to survey list		
		LinearLayout03.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(DashboardThemed.this, Survey.class));
			}
		});
		
		mySurveysButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(DashboardThemed.this, Survey.class));
				// handler.sendEmptyMessage(0);
			}
		});
		
		btnV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
			}
		});
		btnV.setVisibility(View.GONE);
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			(new BackgroundDataTask()).execute();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			(new JSONParseTask()).execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Toiler.hideTitleProgressBar(DashboardThemed.this);
	}

	@Override
	protected void onDestroy() {
		if (alert != null && alert.isShowing()) {
			alert.dismiss();
		}
		try {
			if (alertRunnable != null)
				handler.removeCallbacks(alertRunnable);
		} catch (Exception e) {
			e.printStackTrace();
		}

		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case Activity.RESULT_OK:

			break;
		case Activity.RESULT_CANCELED:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(DashboardThemed.this);
			String tempToken = prefs.getString(
					DashboardThemed.this.getString(R.string.token), null);
			if (tempToken != null) {
//				AlertDialog.Builder builder = new AlertDialog.Builder(
//						RegistrationThemed.this);
//				builder.setMessage(R.string.you_are_already_logged_in)
//						.setCancelable(false)
//						.setPositiveButton(R.string.ok,
//								new DialogInterface.OnClickListener() {
//									@Override
//									public void onClick(DialogInterface dialog,
//											int id) {
//										return;
//									}
//								});
//				AlertDialog alert = builder.create();
//				alert.show();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}	

	private void enableControls(boolean visible) {
	}

	private void updateUI() {
		int currentBalancependingRewards;
		try {
			currentBalancependingRewards = Integer.parseInt(pendingRewardsStored)
							+ Integer.parseInt(currentBalanceStored);
		} catch (Exception e) {
			currentBalancependingRewards = 0;
			e.printStackTrace();
		}
		txtCurrentBalance.setText(String.valueOf(currentBalancependingRewards));
		int careerEarning;
		try {
			careerEarning = Integer.parseInt(careerEarningsStored);
		} catch (Exception e) {
			careerEarning = 0;
			e.printStackTrace();
		}
		txtCurrentEarnings.setText(String.valueOf(careerEarning));
		txtSurveysCompleted.setText("" + surveysCompletedStored);
		txtSurveyInProgress.setText("" + surveysAvailableStored);
	}

	private void appUpdate(JSONObject appInfo) {
		try {
			String version = appInfo.getString("version");
//			String release_date = appInfo.getString("release_date");
//			String link = appInfo.getString("url");
			String versionName = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName;
//			int versionCode = getPackageManager().getPackageInfo(
//					getPackageName(), 0).versionCode;

			int up = compare(version, versionName);
			if (up > 0) {
				btnV.setVisibility(View.VISIBLE);
			} else {
				btnV.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			btnV.setVisibility(View.GONE);
			e.printStackTrace();
		}
	}

	private int compare(String v1, String v2) {
		String s1 = normalisedVersion(v1);
		String s2 = normalisedVersion(v2);
		int cmp = s1.compareTo(s2);
		return cmp;
	}

	private String normalisedVersion(String version) {
		return normalisedVersion(version, ".", 4);
	}

	private String normalisedVersion(String version, String sep, int maxWidth) {
		String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
		StringBuilder sb = new StringBuilder();
		for (String s : split) {
			sb.append(String.format("%" + maxWidth + 's', s));
		}
		return sb.toString();
	}

	private class JSONParseTask extends AsyncTask<Void, Void, JSONObject> {
		@Override
		protected JSONObject doInBackground(Void... urls) {
			appInfo = null;
			try {
				appInfo = RestClient.getAppVersionInfo();

			} catch (Exception e) {
				e.printStackTrace();
			}
			return appInfo;
		}

		@Override
		protected void onProgressUpdate(Void... progress) {
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			if (result != null) {
				try {
					appUpdate(result);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class DBTask extends AsyncTask<Integer, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			alertRunnable = new Runnable() {

				public void run() {
					pd = ProgressDialog.show(DashboardThemed.this, getResources()
							.getString(R.string.loading), getResources()
							.getString(R.string.please_wait), true, false);
				}
			};
			handler.postDelayed(alertRunnable, 0);
		}

		@Override
		protected Boolean doInBackground(Integer... number) {
			numberAttempts = number[0] + 1;

			// paused can be member of Dashboard and can be cached to save time
			Boolean paused = false;
			try {
				// cached = false;
				SerializationHelper dbHelper = new SerializationHelper(
						DashboardThemed.this);

				paused = dbHelper.isPaused();
				if (dbHelper.getSQLiteDatabase().isOpen()) {
					dbHelper.getSQLiteDatabase().close();
				}
				if (getIntent().getBooleanExtra("EXIT", false)) {
					finish();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			return paused;
		}

		@Override
		protected void onProgressUpdate(Void... progress) {
		}

		@Override
		protected void onPostExecute(Boolean paused) {
			if (pd.isShowing()) {
				pd.dismiss();
			}
			if (paused) {
				// if (Toiler.isNetworkAvailable(DashboardThemed.this)) {

				Intent i = new Intent(DashboardThemed.this, Question.class);
				i.putExtra("is_paused", paused);
				startActivity(i);
			}
		}
	}

	// ************************************************************************************************************
	private class BackgroundDataTask extends AsyncTask<Void, Void, JSONObject> {

		@Override
		protected void onPreExecute() {
			Toiler.showTitleProgressBar(DashboardThemed.this);
		}

		@Override
		protected JSONObject doInBackground(Void... urls) {

			JSONObject result = null;
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(DashboardThemed.this);
			String userToken = prefs.getString(
					DashboardThemed.this.getString(R.string.token), null);
			String userId;
			// if (cached == false) {
			try {
				if (userToken != null) {
					JSONObject tempUserIdJSON = RestClient
							.getUserIdByToken(userToken);
					if (tempUserIdJSON != null && tempUserIdJSON.has("user_id")) {
						userId = tempUserIdJSON.getString("user_id");
						result = RestClient.getEarnings(userToken, userId);
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// }
			return result;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			if (result != null) {
				try {
//					DecimalFormat twoDForm = new DecimalFormat("####.##");
//					twoDForm.setMinimumFractionDigits(2);

					String tempCurrentBalance = "0";
					String tempCareerEarning = "0";
					String tempPendingRewards = "0";
					int tempSurveysCompleted = 0;
					int tempSurveysAvailable = 0;

					if (result.has("current_balance")) {
						tempCurrentBalance = result.getString("current_balance");
						tempCurrentBalance = String.valueOf((int)(Double.valueOf(tempCurrentBalance)*100));
					}
					if (result.has("career_earnings")) {
						tempCareerEarning = result.getString("career_earnings");
						tempCareerEarning = String.valueOf((int)(Double.valueOf(tempCareerEarning)*100));
					}
					if (result.has("pending_rewards")) {
						tempPendingRewards = result.getString("pending_rewards");
						tempPendingRewards = String.valueOf((int)(Double.valueOf(tempPendingRewards)*100));
					}

					if (result.has("surveys_completed")) {
						tempSurveysCompleted = result
								.getInt("surveys_completed");
					}
					if (result.has("surveys_available")) {
						tempSurveysAvailable = result
								.getInt("surveys_available");
					}

					if (!(tempCurrentBalance.equals(currentBalanceStored)
							&& tempCareerEarning.equals(careerEarningsStored)
							&& tempPendingRewards.equals(pendingRewardsStored)
							&& tempSurveysCompleted == surveysCompletedStored && tempSurveysAvailable == surveysAvailableStored)) {

						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(DashboardThemed.this);
						Editor edit = prefs.edit();
						if (!tempCurrentBalance.equals(currentBalanceStored)) {
							currentBalanceStored = tempCurrentBalance;
							edit.putString(
									DashboardThemed.this
											.getString(R.string.current_balance_stored),
									currentBalanceStored);
						}
						if (!tempCareerEarning.equals(careerEarningsStored)) {
							careerEarningsStored = tempCareerEarning;
							edit.putString(
									DashboardThemed.this
											.getString(R.string.career_earnings_stored),
									careerEarningsStored);
						}
						if (!tempPendingRewards.equals(pendingRewardsStored)) {
							pendingRewardsStored = tempPendingRewards;
							edit.putString(
									DashboardThemed.this
											.getString(R.string.pending_rewards_stored),
									pendingRewardsStored);
						}
						if (tempSurveysCompleted != surveysCompletedStored) {
							surveysCompletedStored = tempSurveysCompleted;
							edit.putInt(
									DashboardThemed.this
											.getString(R.string.surveys_completed_stored),
									surveysCompletedStored);
						}
						if (tempSurveysAvailable != surveysAvailableStored) {
							surveysAvailableStored = tempSurveysAvailable;
							edit.putInt(
									DashboardThemed.this
											.getString(R.string.surveys_available_stored),
									surveysAvailableStored);
						}
						edit.commit();
						updateUI();
					}
					enableControls(true);
					// cached = true;
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			} 
			Toiler.hideTitleProgressBar(DashboardThemed.this);
		}
	}

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    @SuppressWarnings("unused")
	private void servicesConnected() {

    	Context ctx = getApplicationContext();
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(ctx);
        
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		Editor editor = prefs.edit();
		
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d(TAG, "google play services NOT available");   
  			editor.putBoolean("google_play_services_available", true);
  			
        // Google Play services was not available for some reason
        } else {
            Log.d(TAG, "google play services NOT available");            
  			editor.putBoolean("google_play_services_available", false);
        }
        
		editor.commit();        
    }

}
