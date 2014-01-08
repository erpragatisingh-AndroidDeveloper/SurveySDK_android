package com.survey.android.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.amazonaws.AmazonClientException;
import com.survey.android.R;
import com.survey.android.containers.PollContainer;
import com.survey.android.model.Prefs;
import com.survey.android.util.Log;

/**
 * Uploads a file to S3 in a background thread, showing a persistent notification as it goes and
 * retrying with progressive backoffs several times in case of failure.
 *
 * Pass EXTRA_LOCAL_URI and EXTRA_S3_PATH when starting this service with Strings for the file to
 * upload and where to put it in the S3 bucket.
 */
public class BackgroundUploader extends Service {
	private static final int NOTIFICATION_ID = 803257228;
	public static final String EXTRA_LOCAL_URI = Prefs.PACKAGE + ".localUri";
	public static final String EXTRA_S3_PATH = Prefs.PACKAGE + ".s3Path";
	private NotificationCompat.Builder mNotificationBuilder;
	protected static final int MAX_RETRIES = 3;
	protected static final long RETRY_DELAY_MULTIPLIER_MILLIS = 10000;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("Starting background service...");
		mNotificationBuilder = new NotificationCompat.Builder(this);
		mNotificationBuilder.setSmallIcon(android.R.drawable.stat_notify_sync_noanim);
		mNotificationBuilder.setContentTitle(getString(R.string.app_name));
		mNotificationBuilder.setContentText(getString(R.string.uplaoding_data));
		mNotificationBuilder.setTicker(getString(R.string.uplaoding_data));
		mNotificationBuilder.setOngoing(true);
		mNotificationBuilder.setWhen(System.currentTimeMillis());
		startForeground(NOTIFICATION_ID, mNotificationBuilder.build());

		String localUriExtra = null;
		String s3PathExtra = null;

		if (intent != null && intent.getExtras() != null) {
			localUriExtra = intent.getStringExtra(EXTRA_LOCAL_URI);
			s3PathExtra = intent.getStringExtra(EXTRA_S3_PATH);
		}

		final String localUri = localUriExtra;
		final String s3Path = s3PathExtra;

		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean success = false;
				int retries = 0;

				if (localUri != null && s3Path != null) {
					while (!success && retries < MAX_RETRIES) {
						try {
							Log.d("Uploading retry "+retries);
							Thread.sleep(RETRY_DELAY_MULTIPLIER_MILLIS * retries);
							PollContainer.putFileToS3(localUri, s3Path,BackgroundUploader.this);
							success = true;
						} catch (AmazonClientException e) {
							Log.e("Error in BackgroundUploader:", e);
							retries++;
						} catch (InterruptedException e) {
							Log.e("Error sleeping before retry", e);
							retries++;
						}
					}

					if (retries == MAX_RETRIES) {
						Log.e("Failed to uplaod file after all retries.");
					}
				} else {
					Log.d("No extras found on intent; nothing to upload.");
				}

				Log.d("Stopping service and removing notification");
				stopForeground(true);
				stopSelf();
			}
		}).start();

		return START_STICKY;
	}
}