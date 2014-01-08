package com.survey.android.webclient;

import java.io.InputStream;
import java.security.KeyStore;

import org.apache.http.HttpResponse;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.protocol.HttpContext;

import android.content.Context;

import com.survey.android.R;
import com.survey.android.util.ConstantData;

public class HttpsClient extends DefaultHttpClient {

	private Context appcontext;

  	public HttpsClient(Context appcontext) {
		super();
	    this.appcontext = appcontext;
  	}

  @Override protected ConnectionKeepAliveStrategy createConnectionKeepAliveStrategy () {
	  return (new ConnectionKeepAliveStrategy() {
		  @Override public long getKeepAliveDuration (HttpResponse response, HttpContext context) {
			  return 60000;
		  }
	  });
  }
  
  @Override protected ClientConnectionManager createClientConnectionManager() {
	try {
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(
				new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		registry.register(new Scheme("https", newSslSocketFactory(), 443));
 
		SingleClientConnManager connmgr = new SingleClientConnManager(getParams(), registry);
		return connmgr;
    } catch (Exception e) {
        e.printStackTrace();
    	return null;
    }
  }

  private SSLSocketFactory newSslSocketFactory() {
	InputStream in = null;
	
    try {
      KeyStore trusted = KeyStore.getInstance("BKS");
      in = appcontext.getResources().openRawResource(R.raw.surveycertstore);
      trusted.load(in, ConstantData.CERTSTORE_KEY.toCharArray());
      return new SSLSocketFactory(trusted);
    } catch (Exception e) {
      throw new AssertionError(e);
    } finally {
    	try {
        in.close();
    	} catch (Exception e) {}
    }
  }

}