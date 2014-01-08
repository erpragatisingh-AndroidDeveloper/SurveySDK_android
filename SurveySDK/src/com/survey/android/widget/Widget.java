package com.survey.android.widget;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import com.survey.android.R;
import com.survey.android.util.ConstantData;
import com.survey.android.view.Dashboard;
import com.survey.android.view.Notification;

public class Widget extends AppWidgetProvider {
	private static final String TAG = "Widget";
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.widget_layout);

		// When the icon on the widget is clicked, we send out a broadcast
		// containing the ACTION_WIDGET_UPDATE_FROM_ACTIVITY.
		// In the manifest, the widget is configured with an intent filter to
		// match this action.
		Intent intent = new Intent(context, Widget.class);
		intent.setAction(ConstantData.ACTION_WIDGET_UPDATE_FROM_WIDGET);

		// When we click the widget, we want to open our main activity.
		Intent defineIntent2 = new Intent(context, Dashboard.class);
		PendingIntent pendingIntent2 = PendingIntent.getActivity(context,
				0 /* no requestCode */, defineIntent2, 0 /* no flags */);
		remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent2);

		// Is this really necessary ?
		ComponentName thisWidget = new ComponentName(context, Widget.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(thisWidget, remoteViews);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "onReceive called with " + intent.getAction());
		super.onReceive(context, intent);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.widget_layout);
		ComponentName cn = new ComponentName(context, Widget.class);

		try {

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);

			String survey_id = prefs.getString("survey_id_c2dm", null);

			remoteViews.setTextViewText(
					R.id.txtLast,
					context.getResources().getString(
							R.string.check_out_available_surveys));

			Intent defineIntent2 = new Intent(context, Notification.class);
			defineIntent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);
			defineIntent2.putExtra("survey_id", survey_id);
			defineIntent2.putExtra("notification_id", 1);
			PendingIntent pendingIntent2 = PendingIntent
					.getActivity(context, 0 /* no requestCode */,
							defineIntent2,PendingIntent.FLAG_CANCEL_CURRENT /* no flags */);
			remoteViews
					.setOnClickPendingIntent(R.id.widget, pendingIntent2);

		} catch (Exception e) {
			e.printStackTrace();
		}

		AppWidgetManager.getInstance(context).updateAppWidget(cn, remoteViews);
	}

}