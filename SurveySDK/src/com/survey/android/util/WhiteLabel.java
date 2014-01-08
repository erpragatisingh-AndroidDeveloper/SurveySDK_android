package com.survey.android.util;

public enum WhiteLabel {
	
    SURVEY, KORINTHOS;
    
    public boolean isWhiteLabel(WhiteLabel whiteLabel) {
    	return whiteLabel == this;
    }
}