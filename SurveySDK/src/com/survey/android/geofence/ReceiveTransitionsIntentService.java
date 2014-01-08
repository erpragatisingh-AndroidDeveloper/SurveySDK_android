package com.survey.android.geofence;

import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.survey.android.R;
import com.survey.android.containers.AppContainer;
import com.survey.android.util.GeoPush;
import com.survey.android.util.LocationLog;
import com.survey.android.util.Toiler;
//import com.nsphere.locationprovider.utils.Toiler;

/**
 * This class receives geofence transition events from Location Services, in the
 * form of an Intent containing the transition type and geofence id(s) that triggered
 * the event.
 */
public class ReceiveTransitionsIntentService extends IntentService {
    /**
     * Sets an identifier for this class' background thread
     */
    public ReceiveTransitionsIntentService() {
        super("ReceiveTransitionsIntentService");
    }

    /**
     * Handles incoming intents
     * @param intent The Intent sent by Location Services. This Intent is provided
     * to Location Services (inside a PendingIntent) when you call addGeofences()
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        // Create a local broadcast Intent
        Intent broadcastIntent = new Intent();

        // Give it the category for all intents sent by the Intent Service
        broadcastIntent.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);

        // First check for errors
        if (LocationClient.hasError(intent)) {

            // Get the error code
            int errorCode = LocationClient.getErrorCode(intent);

            // Get the error message
            String errorMessage = LocationServiceErrorMessages.getErrorString(this, errorCode);

            // Log the error
            Log.e(GeofenceUtils.APPTAG,
            		getString(R.string.geofence_transition_error_detail, errorMessage)
            );

            // Set the action and error message for the broadcast intent
            broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCE_ERROR)
                           .putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, errorMessage);

            // Broadcast the error *locally* to other components in this app
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

        // If there's no error, get the transition type and create a notification
        } else {

            // Get the type of transition (entry or exit)
            int transition = LocationClient.getGeofenceTransition(intent);

            // Test that a valid transition was reported
            if ( (transition == Geofence.GEOFENCE_TRANSITION_ENTER) ||
                 (transition == Geofence.GEOFENCE_TRANSITION_EXIT) ) {
                List<Geofence> geofences = LocationClient.getTriggeringGeofences(intent);
                String[] geofenceIds = new String[geofences.size()];
                String geo_trigger_id = null;
                for (int index = 0; index < geofences.size() ; index++) {
                    geofenceIds[index] = geofences.get(index).getRequestId();
                    geo_trigger_id = geofences.get(index).getRequestId();
                    handleGeoFenceEvent(geo_trigger_id, transition);
                }
            // An invalid transition was reported
            } else {
                // Always log as an error
                Log.e(GeofenceUtils.APPTAG,
                		getString(R.string.geofence_transition_invalid_type, transition));
            }
        }
    }

    private void handleGeoFenceEvent(String geo_trigger_id, int transition) {
        String transitionType = getTransitionString(transition);
		Log.d(GeofenceUtils.APPTAG, "GeoFence Event (geo_trigger_id, transitionType): " + geo_trigger_id + ":" + transitionType);
		String userToken = AppContainer.getUserToken();
		
		// check if GeoFence entered or exited
		if (userToken != null && userToken.length() > 0 &&
				geo_trigger_id != null && geo_trigger_id.length() > 0) {
			if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
				GeoPush.SendPush(userToken, geo_trigger_id,ReceiveTransitionsIntentService.this);
			} else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
				Location loc = Toiler.getLastBestLocation(this);
				LocationLog.SetLocationLog(userToken, geo_trigger_id, loc.getLatitude(), loc.getLongitude(), getString(R.string.geofence_transition_exited));
			}     
		}
    }
    
   
//    /**
//     * Posts a notification in the notification bar when a transition is detected.
//     * If the user clicks the notification, control goes to the main Activity.
//     * @param transitionType The type of transition that occurred.
//     *
//     */
//    private void sendNotification(String transitionType, String ids) {
//
//        // Create an explicit content Intent that starts the main Activity
//        Intent notificationIntent =
//                new Intent(this, Main.class);
//
//        // Construct a task stack
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//
//        // Adds the main Activity to the task stack as the parent
//        stackBuilder.addParentStack(Main.class);
//
//        // Push the content Intent onto the stack
//        stackBuilder.addNextIntent(notificationIntent);
//
//        // Get a PendingIntent containing the entire back stack
//        PendingIntent notificationPendingIntent =
//                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        // Get a notification builder that's compatible with platform versions >= 4
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//
//        // Set the notification contents
//        builder.setSmallIcon(R.drawable.ic_notification)
//               .setContentTitle(
//            		   getString(R.string.geofence_transition_notification_title,
//                               transitionType, ids))
//               .setContentText(getString(R.string.geofence_transition_notification_text))
//               .setContentIntent(notificationPendingIntent);
//
//        // Get an instance of the Notification manager
//        NotificationManager mNotificationManager =
//            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        // Issue the notification
//        mNotificationManager.notify(0, builder.build());
//    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     * @param transitionType A transition type constant defined in Geofence
     * @return A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);

            default:
                return getString(R.string.geofence_transition_unknown);
        }
    }
}
