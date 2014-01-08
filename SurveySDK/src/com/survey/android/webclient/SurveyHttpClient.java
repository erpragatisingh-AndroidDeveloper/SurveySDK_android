package com.survey.android.webclient;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

public class SurveyHttpClient extends DefaultHttpClient {

  	public SurveyHttpClient() {
		super();
  	}

  @Override protected ClientConnectionManager createClientConnectionManager() {
	try {
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(
				new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		registry.register(new Scheme("https", new SurveySSLSocketFactory(), 443));
 
		return new SingleClientConnManager(getParams(), registry);
    } catch (Exception e) {
        e.printStackTrace();
    	return null;
    }
  }

}