package com.survey.android.webclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;

import android.net.Uri;

import com.survey.android.model.AnswerModel;
import com.survey.android.util.ConstantData;
import com.survey.android.util.Log;
import com.survey.android.util.WhiteLabel;

public class HttpRequest {

	private static String getStringFromStream(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	private String url = "";
	private String body = null;
	private Map<String, String> args = new HashMap<String, String>();
	private List<AnswerModel> answers = new LinkedList<AnswerModel>();

	public HttpRequest(String url, Map<String, String> arguments) {
		this.url = url;
		this.args = arguments;
	}

	public HttpRequest(String url, List<AnswerModel> answers) {
		this.url = url;
		this.answers = answers;
	}

	/**
	 * Constructor for argument-less request
	 * 
	 * @param endpoint
	 *            RelativeUrl
	 */
	public HttpRequest(String url, String... args) {
		this.url = url;
		this.args = new HashMap<String, String>();
		if (args.length % 2 != 0) {
			throw new IllegalArgumentException("Unmatched arguments");
		} else {
			for (int i = 0; i < args.length; i += 2) {
				this.args.put(args[i], args[i + 1]);
			}
		}
	}

	public HttpRequest(String url, String body) {
		this.url = url;
		this.body = body;

	}

	public HttpRequest(String url, Map<String, String> args, String body) {
		this.url = url;
		this.args = args;
		this.body = body;
		if (args != null) {
			this.url += "?";
			for (Entry<String, String> arg : args.entrySet()) {
				this.url += arg.getKey() + "=" + arg.getValue() + "&";
			}
		}

	}

	/** DELETE methods */
	public String delete() throws IOException {
		return doRequest(new HttpDelete());
	}

	/**
	 * Do the request (POST/PUT)
	 * 
	 * @param the
	 *            method to call with
	 * @return the results from the server
	 * @throws IOException
	 */
	private String doRequest(HttpEntityEnclosingRequestBase http) {
		String result = "";

		try {
			result = execHttpRequest(http);
		} catch (Exception e1) {
			Log.e("e1:" + e1.toString());			
			Log.e(http.getMethod() + " -> Retrying 2nd attempt");
			try {
				result = execHttpRequest(http);
			} catch (Exception e2) {
				Log.e("e2:" + e2.toString());					
				Log.e(http.getMethod() + " -> Retrying 3rd attempt");			
				try {
					result = execHttpRequest(http);
				} catch (Exception e3) {
					Log.e("e3:" + e3.toString());					
					Log.e(http.getMethod() + " -> Retrying 4th attempt");
					try {
						result = execHttpRequest(http);
					} catch (Exception e4) {
						Log.e("e4:" + e4.toString());
						Log.e(http.getMethod() + " -> Retrying 5th attempt");						
						try {
							result = execHttpRequest(http);
						} catch (Exception e5) {
							Log.e("e5:" + e5.toString());					
							Log.e(http.getMethod() + " -> Retrying 6th attempt");
							try {
								result = execHttpRequest(http);
							} catch (Exception e6) {
								Log.e("e6:" + e6.toString());					
								Log.e(http.getMethod() + " -> Retry attempts Failed");
							}
						}
					}
				}				
			}
		}
		return result;		
	}

	private String execHttpRequest(HttpEntityEnclosingRequestBase http) throws IOException, SSLException, JSONException {
		String result = "";
		SurveyHttpClient httpclient = null;
		HttpEntity entity = null;
		HttpResponse response = null;
		InputStream instream = null;		

		try {
			httpclient = new SurveyHttpClient();
			
			HttpParams r = httpclient.getParams();
			HttpConnectionParams.setConnectionTimeout(r, 90000);
			http.setURI(URI.create(url));
	
			if (body == null) {
				// Prepare a request object (with parameters)
	
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				for (Entry<String, String> arg : args.entrySet()) {
					nameValuePairs.add(new BasicNameValuePair(arg.getKey(), arg
							.getValue()));
				}
				http.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			} else {
				Log.d("Data -> " + body.toString());
				http.setEntity(new ByteArrayEntity(body.toString().getBytes("UTF8")));
			}
	
			// Execute the request
			response = httpclient.execute(http);
	
			// Get hold of the response entity
			entity = response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry about connection release
			if (entity != null) {
				instream = entity.getContent();
				result = getStringFromStream(instream);
				instream.close();
			}
		} catch (SSLException se) {
			Log.e(http.getMethod() + " -> " + se.toString());
			Log.e(http.getURI().toString());
			se.printStackTrace();
			throw new SSLException(se);
		} catch (Exception e) {
			Log.e(http.getMethod() + " -> " + e.toString());
			Log.e(http.getURI().toString());			
			e.printStackTrace();
		}
		finally {
			if (entity != null) {
			    try {
			    	entity.consumeContent();
					instream.close();	
					httpclient.getConnectionManager().shutdown();
			        Log.d("execHttpRequest->closed resources");					
			    } catch (IOException e) {
			        Log.e("execHttpRequest->finally", e);
			    }
			}		
		}		
		Log.d(http.getMethod() + " -> " + this.toString());		
		
		return result;		
	}

	/**
	 * Do the request (GET/DELETE)
	 * 
	 * @param the
	 *            method to call with
	 * @return the results from the server
	 * @throws IOException
	 */
	private String doRequest(HttpRequestBase http) {
		String result = "";

		try {
			result = execHttpRequest(http);
		} catch (Exception e1) {
			Log.e("e1:" + e1.toString());			
			Log.e(http.getMethod() + " -> Retrying 2nd attempt");
			try {
				result = execHttpRequest(http);
			} catch (Exception e2) {
				Log.e("e2:" + e2.toString());					
				Log.e(http.getMethod() + " -> Retrying 3rd attempt");			
				try {
					result = execHttpRequest(http);
				} catch (Exception e3) {
					Log.e("e3:" + e3.toString());					
					Log.e(http.getMethod() + " -> Retrying 4th attempt");
					try {
						result = execHttpRequest(http);
					} catch (Exception e4) {
						Log.e("e4:" + e4.toString());
						Log.e(http.getMethod() + " -> Retrying 5th attempt");						
						try {
							result = execHttpRequest(http);
						} catch (Exception e5) {
							Log.e("e5:" + e5.toString());					
							Log.e(http.getMethod() + " -> Retrying 6th attempt");
							try {
								result = execHttpRequest(http);
							} catch (Exception e6) {
								Log.e("e6:" + e6.toString());					
								Log.e(http.getMethod() + " -> Retry attempts Failed");
							}
						}
					}
				}				
			}
		}
		return result;
	}

	private String execHttpRequest(HttpRequestBase http) throws IOException, SSLException, JSONException {
		String result = "";
		SurveyHttpClient httpclient = null;
		HttpEntity entity = null;
		HttpResponse response = null;
		InputStream instream = null;
		
		try {		
			httpclient = new SurveyHttpClient();
			
			// Prepare a request object (with parameters)
			http.setURI(URI.create(this.toString()));

			// ********************************************************************
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			int timeoutConnection = 3000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT) 
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 30000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			http.setParams(httpParameters);
			// ********************************************************************
			
			// Execute the request
			response = httpclient.execute(http);

			// Get hold of the response entity
			entity = response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry about connection release
			if (entity != null) {
				instream = entity.getContent();
				result = getStringFromStream(instream);
			}
		} catch (SSLException se) {
			Log.e(http.getMethod() + " -> " + se.toString());
			Log.e(http.getURI().toString());
			se.printStackTrace();
			throw new SSLException(se);
		} catch (Exception e) {
			Log.e(http.getMethod() + " -> " + e.toString());
			Log.e(http.getURI().toString());
			e.printStackTrace();
		} 
		finally {
			if (entity != null) {
			    try {
			    	entity.consumeContent();
					instream.close();	
					httpclient.getConnectionManager().shutdown();					
			        Log.d("execHttpRequest->closed resources");					
			    } catch (IOException e) {
			        Log.e("execHttpRequest->finally", e);
			    }
			}		
		}
		Log.d(http.getMethod() + " -> " + this.toString());				
		//Log.d(result);

		return result;
	}
	
