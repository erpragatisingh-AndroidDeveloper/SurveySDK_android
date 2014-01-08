package com.survey.android.view.themed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.survey.android.R;
import com.survey.android.model.CategoryModel;
import com.survey.android.model.ResponseModel;
import com.survey.android.model.SurveyModel;
import com.survey.android.util.ConstantData;
import com.survey.android.util.Log;
import com.survey.android.util.WhiteLabel;
import com.survey.android.view.LocalizedFragmentActivity;
import com.survey.android.view.Notification;
import com.survey.android.view.Survey;
import com.survey.android.webclient.RestClient;

//*************************************************************************************************

public abstract class SurveyThemed extends LocalizedFragmentActivity { /* List */

	private static final String TAG = "SurveyList";
	
	private static final int TIME_DELAYED = 750;
	private static final int POSITION_CATEGORIES = 0;
	private static final int POSITION_SURVEYS = 1;

	protected ListView lvCatSurv;
	protected TextView txtTitle;
	protected Button btnBack;
	private ProgressDialog pd;
	private AlertDialog alert;
	private Runnable alertRunnable;
	private Handler handler;

	int position = POSITION_CATEGORIES;

	String surveyEmpty[] = {};
	static List<CategoryModel> categories;
	static List<SurveyModel> surveys;
	static ResponseModel response;
	static int selectedCategoryIndex = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.survey_list);

		handler = new Handler();
		alertRunnable = null; // explicit null
		position = getIntent().getIntExtra("position", POSITION_CATEGORIES);

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
		lvCatSurv = (ListView) this.findViewById(R.id.lvCatSurv);		
		txtTitle = (TextView) this.findViewById(R.id.txtTitle);
		btnBack = (Button) this.findViewById(R.id.btnBack);

		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SurveyThemed.this.finish();
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY))
			Log.d(TAG, "position: " + position);
		switch (position) {
		case POSITION_CATEGORIES:
			txtTitle.setText(R.string.categories);
			// loadCategories();
			(new LoadCategoriesTask()).execute();

			break;
		case POSITION_SURVEYS:
			txtTitle.setText(R.string.surveys);
			// loadSurveys();
			(new LoadSurveysTask()).execute();
			break;
		}
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
		try {
			if (pd != null) {
				pd.dismiss();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

    @SuppressWarnings("unused")
	private void RemoveSurveyFromUserList(String survey_id) {
		Log.d(TAG, "Removing survey_id from user: " + survey_id);    	
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(SurveyThemed.this);   
		try {
			String userToken = prefs.getString(getString(R.string.token),"");
			Log.d(TAG, "userToken: " + userToken);
			boolean result = RestClient.RemoveSurveyById(userToken, survey_id);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
	private class SurveyListAdapter extends BaseAdapter {
		private String[] items;
		private String[] statuses;

		public SurveyListAdapter(String[] items, String[] statuses) {
			this.items = items;
			this.statuses = statuses;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;

			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) SurveyThemed.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.survey_list_row, null);
			}

			TextView txtValue = (TextView) view.findViewById(R.id.txtValue);
			txtValue.setText(items[position]);
			return view;
		}

		@Override
		public int getCount() {
			return items.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}
		
		public void removeItem(int position) {
			if (position >= 0 && this.items.length >= position) {
				List<String> item_list = new ArrayList<String>(Arrays.asList(this.items));
				List<String> status_list = new ArrayList<String>(Arrays.asList(this.statuses));
				item_list.remove(position);
				status_list.remove(position);	
				this.items = item_list.toArray(new String[item_list.size()]);
				this.statuses = status_list.toArray(new String[status_list.size()]);				
			}
		}		
	}

	/**
	 * Background task loading available surveys for current user in chosen
	 * category
	 * 
	 * @author dominum
	 * 
	 */
	private class LoadSurveysTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			alertRunnable = new Runnable() {

				public void run() {
					if (pd == null) {
						pd = ProgressDialog.show(SurveyThemed.this, getResources()
								.getString(R.string.loading), getResources()
								.getString(R.string.please_wait), true, false);
					}
				}
			};
			handler.postDelayed(alertRunnable, 0);
			// if(pd==null){
			// pd = ProgressDialog.show(SurveyThemed.this,
			// getResources().getString(R.string.loading), getResources()
			// .getString(R.string.please_wait), true, false);
			// }
			// super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			surveys = null;
			try {
				if (categories == null) {
					categories = CategoryModel.allRemote(getApplicationContext());
				}
				surveys = SurveyModel.allRemote(SurveyThemed.this,
						categories.get(selectedCategoryIndex).getId());
			} catch (Exception e) {
				Log.e(TAG, "Exception: " + e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			try {
				// if (pd.isShowing()) {
				pd.dismiss();
				pd = null;
				// }
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (surveys == null) {
				SurveyListAdapter ba = new SurveyListAdapter(surveyEmpty, null);
				lvCatSurv.setAdapter(ba);
				alertRunnable = new Runnable() {
					public void run() {
						alert = new AlertDialog.Builder(SurveyThemed.this)
								.setMessage(
										getResources()
												.getString(
														R.string.connection_error_press_ok))
								.setPositiveButton(R.string.ok,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int whichButton) {
												(new LoadSurveysTask())
														.execute();
											}
										})
								.setNegativeButton(R.string.Cancel,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int whichButton) {
												finish();
											}
										}).create();
						alert.show();
					}
				};
				handler.postDelayed(alertRunnable, TIME_DELAYED);

			} else {
				final List<SurveyModel> filtSurveys = new LinkedList<SurveyModel>();
				for (SurveyModel survey : surveys) {
					if (survey.getQuestionCount() > 0)
						filtSurveys.add(survey);
				}
				if (filtSurveys.size() == 0) {
					SurveyListAdapter ba = new SurveyListAdapter(surveyEmpty, null);
					lvCatSurv.setAdapter(ba);

					alertRunnable = new Runnable() {
						public void run() {
							alert = new AlertDialog.Builder(SurveyThemed.this)
									.setMessage(
											getResources().getString(
													R.string.no_more_surveys))
									.setPositiveButton(
											R.string.ok,
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int whichButton) {
													finish();
												}
											}).create();
							alert.show();
						}
					};
					handler.postDelayed(alertRunnable, TIME_DELAYED);

				} else {
					String[] surveyNames = new String[filtSurveys.size()];
					String[] statuses = new String[filtSurveys.size()];

					for (int i = 0; i < surveyNames.length; i++) {
						surveyNames[i] = filtSurveys.get(i).getTitle();
						statuses[i] = ConstantData.RESPONSE_TYPE_INCOMPLETE;
					}
					final SurveyListAdapter ba = new SurveyListAdapter(surveyNames,
							statuses);
					lvCatSurv.setAdapter(ba);
					lvCatSurv.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int arg2, long arg3) {
							try {
								// response = ResponseModel.remote(SurveyThemed.this,
								// filtSurveys.get(arg2).getId());


								String survey_id = filtSurveys.get(arg2)
										.getId();
								SurveyThemed.this.startActivity(new Intent(
										SurveyThemed.this, Notification.class)
										.putExtra("survey_id", survey_id));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
					
					// Implements survey deletion from user list on LongClick
					lvCatSurv.setOnItemLongClickListener(new OnItemLongClickListener() {
						@Override
						public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
								int arg2, long arg3) {
							final int position = arg2;
							DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() { 
								public void onClick(DialogInterface dialog, int which) { 
									switch (which) { 
										case DialogInterface.BUTTON_POSITIVE: 
											// Yes button clicked 
											//Toast.makeText(SurveyThemed.this, "Yes Clicked", Toast.LENGTH_LONG).show(); 
											try {
												String survey_id = filtSurveys.get(position)
														.getId();
												ba.removeItem(position);
												ba.notifyDataSetChanged();
												RemoveSurveyFromUserList(survey_id);
											} catch (Exception e) {
												e.printStackTrace();
											}											
											break; 
										case DialogInterface.BUTTON_NEGATIVE: 
											// No button clicked 
											// do nothing 
											//Toast.makeText(AlertDialogActivity.this, "No Clicked", Toast.LENGTH_LONG).show(); 
											break; 
									} 
								} 
							};							
							AlertDialog.Builder builder = new AlertDialog.Builder(SurveyThemed.this); 
							builder.setMessage(getString(R.string.remove_survey_from_list_confirm)) 
								.setPositiveButton(getString(R.string.confirm_yes), dialogClickListener) 
								.setNegativeButton(getString(R.string.confirm_no), dialogClickListener).show();

							return false;						
						}
				    });											
				}
			}
			// super.onPostExecute(result);
		}
	}

	private class LoadCategoriesTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			alertRunnable = new Runnable() {

				public void run() {
					if (pd == null) {
						pd = ProgressDialog.show(SurveyThemed.this, getResources()
								.getString(R.string.loading), getResources()
								.getString(R.string.please_wait), true, false);
					}
				}
			};
			handler.postDelayed(alertRunnable, 0);
			// super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			categories = null;
			try {
				categories = CategoryModel.allRemote(SurveyThemed.this);
			} catch (Exception e) {
				if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY)) {
					Log.e(TAG, "Exception: " + e);
				} else {
					Log.e(TAG, "Exc: " + e);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			try {
				// if (pd.isShowing()) {
				pd.dismiss();
				pd = null;
				// }
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (categories == null) {
				BaseAdapter ba = new SurveyListAdapter(surveyEmpty, null);
				lvCatSurv.setAdapter(ba);
				// Handler handler = new Handler();
				alertRunnable = new Runnable() {

					public void run() {
						alert = new AlertDialog.Builder(SurveyThemed.this)
								.setMessage(
										getResources()
												.getString(
														R.string.connection_error_press_ok))
								.setPositiveButton(R.string.ok,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int whichButton) {
												(new LoadCategoriesTask())
														.execute();
											}
										})
								.setNegativeButton(R.string.Cancel,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int whichButton) {
												finish();
											}
										}).create();
						alert.show();

					}
				};
				handler.postDelayed(alertRunnable, TIME_DELAYED);

			} else if (categories.size() == 0) {
				alertRunnable = new Runnable() {
					public void run() {
						alert = new AlertDialog.Builder(SurveyThemed.this)
								.setMessage(
										getResources().getString(
												R.string.no_more_categories))
								.setPositiveButton(R.string.ok,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int whichButton) {
												finish();
											}
										}).create();
						alert.show();
					}
				};
				handler.postDelayed(alertRunnable, TIME_DELAYED);
			} else {
				lvCatSurv.setVisibility(View.VISIBLE);
				String[] categoryNames = new String[categories.size()];
				for (int i = 0; i < categoryNames.length; i++) {
					categoryNames[i] = categories.get(i).getName();
				}
				BaseAdapter ba = new SurveyListAdapter(categoryNames, null);
				lvCatSurv.setAdapter(ba);
				lvCatSurv.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {

						selectedCategoryIndex = arg2;
						SurveyThemed.this.startActivity(new Intent(SurveyThemed.this,
								Survey.class).putExtra("position",
								POSITION_SURVEYS));
					}
				});
			}
			// super.onPostExecute(result);
		}
	}

}
