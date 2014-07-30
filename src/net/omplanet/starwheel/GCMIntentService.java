/*
 * Copyright (c) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package net.omplanet.starwheel;

import java.io.IOException;
import java.util.HashMap;

import net.omplanet.starwheel.cloud.backend.core.Consts;
import net.omplanet.starwheel.ui.activity.GuestbookActivity;
import net.omplanet.starwheel.ui.activity.IntroductionActivity;
import net.omplanet.starwheel.ui.utils.RoundedAvatarDrawable;
import android.app.Application;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * This class manages Google Cloud Messaging push notifications and CloudQuery
 * subscriptions.
 */
public class GCMIntentService extends IntentService {

    private static final String GCM_KEY_SUBID = "subId";

    private static final String GCM_TYPEID_QUERY = "query";

    private static final String PROPERTY_REG_ID = "registration_id";

    private static final String PROPERTY_APP_VERSION = "app_version";

    public static final String BROADCAST_ON_MESSAGE = "on-message-event";
    
    @Override
    protected void onHandleIntent(Intent intent) {
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
                Log.i(Consts.TAG, "onHandleIntent: message error");
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                Log.i(Consts.TAG, "onHandleIntent: message deleted");
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                String subId = intent.getStringExtra(GCM_KEY_SUBID);
                Log.i(Consts.TAG, "onHandleIntent: subId: " + subId);
                String[] tokens = subId.split(":");
                String typeId = tokens[1];

                // dispatch message
                if (GCM_TYPEID_QUERY.equals(typeId)) {
                    Intent messageIntent = new Intent(BROADCAST_ON_MESSAGE);
                    messageIntent.putExtras(intent);
                    messageIntent.putExtra("token", tokens[2]);
                    boolean isReceived = LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
                    //TODO consider improving
                    if (!isReceived) {
                        Log.i(Consts.TAG, "A message has been recieved but no broadcast was handled.");
                        generateNotification(this, intent, tokens[2]);
                    } else {
                        Log.i(Consts.TAG, "A message has been recieved, broadcasted and handled.");
                    }
                }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }
    
    public static void generateNotification(Context context, Intent intent, String message) {
		//Event keys
		HashMap data = new HashMap();
		for (String key : intent.getExtras().keySet()) {
			Log.d(Consts.TAG, "Message key: " + key + " value: " + intent.getExtras().getString(key));
			String eventKey = key.startsWith("data.") ? key.substring(5) : key;
			data.put(eventKey, intent.getExtras().getString(key));
		}

		CharSequence contentTitle = (CharSequence) data.get("updatedBy");
		if (contentTitle == null) contentTitle = "New Message";
		
		CharSequence contentText = (CharSequence) data.get("message");
		if (contentText == null) contentText = "";
		
		CharSequence userId = (CharSequence) data.get("updatedBy");
		Bitmap iconBitmap = getUserIcon(context, userId.toString());
		if (iconBitmap == null) iconBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);

		// the next lines initialize the Notification, using the configurations above
/*
		Notification notification = new Notification(icon, tickerText, when);

		// Custom
		CharSequence vibrate = (CharSequence) data.get("vibrate");
		CharSequence sound = (CharSequence) data.get("sound");

		if("default".equals(sound)) {
			Log.e(Consts.TAG, "Notification: DEFAULT_SOUND");
		    notification.defaults |= Notification.DEFAULT_SOUND;
		} 
		else if(sound != null) {

			Log.e(Consts.TAG, "Notification: sound "+sound);

			String[] packagename = new String[]{"",""};//systProp.getString("com.activate.gcm.component", "").split("/");

			String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
			String path = baseDir + "/"+ packagename[0] +"/sound/"+sound; 
 
 			Log.e(Consts.TAG, path);

			File file = new File(path);

			Log.i(Consts.TAG,"Sound exists : " + file.exists());

			if (file.exists()) {
				Uri soundUri = Uri.fromFile(file);
		    	notification.sound = soundUri;
			}
			else {
		    	notification.defaults |= Notification.DEFAULT_SOUND;
			}
		}

		if(vibrate != null) {
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		}

		notification.defaults |= Notification.DEFAULT_LIGHTS;

		notification.flags = Notification.FLAG_AUTO_CANCEL;
		//notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
*/		
		
