package com.survey.android.custom_widgets;

import java.util.LinkedList;
import java.util.List;

import com.survey.android.R;
import com.survey.android.model.AnswerModel;
import com.survey.android.util.ConstantData;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PollSelectionTable extends LinearLayout {

	public static final int SINGLE_SELECT = 0;
	public static final int MULTIPLE_SELECT = 1;

	private String question_id;
	@SuppressWarnings("unused")
	private List<String> _id;
	private List<String> label;
	private List<String> value;
	private List<Boolean> checked;
	private List<Boolean> mutually_exclusive;
	private int type;

	public PollSelectionTable(Context context) {
		super(context);
	}

	public PollSelectionTable(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void initializeItems(String question_id, int type, List<String> ids,
			List<String> labels, List<String> values, List<Boolean> exclusive) {
		this.question_id = question_id;
		this.type = type;
		this._id = ids;
		this.label = labels;
		this.value = values;
		this.mutually_exclusive = exclusive;
		this.checked = new LinkedList<Boolean>();
		LayoutInflater inflater = (LayoutInflater) this.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for (int i = 0; i < values.size(); i++) {
			final int id = i;
			this.checked.add(false);
			final View row = inflater.inflate(R.layout.q_list_row_text, null);

			TextView txtChoice = (TextView) row.findViewById(R.id.txtChoice);
			txtChoice.setText(label.get(id));

			row.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					boolean temp = checked.get(id);
					if (PollSelectionTable.this.type == SINGLE_SELECT) {
						for (int j = 0; j < checked.size(); j++) {
							checked.set(j, false);
							repaintRow(j, false);
								
//							View tempView = PollSelectionTable.this
//									.getChildAt(j);
//							TextView txtChoice = (TextView) tempView
//									.findViewById(R.id.txtChoice);
//							CheckBox cbTemp = (CheckBox) tempView
//									.findViewById(R.id.chk);
//							cbTemp.setChecked(false);
//							txtChoice.setTextColor(getResources().getColor(R.color.gray));
						}
					}
					else{
						boolean exclusive=mutually_exclusive.get(id);
						if(exclusive && !temp){
							clearAll();
						}
						else{
							boolean flag=true;
							int k=0;
							while(k<checked.size() && flag){
								if(checked.get(k) && mutually_exclusive.get(k)){
									clearAll();
									flag=false;
								}
								k++;
							}
						}
					}
					checked.set(id, !temp);
					CheckBox cbTemp = (CheckBox) row.findViewById(R.id.chk);
					TextView txtTemp = (TextView) row
							.findViewById(R.id.txtChoice);

					txtTemp.setTextColor(!temp ? getResources().getColor(R.color.green_slider) :getResources().getColor( R.color.gray));
					cbTemp.setChecked(!temp);
				}
			});
			this.addView(row);
		}
	}

	public void initializeItems(int type, List<String> ids,
			List<String> labels, List<String> values) {
		this.type = type;
		this._id = ids;
		this.label = labels;
		this.value = values;
		this.checked = new LinkedList<Boolean>();
		LayoutInflater inflater = (LayoutInflater) this.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for (int i = 0; i < values.size(); i++) {
			final int id = i;
			this.checked.add(false);
			final View row = inflater.inflate(R.layout.q_list_row_text, null);

			TextView txtChoice = (TextView) row.findViewById(R.id.txtChoice);
			txtChoice.setText(label.get(id));

			row.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					boolean temp = checked.get(id);
					if (PollSelectionTable.this.type == SINGLE_SELECT) {
						for (int j = 0; j < checked.size(); j++) {
							checked.set(j, false);
							repaintRow(j, false);
//							View tempView = PollSelectionTable.this
//									.getChildAt(j);
//							CheckBox cbTemp = (CheckBox) tempView
//									.findViewById(R.id.chk);
//							TextView txtChoice = (TextView) tempView
//									.findViewById(R.id.txtChoice);
//
//							cbTemp.setChecked(false);
//							txtChoice.setTextColor(getResources().getColor(R.color.gray));
						}
					}
					else{
						boolean exclusive=mutually_exclusive.get(id);
						if(exclusive && !temp){
							clearAll();
						}
						else{
							boolean flag=true;
							int k=0;
							while(k<checked.size() && flag){
								if(checked.get(k) && mutually_exclusive.get(k)){
									clearAll();
									flag=false;
								}
								k++;
							}
						}
					}
					checked.set(id, !temp);
					CheckBox cbTemp = (CheckBox) row.findViewById(R.id.chk);
					TextView txtTemp = (TextView) row
							.findViewById(R.id.txtChoice);

					txtTemp.setTextColor(!temp ? getResources().getColor(R.color.green_slider) : getResources().getColor(R.color.gray));
					cbTemp.setChecked(!temp);
				}
			});

			this.addView(row);
		}
	}

	/**
	 * Returns boolean which indicates if there is at least one row marked
	 * 
	 * @return
	 */
	public boolean isAnswered() {
		for (boolean b : this.checked) {
			if (b)
				return true;
		}
		return false;
	}

	/**
	 * Returns list of selected indexes
	 * 
	 * @return	
	 */
	private List<Integer> getSelectedIndexes() {
		List<Integer> result = new LinkedList<Integer>();
		for (int i = 0; i < this.checked.size(); i++) {
			if (this.checked.get(i))
				result.add(i);
		}
		return result;
	}

	/**
	 * Returns selected answer - used in single select question
	 * 
	 * @return
	 */
	public String getSelectedAnswer() {
		List<Integer> selected = getSelectedIndexes();
		return this.value.get(selected.get(0));
	}

	/**
	 * Returns list of selected answers - used in multi select question
	 * 
	 * @return
	 */
	public List<AnswerModel> getSelectedAnswers() {
		List<AnswerModel> result = new LinkedList<AnswerModel>();
		for (int i = 0; i < checked.size(); i++) {
			if (checked.get(i)) {
				result.add(new AnswerModel(question_id,
						ConstantData.RESPONSE_TYPE_MULTIPLE_SELECT, this.value
								.get(i)));
			}
		}
		return result;
	}

	/**
	 * 
	 * @param answers
	 */
	public void setSelectedAnswers(List<AnswerModel> answers) {
		if (answers != null) {
			for (AnswerModel answer : answers) {
				boolean tempContinue = true;
				for (int i = 0; i < value.size() && tempContinue; i++) {
					if (value.get(i).equals(answer.getAnswer())) {
						this.checked.set(i, true);
						tempContinue = false;
						repaintRow(i,/*this.checked.get(i)*/true);
					}
				}
			}
			invalidate();
		}
	}

	/**
	 * Clear list
	 */
	public void clearAll() {
		for (int i = 0; i < value.size(); i++) {
			boolean tempChecked=this.checked.get(i);
			if(tempChecked){
				repaintRow(i, false);
//				View temp=PollSelectionTable.this.getChildAt(i);
//				CheckBox cbTemp=(CheckBox)temp.findViewById(R.id.chk);
//				TextView txtTemp=(TextView)temp.findViewById(R.id.txtChoice);
//				cbTemp.setChecked(false);
//				txtTemp.setTextColor(getResources().getColor(R.color.gray));
			}
			this.checked.set(i, false);
		}
		invalidate();
	}
	
	/**	
	 * Repaints text and checkbox based on passed parameters: index of row and value ( tru, false )
	 * @param index
	 * @param value
	 */
	private void repaintRow(int index, boolean value){
		try{
			View temp=PollSelectionTable.this.getChildAt(index);
			CheckBox cbTemp=(CheckBox)temp.findViewById(R.id.chk);
			TextView txtTemp=(TextView)temp.findViewById(R.id.txtChoice);
			cbTemp.setChecked(value);
			txtTemp.setTextColor(value?getResources().getColor(R.color.green_slider):getResources().getColor(R.color.gray));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
