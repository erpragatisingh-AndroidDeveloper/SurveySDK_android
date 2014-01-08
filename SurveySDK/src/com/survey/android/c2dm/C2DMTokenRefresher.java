package com.survey.android.c2dm;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.survey.android.util.Log;
import com.survey.android.util.Toiler;
 
public class C2DMTokenRefresher extends BroadcastReceiver {
 
	private static final String TAG = "C2DMTokenRefresher";

	@SuppressWarnings("deprecation")
	@Override
	public void onReceive(Context context, Intent intent) {
		 Log.d(TAG, "date Received: " + new Date().toGMTString());
		 Toiler.refreshAndScheduleNotificationToken(context.getApplicationContext());
	}
 
}
