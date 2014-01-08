package com.survey.android.util;

import com.survey.android.services.DataBroadcastReceiver;

import android.content.Context;

public class GeoTriggerBroadcastReceiver {
	
	private Context context;
	private DataBroadcastReceiver receiver;
	
	public Context getContext() {
		return this.context;
	}
	
	public DataBroadcastReceiver getReceiver() {
		return this.receiver;
	}
	
	public GeoTriggerBroadcastReceiver(Context context, DataBroadcastReceiver receiver)	{
		this.context = context;
		this.receiver = receiver;
	}

}

