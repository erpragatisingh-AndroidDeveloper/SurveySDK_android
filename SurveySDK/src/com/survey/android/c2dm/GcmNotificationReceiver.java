/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.survey.android.c2dm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.survey.android.R;
import com.survey.android.session.Configuration;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmNotificationReceiver extends IntentService {
    public static final String TAG = "GcmNotificationReceiver";
	public static final int NOTIFICATION_ID = 1;
	
    NotificationCompat.Builder builder;

    public GcmNotificationReceiver() {
        super("GcmNotificationReceiver");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    	if (Configuration.isGCMNotificationsEnabled()) {    	
	        Bundle extras = intent.getExtras();
	        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
	        // The getMessageType() intent parameter must be the intent you received
	        // in your BroadcastReceiver.
	        String messageType = gcm.getMessageType(intent);
	
	        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
	            /*
	             * Filter messages based on message type. Since it is likely that GCM will be
	             * extended in the future with new message types, just ignore any message types you're
	             * not interested in, or that you don't recognize.
	             */
	            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
	                Log.e(TAG, "Send error: " + extras.toString());
	            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
	                Log.d(TAG, "Deleted messages on server: " + extras.toString());
	            // If it's a regular GCM message, post notification.
	            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
	                Log.d(TAG, "Received: " + extras.toString());
	            	sendNotification(intent);
	            }
	        }
    	}
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    @SuppressWarnings("deprecation")
	private void sendNotification(Intent intent) {
    	
        Log.d(TAG, "In sendNotification()"); 
    	if (Configuration.isGCMEnabled() && Configuration.isGCMNotificationsEnabled()) {
            Log.i(TAG, "posting Notification to status bar"); 
            
    		final String testResult = intent.getStringExtra("message");
    		final String surveyId = intent.getStringExtra("survey_id");

    		try {
    			NotificationManager mNotificationManager = (NotificationManager) getBaseContext()
    					.getSystemService(Context.NOTIFICATION_SERVICE);

    			CharSequence tickerText = "Survey notification";
    			long when = System.currentTimeMillis();
    			NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(this);
    			mNotificationBuilder.setSmallIcon(R.drawable.c2dm_icon);
    			mNotificationBuilder.setTicker(tickerText);
    			mNotificationBuilder.setWhen(when);
    			
    			CharSequence contentTitle = "Survey notification";	
    			int NOTIFICATION_ID = 1;
    			Intent notificationIntent = new Intent(this,
    					com.survey.android.view.Notification.class);
    			notificationIntent.putExtra("notification_id", NOTIFICATION_ID);
    			notificationIntent.putExtra("survey_id", surveyId);
    			PendingIntent contentIntent = PendingIntent.getActivity(
    					this, 0, notificationIntent,
    					PendingIntent.FLAG_ONE_SHOT
    							+ PendingIntent.FLAG_UPDATE_CURRENT);

    			mNotificationBuilder.setContentTitle(contentTitle);
    			mNotificationBuilder.setContentText(testResult);
    			mNotificationBuilder.setContentIntent(contentIntent);

    			mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    		} catch (Exception e) {
    			e.printStackTrace();
    		}	
    	} else {
            Log.i(TAG, "GCM Notification Not Enabled");     		
    	}
    }
}
