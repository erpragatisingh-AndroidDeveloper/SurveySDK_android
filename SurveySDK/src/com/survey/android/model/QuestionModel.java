package com.survey.android.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

@SuppressWarnings("serial")
public class QuestionModel implements Serializable{

  private String _id;
  private String description;
  private String hint;
  private String key;
  private Integer length; // In seconds; for video and audio questions.
  private Double max;
  private String max_label;
  private Double min;
  private String min_label;
  private String response_type;
  private String text;
  private List<Choice> choices;
//  private Integer question_count;
  private List<QuestionModel> questions;
  private boolean google_place_question;

  public String getId() {
    return this._id;
  }

  public void setId(String id) {
    this._id = id;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getHint() {
    return this.hint;
  }

  public void setHint(String hint) {
    this.hint = hint;
  }

  public String getKey() {
    return this.key;
  }

  public void setKey(String key) {
    this.key = key;
  }
  
  public Integer getLength() {
    return this.length;
  }

  public void setLength(Integer length) {
    this.length = length;
  }

  public Double getMax() {
    return this.max;
  }

  public void setMax(Double max) {
    this.max = max;
  }

  public String getMaxLabel() {
    return this.max_label;
  }

  public void setMaxLabel(String maxLabel) {
    this.max_label = maxLabel;
  }

  public Double getMin() {
    return this.min;
  }

  public void setMin(Double min) {
    this.min = min;
  }

  public String getMinLabel() {
    return this.min_label;
  }

  public void setMinLabel(String minLabel) {
    this.min_label = minLabel;
  }

  public String getResponseType() {
    return this.response_type;
  }

  public void setResponseType(String responseType) {
    this.response_type = responseType;
  }

  public String getText() {
    return this.text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public List<Choice> getChoices() {
    return this.choices;
  }

  public void setChoices(List<Choice> choices) {
    this.choices = choices;
  }

  public List<QuestionModel> getQuestions() {
    return this.questions;
  }

  public void setQuestions(List<QuestionModel> questions) {
    this.questions = questions;
  }

  public List<String> getChoiceId() {
    List<String> result = new LinkedList<String>();
    for (Choice s : choices)
      result.add(s._id);
    return result;
  }

  public List<String> getChoiceLabel() {
    List<String> result = new LinkedList<String>();
    for (Choice s : choices)
      result.add(s.label);
    return result;
  }
  
  public List<Boolean> getMutuallyExclusive(){
	  List<Boolean> result=new LinkedList<Boolean>();
	    for (Choice s : choices)
	        result.add(s.exclusive==null?false:s.exclusive);
	  return result;
  }

  public List<String> getChoiceValue() {
    List<String> result = new LinkedList<String>();
    for (Choice s : choices)
      result.add(s.value);
    return result;
  }

  public Map<String, List<String>> getSubquestions() {
    Map<String, List<String>> result = new HashMap<String, List<String>>();
    List<String> id = new LinkedList<String>();
    List<String> text = new LinkedList<String>();

    for (QuestionModel q : questions) {
      id.add(q._id);
      text.add(q.text);
    }
    result.put("_id", id);
    result.put("text", text);
    return result;
  }
  
  public String toJson() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }

  public void setGooglePlaceQuestion(String google_place_question) {
	this.google_place_question = google_place_question.equalsIgnoreCase("true");
  }

  public boolean getGooglePlaceQuestion() {
	return google_place_question;
  }

  class Choice implements Serializable{
    public String _id;
    public String label;
    public String value;
    public Boolean exclusive;
  }
}