package com.zuzex.look2meet.PushNotifications;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.zuzex.look2meet.DataModel.DialogObject;
import com.zuzex.look2meet.DataModel.UserProfile;
import com.zuzex.look2meet.OrganisationActivity;
import com.zuzex.look2meet.ProfilesActivity;
import com.zuzex.look2meet.R;
import com.zuzex.look2meet.chat.ChatActivity;

public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    public static final String TAG = "PUSH_NOTIFICATIONS";
    public static final String TITLE = "Look2meet";

    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    private enum NotificationType { ANNOUNCE, CHAT_MESSAGE, UNKNOWN};
    private NotificationType notificationType;

    public GcmIntentService() {
        super("GcmIntentService");
    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.i("LocalService", "Received start id " + startId + ": " + intent);
//        // We want this service to continue running until it is explicitly
//        // stopped, so return sticky.
//        return START_STICKY;
//    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);


//        for (String key : extras.keySet()) {
//            Object value = extras.get(key);
//            Log.w(TAG, String.format("%s %s (%s)", key,
//                    value.toString(), value.getClass().getName()));
//        }


//        Log.w("PUSH", "RECIEVED");

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                // ...
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                // ...
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Post notification of received message.
                sendNotification(intent);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(Intent intent) {
        Log.w("Notify", "Recieved");

        String body = "";
        Intent targetIntent = null;
        body =  intent.getStringExtra("message");
        String idTo = intent.getStringExtra("idTo");
        String idFrom = intent.getStringExtra("idFrom");
        String dialogId = intent.getStringExtra("dialogId");
        String type = intent.getStringExtra("type");
        if(type.equals("message")) {
            if (Integer.valueOf(idTo) == UserProfile.getInstance().id) {
                //redirect to dialog
                targetIntent = new Intent(this, ChatActivity.class);
                DialogObject dialogObject = new DialogObject(Integer.valueOf(dialogId), Integer.valueOf(idFrom), Integer.valueOf(idTo));
                targetIntent.putExtra("currDialogObject", dialogObject);
            } else {
                targetIntent = new Intent(this, ProfilesActivity.class);
            }
        } else if(type.equals("anons")) {
            String idObject = intent.getStringExtra("idObject");
            targetIntent = new Intent(this, OrganisationActivity.class);
            targetIntent.putExtra("mapObjectID", Integer.valueOf(idObject));
        } else {
            targetIntent = new Intent(this, ProfilesActivity.class);
        }

        mNotificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int width = getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
        int height = getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
        Bitmap largeIcon = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.l2m_icon_60_2x), width, height, true);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] pattern = {300,300,100,300};

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setLargeIcon(largeIcon)
                        .setSmallIcon(R.drawable.l2m_icon_60_2x)
                        .setSound(alarmSound)
                        .setVibrate(pattern)
                        .setLights(Color.YELLOW, 1000, 3000)
                        .setContentTitle(TITLE)
                        .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body))
                        .setContentText(body)
                        .setAutoCancel(true);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}