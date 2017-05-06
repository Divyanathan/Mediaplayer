package com.example.user.mediaplayer.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import com.example.user.mediaplayer.R;

/**
 * Created by user on 05/05/17
 */

public class MediaPlayerUnBoundService extends Service {

    private static final String TAG = "MediaPlayerUnBoundService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate: ");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onBind: ");
        MediaPlayer lMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm_sound );

        Log.v(TAG, "onStartCommand: " + lMediaPlayer.isPlaying());


        lMediaPlayer.start();
        lMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.v(TAG, "onCompletion: " );
                stopSelf();

            }
        });
        return null;

    }

        @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        MediaPlayer lMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm_sound );

           Log.v(TAG, "onStartCommand: " + lMediaPlayer.isPlaying());


        lMediaPlayer.start();
        lMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.v(TAG, "onCompletion: " );
                stopSelf();

            }
        });

        return START_STICKY;

    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        Log.v(TAG, "onDestroy: ");

    }
}
