package com.survey.android.custom_widgets;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.AttributeSet;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.survey.android.R;
import com.survey.android.model.AnswerModel;
import com.survey.android.util.ConstantData;

public class PollRatings extends RelativeLayout {

	private LinearLayout llRatingNumbers;
	private TableLayout tblRatings;
	private List<String> _id;
	private List<String> text;
	private List<Integer> values;
	private List<Boolean> touched;

	// ******************************************************************
	private boolean singleRating;
	private String masterId;
	private List<SeekBar> seekBars;
	//private Toast toast;

	// ******************************************************************

	public PollRatings(Context context) {
		super(context);
		this.text = new ArrayList<String>();
		this.values = new ArrayList<Integer>();
		this.touched = new ArrayList<Boolean>();
	}

	public PollRatings(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.text = new ArrayList<String>();
		this.values = new ArrayList<Integer>();
		this.touched = new ArrayList<Boolean>();
	}

	public List<Boolean> getTouched() {
		return this.touched;
	}

	public void setTouched(List<Boolean> touched) {
		this.touched = touched;
	}

	public List<String> get_id() {
		return this._id;
	}

	public void set_id(List<String> id) {
		this._id = id;
	}

	@SuppressWarnings("deprecation")
	public void initiliaze(Map<String, List<String>> subquestions,
			final double min, double max, String minLabel, String maxLabel,
			String masterId) {

		// **********************************************************************************
		seekBars = new LinkedList<SeekBar>();
		// **********************************************************************************

		final int drawableId = R.drawable.slider_untouched;
		Drawable d = getResources().getDrawable(drawableId);
		d.setBounds(new Rect(0, 0, d.getIntrinsicWidth(), d
				.getIntrinsicHeight()));

		this.masterId = masterId;
		this.text = subquestions.get("text");
		this.singleRating = text == null || text.size() == 0;
		this._id = new ArrayList<String>();
		if (!this.singleRating) {

			this._id = subquestions.get("_id");
			// this._id.add(masterId);
		} else {
			this._id.add(masterId);
		}

		Display display = ((WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE)).getDefaultDisplay();
		int displayWidth = display.getWidth();
		final float scale = getContext().getResources().getDisplayMetrics().density;
		int cellAnswerWidth = /*
							 * (answers == null || answers.size() < 2) ? 0 :
							 */(int) (80 * scale + 0.5f);

		int seekBarWidth = (int) (displayWidth
				- (this.singleRating ? 0 : cellAnswerWidth) - (30 * scale + 0.5f))
				+ d.getIntrinsicWidth() / 2;
		int cellCaptureWidth = (int) ((seekBarWidth - 10 - d
				.getIntrinsicWidth()) / (float) (max - min));
		int captureWidth = (int) ((seekBarWidth - 10 - d.getIntrinsicWidth())
				* (max - min + 1) / (float) (max - min));

		llRatingNumbers = (LinearLayout) this
				.findViewById(R.id.llRatingNumbers);
		tblRatings = (TableLayout) this.findViewById(R.id.tblRatings);
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		this.values = new ArrayList<Integer>();
		this.touched = new ArrayList<Boolean>();
		for (int i = 0; i < this.text.size() || (singleRating && i < 1); i++) {
			this.values.add((int) min);
			this.touched.add(false);
		}
		
	

		for (int i = 0; (i < this.text.size() || (singleRating && i < 1)); i++) {
			final int index = i;

			TableRow tempView = (TableRow) inflater.inflate(
					R.layout.q_table_row, null);
			FontTextView txtRowName = (FontTextView) tempView
					.findViewById(R.id.txtRowName);
			final FontTextView txtVal=(FontTextView)tempView.findViewById(R.id.txtVal);

			final SeekBar sbRowValue = (SeekBar) tempView
					.findViewById(R.id.sbRowValue);
					
			// *************************************************************************************
			seekBars.add(sbRowValue);
			// *************************************************************************************

			if (this.singleRating) {
				txtRowName.setWidth(0);
				txtRowName.setVisibility(View.GONE);
			} else {
				txtRowName.setText(this.text.get(i).trim());
				txtRowName.setWidth(cellAnswerWidth);
				txtRowName.setClickable(false);
				txtRowName.setFocusable(false);
				txtRowName.setFocusableInTouchMode(false);
			}
			android.view.ViewGroup.LayoutParams a = sbRowValue
					.getLayoutParams();

			a.width = seekBarWidth;
			a.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
			sbRowValue.setLayoutParams(a);

			sbRowValue.setPadding(singleRating ? ((int) (20 * scale + 0.5f))
					: 0, 0, singleRating ? ((int) (10 * scale + 0.5f)) : 0, 0);
			sbRowValue.setThumb(d);
			sbRowValue.incrementProgressBy(1);
			sbRowValue.setProgress(0);
			sbRowValue.setThumbOffset(-1);
			sbRowValue.setMax((int) (max - min));
			
			sbRowValue
					.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
//							int progress = seekBar.getProgress();

//							txtVal.setText(getResources().getString(
//									R.string.new_rating)
//									+ ((int) (progress + min)));
							
//							Toiler.showToast(
//									PollRatings.this.getContext(),
//									toast,
//									getResources().getString(
//											R.string.new_rating)
//											+ ((int) (progress + min)));

						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
							if (!touched.get(index)) {
								final int drawableId = R.drawable.slider;
								final Drawable d = getResources().getDrawable(
										drawableId);
								d.setBounds(new Rect(0, 0, d
										.getIntrinsicWidth(), d
										.getIntrinsicHeight()));
								sbRowValue.setThumb(d);
								sbRowValue.setThumbOffset(-1);
								touched.set(index, true);
							}

//							// ***********************************************************************************
//							LayoutInflater inflater = (LayoutInflater) PollRatings.this
//									.getContext().getSystemService(
//											Context.LAYOUT_INFLATER_SERVICE);
//							View layout = inflater
//									.inflate(R.layout.toast, null);
//							PopupWindow pw = new PopupWindow(layout, 300, 470,
//									true);
//							pw.setFocusable(true);
//							pw.setOutsideTouchable(true);
//							pw.setTouchable(true);
//							pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
//							// ***********************************************************************************
						}

						@Override
						public void onProgressChanged(SeekBar seekBar,
								int progress, boolean fromUser) {
							values.set(index, (int) (progress + min));
							
							txtVal.setText(getResources().getString(
									R.string.new_rating)
									+ ((int) (progress + min)));

						}
					});

			tblRatings.addView(tempView);
		}

		LayoutParams params = (LayoutParams) llRatingNumbers.getLayoutParams();
		params.leftMargin = (displayWidth - captureWidth) / 2 + 10;
		params.width = captureWidth;
		TextView txtLowerBound = (TextView) this
				.findViewById(R.id.txtLowerBound);
		TextView txtUpperBound = (TextView) this
				.findViewById(R.id.txtUpperBound);

		txtLowerBound.setMaxWidth(seekBarWidth / 2 - 10);
		txtLowerBound.setMinWidth((int) (60 * scale + 0.5f));
		if (this.singleRating) {
			RelativeLayout.LayoutParams paramsSingle = (RelativeLayout.LayoutParams) txtLowerBound
					.getLayoutParams();
			paramsSingle.setMargins(0, 0, 0, 0); // substitute parameters for
													// left, top, right, bottom
			txtLowerBound.setLayoutParams(paramsSingle);
		}

		txtUpperBound.setMaxWidth(seekBarWidth / 2 - 10);
		txtUpperBound.setMinWidth((int) (60 * scale + 0.5f));

		Resources res = getResources();
		StringBuilder tempMinCapture = new StringBuilder();
		tempMinCapture.append(/*
							 * minLabel.length() > 20 ? minLabel .substring(0,
							 * 17) + "..." :
							 */minLabel);

		minLabel = tempMinCapture.toString();
		String text = String.format(res.getString(R.string.rating), minLabel);
		CharSequence styledText = Html.fromHtml(text);
		txtLowerBound.setText(styledText);

		StringBuilder tempMaxCapture = new StringBuilder();
		tempMaxCapture.append(/*
							 * maxLabel.length() > 20 ? maxLabel .substring(0,
							 * 17) + "..." :
							 */maxLabel);

		maxLabel = tempMaxCapture.toString();

		text = String.format(res.getString(R.string.rating), maxLabel);
		styledText = Html.fromHtml(text);
		txtUpperBound.setText(styledText);
		txtUpperBound.setText(styledText);
		int numberCells = (int) (max - min + 1);
		for (int i = 0; i < numberCells; i++) {
			TextView txtCapture = (TextView) inflater.inflate(
					R.layout.q_table_cell_header, null);
			android.view.ViewGroup.LayoutParams a = new android.view.ViewGroup.LayoutParams(
					cellCaptureWidth,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			txtCapture.setLayoutParams(a);
			txtCapture.setText("" + (i + 1));
			llRatingNumbers.addView(txtCapture);
		}
		llRatingNumbers.setVisibility(View.GONE);
	}

	public List<String> getText() {
		return this.text;
	}

	public void setText(List<String> answers) {
		this.text = answers;
	}

	public List<Integer> getValues() {
		return this.values;
	}

	public void setValues(List<Integer> values) {
		this.values = values;
	}

	/**
	 * Return list of answers
	 * 
	 * @return
	 */
	public List<AnswerModel> getAnswers() {
		List<AnswerModel> result = new ArrayList<AnswerModel>(this.text.size());

		for (int i = 0; i < this.values.size() || (this.singleRating && i < 1); i++) {
			result.add(new AnswerModel(this._id.get(i),
					ConstantData.RESPONSE_TYPE_RATING_SCALE, this.touched
							.get(i) ? this.values.get(i).toString() : null));
		}

		// Backend request this piece of "code" for grouping subquestions in one
		// group
		if (!this.singleRating) {
			result.add(new AnswerModel(
					masterId,
					ConstantData.RESPONSE_TYPE_RATING_SCALE_LABELED,
					/* this.values.get(currentSelected.get(i)).toString() */"parent"));
		}
		return result;
	}

	/**
	 * Iterate through list of answers and set value on every
	 * 
	 * @param ans
	 */
	public void setAnswers(List<AnswerModel> ans) {
		for (int i = 0; i < ans.size(); i++) {
			AnswerModel a = ans.get(i);
			if (a.getAnswer() != null) {
				final int drawableId = R.drawable.slider;
				final Drawable d = getResources().getDrawable(drawableId);
				d.setBounds(new Rect(0, 0, d.getIntrinsicWidth(), d
						.getIntrinsicHeight()));
				seekBars.get(i).setThumb(d);
				seekBars.get(i).setThumbOffset(-1);
				seekBars.get(i).setProgress(Integer.parseInt(a.getAnswer()));
				values.set(i, Integer.parseInt(a.getAnswer()));
				touched.set(i, true);
			} else {
				touched.set(i, false);
			}
		}
		invalidate();
	}

	public boolean isAnswered() {
		for (Boolean b : touched) {
			if (b == false)
				return false;
		}
		return true;
	}
}