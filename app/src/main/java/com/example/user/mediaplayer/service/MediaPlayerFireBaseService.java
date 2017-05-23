package com.example.user.mediaplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.user.mediaplayer.R;
import com.example.user.mediaplayer.ui.MainActivity;
import com.example.user.mediaplayer.utility.UtilityClass;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by user on 22/05/17.
 */

public class MediaPlayerFireBaseService extends FirebaseMessagingService {

    private static final String TAG = "MediaPlayerFireBaseServ";
    private static final int PENDING_INTENT_REQUEST_CODE = 1;


    @Override
    public void onMessageReceived(RemoteMessage pFirebaseMessage) {
//        super.onMessageReceived(pFirebaseMessage);



        Log.d(TAG, "onMessageReceived: ");
        if (pFirebaseMessage.getNotification()!= null) {
            Log.d(TAG, "onMessageReceived: " + pFirebaseMessage.getNotification().getBody());

            setNotification(pFirebaseMessage.getNotification().getBody());

        }else if(pFirebaseMessage.getData()!=null && pFirebaseMessage.getNotification()==null) {

            Log.d(TAG, "onMessageReceived: "+pFirebaseMessage.getData().get("message"));
            setNotification(pFirebaseMessage.getData().get("message"));
        }


    }

    void setNotification(String pMessgae){
        SharedPreferences lSharedPreference = getSharedPreferences(UtilityClass.MY_SHARED_PREFRENCE, Context.MODE_PRIVATE);

        String lGetPreviousMessage = lSharedPreference.getString(UtilityClass.NOTIFICATION_MESSAGE, null);
        String lNotificationString;
        if (lGetPreviousMessage == null) {
            lNotificationString = pMessgae;
        } else {
            lNotificationString = lGetPreviousMessage + ":" + pMessgae;
        }
        Log.d(TAG, "onMessageReceived:  str " + lNotificationString);

        String[] lAllNotificationMessage = lNotificationString.split(":");
        Notification.InboxStyle lInboxStyle = new Notification.InboxStyle();
        for (String lMessage : lAllNotificationMessage) {
            Log.d(TAG, "onMessageReceived: loop " + lMessage);
            lInboxStyle.addLine(lMessage);
        }
        lInboxStyle.setBigContentTitle("FireBase Message");

        lSharedPreference
                .edit()
                .putString(UtilityClass.NOTIFICATION_MESSAGE, lNotificationString)
                .commit();


        Notification lNotification = new Notification.Builder(this)
                .setContentTitle("FireBaseMessage")
                .setContentText(pMessgae)
                .setSmallIcon(R.drawable.icon)
                .setStyle(lInboxStyle)
                .setContentIntent(PendingIntent.getActivity(this, PENDING_INTENT_REQUEST_CODE, new Intent(MediaPlayerFireBaseService.this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .build();

        NotificationManager lNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        lNotificationManager.notify(UtilityClass.NOTIFICATION_ID, lNotification);
    }

}
