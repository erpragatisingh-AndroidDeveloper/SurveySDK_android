package com.survey.android.custom_widgets;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.AttributeSet;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.survey.android.R;
import com.survey.android.model.AnswerModel;
import com.survey.android.util.ConstantData;

public class PollRatingsScaleLabeled extends TableLayout {

	private List<String> subquestions;
	private List<String> labels;
	private List<String> _id;
	private List<String> values;
	private List<Integer> currentSelected;
	private List<TextView> currentTextViewSelected;

	// ************************************************************
	private String masterQuestion;
	private boolean singleSlider;
	private List<SeekBar> seekBars;
	//private Toast toast;

	private String masterId;

	// ************************************************************

	public PollRatingsScaleLabeled(Context context) {
		super(context);
	}

	public PollRatingsScaleLabeled(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
	public void initiliaze(Map<String, List<String>> subquestions,
			List<String> labels, List<String> list, String masterQuestion,
			String id) {
		// ****************************************************************************
		this.seekBars = new LinkedList<SeekBar>();
		// ****************************************************************************

		this.masterId = id;

		this.subquestions = subquestions.get("text");
		this.singleSlider = this.subquestions == null
				|| this.subquestions.size() == 0;
		if (this.singleSlider) {
			this._id = new ArrayList<String>();
			this._id.add(id);
		} else {
			this._id = subquestions.get("_id");
			// this._id.add(id);
		}

		this.labels = labels;
		this.values = list;
		this.currentSelected = new ArrayList();
		this.currentTextViewSelected = new ArrayList<TextView>();
		this.masterQuestion = masterQuestion;

		for (int i = 0; i < this.subquestions.size()
				|| (this.singleSlider && i < 1); i++) {
			this.currentSelected.add(-1);
			this.currentTextViewSelected.add(null);
		}

		final int drawableId = R.drawable.slider_untouched;
		final Drawable d = getResources().getDrawable(drawableId);
		d.setBounds(new Rect(0, 0, d.getIntrinsicWidth(), d
				.getIntrinsicHeight()));

		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		Display display = ((WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE)).getDefaultDisplay();
		int displayWidth = display.getWidth();
		final float scale = getContext().getResources().getDisplayMetrics().density;
		int arrowRight = (int) (30 * scale + 0.5f);
		int rowWidth = (int) (displayWidth - arrowRight);
		int textWidth = labels.size() <= 3 ? (int) ((float) rowWidth / labels
				.size())
				: (int) (rowWidth / (Math.ceil((float) labels.size() / 2) + ((labels
						.size() % 2 == 0) ? 0.5 : 0)));
		for (int i = 0; i < this.subquestions.size()
				|| (this.singleSlider && i < 1); i++) {
			final int index = i;
			TableRow tempView = (TableRow) inflater.inflate(
					R.layout.q_ratings_scale_labeled_row, null);
			// *************************************************************************************************
			TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(
					rowWidth, TableLayout.LayoutParams.WRAP_CONTENT);
			int leftMargin = 0;
			int topMargin = 0;
			int rightMargin = 0;
			int bottomMargin = 20;
			tableRowParams.setMargins(leftMargin, topMargin, rightMargin,
					bottomMargin);
			tempView.setLayoutParams(tableRowParams);
			// *************************************************************************************************
			FontTextView txtQuestion = (FontTextView) tempView
					.findViewById(R.id.txtQuestion);
			final LinearLayout llUpperLabels = (LinearLayout) tempView
					.findViewById(R.id.llUpperLabels);
			// LinearLayout llSeekBar = (LinearLayout) tempView
			// .findViewById(R.id.llSeekBar);
			final LinearLayout llLowerLabels = (LinearLayout) tempView
					.findViewById(R.id.llLowerLabels);

			txtQuestion.setWidth(rowWidth);
			llUpperLabels.setLayoutParams(new LayoutParams(rowWidth,
					LayoutParams.WRAP_CONTENT));
			llUpperLabels.setPadding(0, 0, 0, 10);
			// llSeekBar.setLayoutParams(new LayoutParams(rowWidth,
			// LayoutParams.WRAP_CONTENT));
			// llSeekBar.setPadding(0, 0, 0, 0);
			llLowerLabels.setLayoutParams(new LayoutParams(rowWidth,
					LayoutParams.WRAP_CONTENT));
			llLowerLabels.setPadding(0, 0, 0, 20);
			llLowerLabels.setVisibility(labels.size() <= 3 ? View.GONE
					: View.VISIBLE);
			llLowerLabels.setPadding(textWidth / 2, 0, 0, 0);

			final SeekBar sbRowValue = (SeekBar) tempView
					.findViewById(R.id.sbRating);
			// *******************************************************************************
			seekBars.add(sbRowValue);
			// *******************************************************************************
			android.view.ViewGroup.LayoutParams a = sbRowValue
					.getLayoutParams();
			a.width = rowWidth;
			sbRowValue.setLayoutParams(a);
			// sbRowValue.setPadding(0, 0, 0, 0);
			sbRowValue.setThumb(d);
			sbRowValue.incrementProgressBy(1);
			sbRowValue.setProgress(0);
			sbRowValue.setThumbOffset(-1);
			sbRowValue.setMax((int) (labels.size() - 1));
			sbRowValue.setPadding(textWidth / 4, 0, textWidth / 4, 0);

			sbRowValue
					.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {

						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
							if (currentSelected.get(index) == -1) {
								final int drawableId = R.drawable.slider;
								final Drawable d = getResources().getDrawable(
										drawableId);
								d.setBounds(new Rect(0, 0, d
										.getIntrinsicWidth(), d
										.getIntrinsicHeight()));
								sbRowValue.setThumb(d);
								sbRowValue.setThumbOffset(-1);
								TextView newTxt = null;
								if (llLowerLabels.getVisibility() == View.GONE) {
									newTxt = (TextView) llUpperLabels
											.getChildAt(seekBar.getProgress());
								} else {
									if (seekBar.getProgress() % 2 == 0) {
										newTxt = (TextView) llUpperLabels
												.getChildAt(seekBar
														.getProgress() / 2);
									} else {
										newTxt = (TextView) llLowerLabels
												.getChildAt(seekBar
														.getProgress() / 2);
									}
								}
								newTxt.setTextColor(getResources().getColor(
										R.color.green_slider));
								currentTextViewSelected.set(index, newTxt);
								currentSelected.set(index,
										seekBar.getProgress());

								// touched.set(index, true);
							}
						}

						@Override
						public void onProgressChanged(SeekBar seekBar,
								int progress, boolean fromUser) {
							currentSelected.set(index, seekBar.getProgress());

							TextView oldTxt = currentTextViewSelected
									.get(index);
							TextView newTxt = null;
							if (llLowerLabels.getVisibility() == View.GONE) {
								newTxt = (TextView) llUpperLabels
										.getChildAt(seekBar.getProgress());
							} else {
								if (seekBar.getProgress() % 2 == 0) {
									newTxt = (TextView) llUpperLabels
											.getChildAt(seekBar.getProgress() / 2);
								} else {
									newTxt = (TextView) llLowerLabels
											.getChildAt(seekBar.getProgress() / 2);
								}
							}
							if (oldTxt != null) {
								oldTxt.setTextColor(getResources().getColor(
										R.color.white));
							}
							newTxt.setTextColor(getResources().getColor(
									R.color.green_slider));
							currentTextViewSelected.set(index, newTxt);

//							Toiler.showToast(
//									PollRatingsScaleLabeled.this.getContext(),
//									toast,
//									getResources().getString(
//											R.string.new_rating)
//											+ newTxt.getText().toString());

						}
					});

			// this.seekBars.add(sbRowValue);

			CharSequence styledText = Html
					.fromHtml(this.singleSlider ? this.masterQuestion
							: subquestions.get("text").get(i));
			txtQuestion.setText(styledText);

			for (int j = 0; j < labels.size(); j++) {
				FontTextView temp = (FontTextView) inflater.inflate(
						R.layout.q_text_cell, null);
				temp.setText(labels.get(j));
				temp.setWidth(textWidth);
				temp.setMaxWidth(textWidth);
				// temp.setMinWidth(textWidth);
				if (labels.size() <= 3 || j % 2 == 0) {
					llUpperLabels.addView(temp);
				} else {
					llLowerLabels.addView(temp);
				}
			}
			this.addView(tempView);
		}

	}

	public boolean isAnswered() {
		for (Integer b : currentSelected) {
			if (b == -1)
				return false;
		}
		return true;
	}

	/**
	 * Returns list of answers
	 * 
	 * @return
	 */
	public List<AnswerModel> getAnswers() {
		List<AnswerModel> result = new ArrayList<AnswerModel>();

		for (int i = 0; i < this.subquestions.size()
				|| (this.singleSlider && i < 1); i++) {

			String tempVal = null;
			if (currentSelected.get(i) != -1) {
				tempVal = this.values.get(currentSelected.get(i)).toString();
			}

			result.add(new AnswerModel(
					this._id.get(i),
					ConstantData.RESPONSE_TYPE_RATING_SCALE_LABELED,
					/* this.values.get(currentSelected.get(i)).toString() */tempVal));
		}

		// Backend request this piece of "code" for grouping subquestions in one
		// group
		if (!this.singleSlider) {
			result.add(new AnswerModel(
					this.masterId,
					ConstantData.RESPONSE_TYPE_RATING_SCALE_LABELED,
					/* this.values.get(currentSelected.get(i)).toString() */"parent"));
		}
		return result;
	}

	/**
	 * Iterate through list of answers and set SeekBars to appropriate value
	 * 
	 * @param answers
	 */
	public void setAnswers(List<AnswerModel> answers) {
		for (int i = 0; i < answers.size(); i++) {
			String tempVal = answers.get(i).getAnswer();
			if (tempVal != null) {
				int index = getIndexByLabel(answers.get(i).getAnswer());
				if (index != -1) {
					final int drawableId = R.drawable.slider;
					final Drawable d = getResources().getDrawable(drawableId);
					d.setBounds(new Rect(0, 0, d.getIntrinsicWidth(), d
							.getIntrinsicHeight()));
					seekBars.get(i).setThumb(d);
					seekBars.get(i).setThumbOffset(-1);
					seekBars.get(i).setProgress(index);
					// *********************************************************
					currentSelected.set(i, index);
					// *********************************************************
				}
			}
		}
	}

	public List<String> getSubquestions() {
		return this.subquestions;
	}

	public void setSubquestions(List<String> subquestions) {
		this.subquestions = subquestions;
	}

	public List<String> getLabels() {
		return this.labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

	public List<String> getValues() {
		return this.values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public List<Integer> getCurrentSelected() {
		return this.currentSelected;
	}

	public void getCurrentSelected(List<Integer> currentSelected) {
		this.currentSelected = currentSelected;
	}

	public List<String> getID() {
		return this._id;
	}

	public void setID(List<String> id) {
		this._id = id;
	}

	private int getIndexByLabel(String label) {
		Integer result = -1;
		boolean continueSearch = true;
		for (int i = 0; i < values.size() && continueSearch; i++) {
			if (values.get(i).equals(label)) {
				result = i;
				continueSearch = false;
			}
		}
		return result;
	}

}