		// Creates an Intent for the Activity
//    	Intent launcherintent = new Intent("android.intent.action.MAIN");
//		launcherintent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//		Intent notificationIntent = new Intent(context, GCMIntentService.class);
//		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, launcherintent, 0);
		
		// Creates an Intent for the Activity
		Intent resultIntent = new Intent(context, GuestbookActivity.class);
		// The stack builder object will contain an artificial back stack for the started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(IntroductionActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		
		
		Notification.Builder mBuilder = new Notification.Builder(context);
		mBuilder.setContentIntent(resultPendingIntent);

		Notification notification = mBuilder
		.setContentTitle(contentTitle)
        .setContentText(contentText)
        .setSmallIcon(R.drawable.notification_icon)
        .setLargeIcon(iconBitmap)
        .setTicker(contentTitle + ": " + contentText)
        .setWhen(System.currentTimeMillis())
        .setAutoCancel(true)
        .build();
		
		///Get the notification ID, /it allows to update the notification later on.
		int notifyID = 1;
		String contentID = (String) data.get("id");
		if(contentID != null) {
			notifyID = Integer.parseInt(contentID);
		}
		
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(notifyID, notification);
    }
    
    private static Bitmap getUserIcon(Context context, String userId) {
    	//TODO get real user icons
    	if (userId != null && userId.contains("nacenonyx")) {
    		return RoundedAvatarDrawable.getCroppedBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.user_no_icon));
    	} else {
        	return null;
    	}
    }

    /**
     * Returns registration id associated with the specified {@link Application}
     * . This method will block the thread until regId will be available.
     * 
     * @param app {@link Application}
     * @return registration id
     */
    public static String getRegistrationId(Application app) {
        SharedPreferences prefs = getGcmPreferences(app);
        String regId = prefs.getString(PROPERTY_REG_ID, "");
        
        if (regId.isEmpty()) {
            Log.i(Consts.TAG, "Registration not found.");
            return doRegister(app);
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(app);
        if (registeredVersion != currentVersion) {
            Log.i(Consts.TAG, "App version changed.");
            return doRegister(app);
        }
        return regId;
    }
    
    private static String doRegister(Context context) {
        String msg = "";
        
        try {
        	// Check device for Play Services APK. If check succeeds, proceed with GCM registration.
            if (checkPlayServices(context)) {
            	GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
                String regId = gcm.register(Consts.PROJECT_NUMBER);
                msg = "Device registered, registration ID=" + regId;

                // For this demo: we don't need to send it because the device will send
                // upstream messages to a server that echo back the message using the
                // 'from' address in the message.

                SharedPreferences prefs = getGcmPreferences(context);
                int appVersion = getAppVersion(context);
                Log.i(Consts.TAG, "Saving regId on app version " + appVersion);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(PROPERTY_REG_ID, regId);
                editor.putInt(PROPERTY_APP_VERSION, appVersion);
                editor.commit();
            } else {
                Log.i(Consts.TAG, "No valid Google Play Services APK found.");
            }         
        } catch (IOException ex) {
            msg = "Error :" + ex.getMessage();
            // If there is an error, don't just keep trying to register.
            // Require the user to click a button again, or perform
            // exponential back-off.
        }
        return msg;
    }

    public GCMIntentService() {
        super(Consts.PROJECT_NUMBER);
    }
    
    /**
     * @return the stored SharedPreferences for GCM
     */
    private static SharedPreferences getGcmPreferences(Context context) {
        return context.getSharedPreferences(GCMIntentService.class.getSimpleName(), MODE_PRIVATE);
    }
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static boolean checkPlayServices(Context context) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            return false;
        }
        return true;
    }
}
