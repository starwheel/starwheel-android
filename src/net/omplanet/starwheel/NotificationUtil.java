package net.omplanet.starwheel;

import java.util.HashMap;

import net.omplanet.starwheel.cloud.backend.core.Consts;
import net.omplanet.starwheel.ui.activity.GuestbookActivity;
import net.omplanet.starwheel.ui.activity.IntroductionActivity;
import net.omplanet.starwheel.ui.utils.RoundedAvatarDrawable;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

public class NotificationUtil {
    
    public static void generateNotification(Context context, Intent intent) {
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

//		CharSequence vibrate = (CharSequence) data.get("vibrate");
		CharSequence sound = (CharSequence) data.get("sound");			
		
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
		
		// The next lines initialize the Notification, using the configurations above
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
		
		// Set sound and vibrate
		if("default".equals(sound)) {
			Log.e(Consts.TAG, "Notification: DEFAULT_SOUND");
		    notification.defaults |= Notification.DEFAULT_SOUND;
		} 
		else if(sound != null) {
			Log.e(Consts.TAG, "Notification: sound "+sound);
			Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
						 + "://" + context.getPackageName() + "/raw/" + sound);
	    	notification.sound = soundUri;
		}

//		if(vibrate != null) {
//			notification.defaults |= Notification.DEFAULT_VIBRATE;
//		}

		notification.defaults |= Notification.DEFAULT_LIGHTS;

		//notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
	
		
		
		// Get the notification ID, /it allows to update the notification later on.
		int notifyID = 1;
		String contentID = (String) data.get("id");
		if(contentID != null) {
			notifyID = Integer.parseInt(contentID);
		}
		
		// Notify
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
}
