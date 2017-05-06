package com.example.user.mediaplayer.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

import com.example.user.mediaplayer.R;

/**
 * Created by user on 06/05/17.
 */

public class MediaPlayerBoundService extends Service {


    private static final String TAG = "MediaPlayerBoundService";

    IBinder mIBinder=new LocalBinder();


    Intent mIntent;

    MediaPlayer lMediaPlayer;

    public class LocalBinder extends Binder{

        public MediaPlayerBoundService getService(){

            return MediaPlayerBoundService.this;

        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: " );

//        LocalBinder lLocalBinder=(LocalBinder)mIBinder;
//        lLocalBinder.getService();
    }

    @Override
    public IBinder onBind(final Intent intent) {


        Log.e(TAG, " onBind: " );

        return mIBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind: " );
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: " );
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        Log.e(TAG, "onStartCommand: ");
        mIntent=intent;

         lMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm_sound );



        lMediaPlayer.start();
//        Log.v(TAG, "onMediaPlayer: " + lMediaPlayer.isPlaying());
//
//        lMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                Log.v(TAG, "onCompletion: " );
//
//                stopSelf(startId);
//            }
//        });

        return START_STICKY;
    }

    public MediaPlayer getMediaPlayer(){
        return lMediaPlayer;
    }
}