	private boolean doRequestMulti(HttpEntityEnclosingRequestBase http) {
		boolean result = false;		

		try {
			result = execHttpRequestMulti(http);
		} catch (Exception e1) {
			Log.e("e1:" + e1.toString());			
			Log.e(http.getMethod() + " -> Retrying 2nd attempt");
			try {
				result = execHttpRequestMulti(http);
			} catch (Exception e2) {
				Log.e("e2:" + e2.toString());					
				Log.e(http.getMethod() + " -> Retrying 3rd attempt");			
				try {
					result = execHttpRequestMulti(http);
				} catch (Exception e3) {
					Log.e("e3:" + e3.toString());					
					Log.e(http.getMethod() + " -> Retrying 4th attempt");
					try {
						result = execHttpRequestMulti(http);
					} catch (Exception e4) {
						Log.e("e4:" + e4.toString());
						Log.e(http.getMethod() + " -> Retrying 5th attempt");						
						try {
							result = execHttpRequestMulti(http);
						} catch (Exception e5) {
							Log.e("e5:" + e5.toString());					
							Log.e(http.getMethod() + " -> Retrying 6th attempt");
							try {
								result = execHttpRequestMulti(http);
							} catch (Exception e6) {
								Log.e("e6:" + e6.toString());					
								Log.e(http.getMethod() + " -> Retry attempts Failed");
							}
						}
					}
				}				
			}
		}	
		return result;
	}

