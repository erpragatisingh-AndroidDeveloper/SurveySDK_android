package com.survey.android.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.io.IOUtils;

import com.survey.android.util.ConstantData;

@SuppressWarnings("serial")
public class AnswerModel implements Serializable {
	private String questionId;
	private String responseType;
	private String answer = null;
	private String json = null;

	public String getResponseType() {
		return this.responseType;
	}

	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}

	public AnswerModel(String questionId, String responseType) {
		this.questionId = questionId;
		this.responseType = responseType;
		this.answer = null;
	}

	public AnswerModel(String questionId, String responseType, byte[] data) {
		this.questionId = questionId;
		this.responseType = responseType;
		this.answer = null;
	}

	public AnswerModel(String questionId, String responseType, String answer) {
		this.questionId = questionId;
		this.responseType = responseType;
		this.answer = answer;
	}

	public AnswerModel(String questionId, String responseType, String answer, String json) {
		this.questionId = questionId;
		this.responseType = responseType;
		this.answer = answer;
		this.json = json;
	}

	public AnswerModel(String questionId, String responseType, byte[] data,String pathMedia) {
		this.questionId = questionId;
		this.responseType = responseType;
		this.answer = pathMedia;
	}
	public String getQuestionId() {
		return questionId;
	}

	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}

	public String getAnswer() {
		return this.answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public byte[] getData() {
		byte[] result=toByteArray(this.answer, this.responseType);
		return result;
	}

	public String getKeyForUrl() {
		String result = "answers[" + this.questionId + "]";
		if (responseType.equals(ConstantData.RESPONSE_TYPE_MULTIPLE_SELECT)) {
			result += "[]";
		}
		return result;
	}

	
	
	//  *******  merge toByteArray functions to one function ***************
	private byte[] toByteArray(String fileName, String fileType) {
		byte[] result = null;
//		String filePath = "";
//		if (fileType.equals(ConstantData.RESPONSE_TYPE_VIDEO)) {
//			filePath = ConstantData.VIDEO_FOLDER_ROOT + fileName;
//		} else {
//			filePath = ConstantData.IMAGE_FOLDER_ROOT + fileName;
//		}
		result = toByteArray(fileName);
		return result;
	}

	private byte[] toByteArray(String filePath) {
		byte[] result = null;

		FileInputStream fileStream = null;
		try {
			fileStream = new FileInputStream(filePath);
			result = IOUtils.toByteArray(fileStream);
			// Do something useful to the data
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(fileStream);
		}

		return result;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public String getJson() {
		return json;
	}

}
