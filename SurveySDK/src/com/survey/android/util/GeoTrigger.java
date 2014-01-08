package com.survey.android.util;

public class GeoTrigger implements Comparable<GeoTrigger> {
	
	private double latitude;
	private double longitude;
	private Integer radius;
	private String survey_id;
	private String title;
	private String geo_trigger_id;
	private float distance;
	
	public double getLatitude() {
		return this.latitude;
	}
	
	public double getLongitude() {
		return this.longitude;
	}
	
	public Integer getRadius() {
		return this.radius;
	}	
	
	public String getSurveyId() {
		return this.survey_id;
	}	
	
	public String getTitle() {
		return this.title;
	}	
	
	public String getGeoTriggerId() {
		return this.geo_trigger_id;
	}	
	
	public float getDistance() {
		return this.distance;
	}	
	
	public void setDistance(float distance) {
		this.distance = distance;
	}
	
	public GeoTrigger(double latitude, double longitude, Integer radius, 
			String survey_id, String title, String geo_trigger_id, float distance)	{
		this.latitude = latitude;
		this.longitude = longitude;
		this.radius = radius;
		this.survey_id = survey_id;
		this.title = title;
		this.geo_trigger_id = geo_trigger_id;
		this.distance = distance;
	}
	//Before the code was-> new Float(distance).
    @Override
    public int compareTo(GeoTrigger o) {
        return Float.valueOf(distance).compareTo(o.distance);
    }
}
