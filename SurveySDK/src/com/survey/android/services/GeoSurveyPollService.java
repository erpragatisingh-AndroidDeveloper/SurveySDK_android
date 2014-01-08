package com.survey.android.services;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.survey.android.R;
import com.survey.android.util.ConstantData;
import com.survey.android.util.Toiler;


public class GeoSurveyPollService extends Service {
	
	  protected static final String TAG = "GeoSurveyPollService";
	  private Looper mServiceLooper;
	  private ServiceHandler mServiceHandler;

	  // Handler that receives messages from the thread
	  private final class ServiceHandler extends Handler {
	
		  public ServiceHandler(Looper looper) {
			  super(looper);
		  }

		  @Override
		  public void handleMessage(Message msg) {
			  Timer timer = new Timer();
			  timer.scheduleAtFixedRate(new TimerTask() {
			
				  @SuppressWarnings("deprecation")
				@Override
				  public void run() {
					  SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					  Log.i(TAG, "Polling for geo surveys initated at: " + new Date().toGMTString());
					  String userToken = prefs.getString(getString(R.string.token), null);
					  Log.d(TAG, "userToken: " + userToken);
					  Toiler.retrieveAndRunGeoSurveys(GeoSurveyPollService.this, userToken, false);
				  }
			  }, 0, ConstantData.GEO_POLL_SERVICE_INTERVAL);
		  }
	  }

	  @Override
	  public void onCreate() {
	    // Start up the thread running the service.  Note that we create a
	    // separate thread because the service normally runs in the process's
	    // main thread, which we don't want to block.  We also make it
	    // background priority so CPU-intensive work will not disrupt our UI.
	    HandlerThread thread = new HandlerThread("ServiceStartArguments",
	            10);
	    thread.start();
	    
	    // Get the HandlerThread's Looper and use it for our Handler 
	    mServiceLooper = thread.getLooper();
	    mServiceHandler = new ServiceHandler(mServiceLooper);
	  }

	  @Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
	      Log.i(TAG, "GeoSurveyPollService Service starting");
	      
	      Message msg = mServiceHandler.obtainMessage();
	      msg.arg1 = startId;
	      mServiceHandler.sendMessage(msg);
	      
	      // If we get killed, after returning from here, restart
	      return START_STICKY;
	  }

	  @Override
	  public IBinder onBind(Intent intent) {
	      // We don't provide binding, so return null
	      return null;
	  }
	  
	  @Override
	    public void onDestroy() {
	        //Toiler.unregisterReceivers();
	        Log.i(TAG, "GeoSurveyPollService Service ended");
	    }

	}
