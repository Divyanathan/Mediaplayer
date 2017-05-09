package com.example.user.mediaplayer.demo.sercvice;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by user on 05/05/17.
 */

public class DemoMediaPlayerIntentService extends IntentService {


    final String TAG="MediaPlayer";


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param pClassName Used to pClassName the worker thread, important only for debugging.
     */
    public DemoMediaPlayerIntentService(String pClassName) {
        super(pClassName);
    }


    public DemoMediaPlayerIntentService() {
        super("calling the super class");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {


        try {
            for (int i=0;i<20;i++) {
                Thread.sleep(2000);
                Log.e(TAG, "onHandleIntent: " );
                Toast.makeText(this, "service", Toast.LENGTH_SHORT).show();
            }
        } catch (InterruptedException e) {
            // Restore interrupt status.
//            Thread.currentThread().interrupt();
        }


    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: " );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e(TAG, "onDestroy: " );
    }
}
