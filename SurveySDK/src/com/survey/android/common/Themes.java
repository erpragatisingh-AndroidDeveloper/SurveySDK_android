package com.survey.android.common;

public enum Themes {
	
	KORINTHOS ("korinthos"),
	DEFAULT("default");
	
	private String theme;
	
	Themes(String theme) {
		this.theme = theme;
	}
	
	public static Themes getValue(String theme) {
		for (Themes currTheme:values()) {
			if (theme.equals(currTheme.theme)) {
				return currTheme;
			}
		}
		return DEFAULT;
	}

}