	private boolean execHttpRequestMulti(HttpEntityEnclosingRequestBase http)  throws IOException, SSLException, JSONException {
		SurveyHttpClient httpclient = null;
		MultipartEntity reqEntity = null;
		HttpResponse response = null;
		HttpEntity entity = null;
		InputStream instream = null;
		
		try {		
			httpclient = new SurveyHttpClient();
			
			http.setURI(URI.create(url));
			reqEntity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);
	
			// httpclient.getParams().setParameter("http.socket.timeout", new
			// Integer(1000));
			HttpParams r = httpclient.getParams();
			HttpConnectionParams.setConnectionTimeout(r, 10000);

			for (AnswerModel a : answers) {
				if (!ConstantData.WHITE_LABEL_APP.isWhiteLabel(WhiteLabel.SURVEY))
					Log.d("HttRequest", "answer that is being sent: " + a.getResponseType() + ": value: " + a.getAnswer());
				if (a.getResponseType().equals("TOKEN")) {
					reqEntity.addPart("token", new StringBody(a.getAnswer()));
				} else if(a.getResponseType().equals(ConstantData.RESPONSE_TYPE_OPEN_ENDED_TEXT) ||
						a.getResponseType().equals(ConstantData.RESPONSE_TYPE_FREE_TEXT)){
					if(a.getJson() != null)
						reqEntity.addPart("google_places_payload", new StringBody(a.getJson(), "application/json", Charset.forName("UTF-8")));
					reqEntity.addPart(a.getKeyForUrl(),
							new StringBody(a.getAnswer()));
					
				} else {
					reqEntity.addPart(a.getKeyForUrl(),
							new StringBody(a.getAnswer()));
				}
			}
			http.setEntity(reqEntity);
			
			response = httpclient.execute(http);
			entity = response.getEntity();
			String result = "";
			if (entity != null) {
				instream = entity.getContent();
				result = getStringFromStream(instream).trim();
				instream.close();
			}
			return (result.equals("true") ? true : false);
		} catch (SSLException se) {
			Log.e(http.getMethod() + " -> " + se.toString());
			Log.e(http.getURI().toString());
			se.printStackTrace();
			throw new SSLException(se);
		} catch (Exception e) {
			Log.e(http.getMethod() + " -> " + e.toString());
			Log.e(http.getURI().toString());
			e.printStackTrace();
		}
		finally {
			if (entity != null) {
			    try {
			    	entity.consumeContent();
					instream.close();	
					httpclient.getConnectionManager().shutdown();					
			        Log.d("execHttpRequestMulti->closed resources");					
			    } catch (IOException e) {
			        Log.e("execHttpRequestMulti->finally", e);
			    }
			}		
		}
		return false;
	}
	
	/** GET methods */
	public String get() throws IOException {
		return doRequest(new HttpGet());
	}

	/** POST methods */
	public String post() throws IOException {
		return doRequest(new HttpPost());
	}

	/** PUT methods */
	public String put() throws IOException {
		return doRequest(new HttpPut());
	}

	/**
	 * Returns the encoded URI as a String
	 * 
	 * @return String encoded as URI (' ' -> %20)
	 */
	@Override
	public String toString() {
		return this.toUri().toString();
	}

	/**
	 * Generates a URI for this request
	 * 
	 * @return URI with formatted URL
	 */
	public Uri toUri() {
		StringBuilder res = new StringBuilder(url);
		String myuri = ""; 
		if (args != null) {
			res.append("?");

			for (Entry<String, String> arg : args.entrySet()) {
				res.append(Uri.encode(arg.getKey()));
				res.append("=");
				res.append(Uri.encode(arg.getValue()));
				res.append("&");
			}
		}
		myuri = res.toString();
		if (myuri.charAt(myuri.length() - 1) == '&')
			myuri = myuri.substring(0, myuri.length() - 1);		

		return Uri.parse(myuri);
	}

	public boolean post(boolean b) throws IOException {
		return doRequestMulti(new HttpPost());
	}

}
