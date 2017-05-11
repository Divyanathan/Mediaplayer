package com.example.user.mediaplayer.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.user.mediaplayer.R;
import com.example.user.mediaplayer.dataBase.SongTable;
import com.example.user.mediaplayer.jdo.SongJDO;
import com.example.user.mediaplayer.ui.MainActivity;
import com.example.user.mediaplayer.utility.UtilityClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by user on 06/05/17.
 */


public class MediaPlayerBoundService extends Service {


    IBinder mIBinder = new LocalBinder();
    final int NOTIFICATION_ID = 1;
    int mSongId;
    int mSongColumnId;
    int mSongFavourite;
    int mNumberOfSongs;
    MediaPlayer mMediaPlayer;
    Timer mTimer;
    SongTable mSongTable;
    String mSongTitle, mSongArtist, mSongDuration, mSongAlbumm, mSongImgeUri;
    SongJDO mSongJDObject;
    Boolean mIsAllSongPlayed = false;
    Boolean mIsSongDetailsAvailable = false;
    ArrayList<SongJDO> mSongJDOArrayList = new ArrayList<SongJDO>();;
    private static final String TAG = "MediaPlayerBoundService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: Service =================");

        mSongTable = new SongTable(this);
        mSongTable.open();

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        /**
         * this method will get call once the media player comes to on prepared state then we can play the song from here
         */
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer pMediaPlayer) {
                Log.d(TAG, "testonPrepared: =================");
                pMediaPlayer.start();

                if (mIsAllSongPlayed) {
                    pauseSong();
                    mTimer.cancel();
                    Intent mPassSongValueIntent = new Intent(UtilityClass.PASS_SONG_VALUE_INTENT);
                    mPassSongValueIntent.putExtra(UtilityClass.IS_ALL_SONG_PLAYED, true);
                    LocalBroadcastManager.getInstance(MediaPlayerBoundService.this).sendBroadcast(mPassSongValueIntent);
                    mIsAllSongPlayed = false;
                }
                SharedPreferences mSharedPrefrence = getSharedPreferences(UtilityClass.MY_SHARED_PREFRENCE, MODE_PRIVATE);
                SharedPreferences.Editor lSharedPrefreceEditor = mSharedPrefrence.edit();
                lSharedPrefreceEditor.putInt(UtilityClass.CURRENTLY_PLAYING_SONG, Integer.parseInt(mSongJDOArrayList.get(mSongColumnId).getmSongId()));
                Log.d(TAG, " test OnPreprerd: "+mSongColumnId);
                lSharedPrefreceEditor.commit();

            }
        });
        /**
         * once song is completed this method will get call
         */
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                Boolean lIsPressedByUser = false;
                SharedPreferences lSharedPrefrence = getSharedPreferences(UtilityClass.MY_SHARED_PREFRENCE, Context.MODE_PRIVATE);
                String lSongCurrentStat = lSharedPrefrence.getString(UtilityClass.REPEAT_SONG, UtilityClass.REPEAT_SONG_OFF);
                if (lSongCurrentStat.equals(UtilityClass.REPEAT_SONG_ONCE)) {
                    mMediaPlayer.reset();
                    mTimer.cancel();
                    playTheSong(Integer.parseInt(mSongJDOArrayList.get(mSongColumnId).getmSongId()));
                    Log.d(TAG, " test Oncomplete: "+mSongColumnId);
                } else {
                    mMediaPlayer.reset();
                    playNextSong(lIsPressedByUser);
                    sendValuesTotheActivity();
                }
            }
        });
        /**
         * get the song form local db
         */
        getSongsFromSQLite();

        /**
         * set the notification for service
         */
        Intent lPlySongActivity = new Intent(MediaPlayerBoundService.this, MainActivity.class);
        lPlySongActivity.putExtra(UtilityClass.PENDING_INTENT, true);
        Notification lNotification = new Notification.Builder(this)
                .setContentTitle("Music Player")
                .setContentText("Song is Playing")
                .setSmallIcon(R.drawable.play)
                .setContentIntent(PendingIntent.getActivity(this, 1, lPlySongActivity, PendingIntent.FLAG_UPDATE_CURRENT))
                .build();
        startForeground(NOTIFICATION_ID, lNotification);
    }

    /**
     *
     * @param intent
     * @param flags
     * @param startId
     * @return we are returning the start_stick so that it will restart the service once it is killed by the os
     */
    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        Log.e(TAG, "onStartCommand: =================");

        return START_STICKY;
    }


    /**
     *
     * @param pSongId
     * play the song
     */
    public void playTheSong(final int pSongId) {

        try {
            Uri contentUri = ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, pSongId);
            mMediaPlayer.setDataSource(getApplicationContext(), contentUri);
            mMediaPlayer.prepareAsync();
            setTimer();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * set the timer
     */
    public void setTimer() {

        mTimer = new Timer();
        mTimer.schedule(new MyTimer(), 1000, 1000);

    }

    /***
     * stop the timer from the activity
     */
    public void stopTimer() {
        mTimer.cancel();
    }

    /**
     * timer task which  helps us to schedule the timer
     */
    public class MyTimer extends TimerTask {

        @Override
        public void run() {

            Log.e(TAG, "run: =================");
            if (mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) {

                    if (mMediaPlayer.getDuration() != mMediaPlayer.getCurrentPosition()) {
                        Intent mPassSongValueIntent = new Intent(UtilityClass.PASS_SONG_VALUE_INTENT);
                        mPassSongValueIntent.putExtra(UtilityClass.SENDING_SONG_TIME, true);
                        mPassSongValueIntent.putExtra(UtilityClass.SENDING_SONG_POSITION, mMediaPlayer.getCurrentPosition());
                        LocalBroadcastManager.getInstance(MediaPlayerBoundService.this).sendBroadcast(mPassSongValueIntent);
                        Log.d(TAG, "+++++++++++++++run: " + mMediaPlayer.getCurrentPosition());
                    } else {
                        mTimer.cancel();
                    }
                }
            }
        }
    }

    /**
     * stop the song
     */
    public void stopTheSong() {
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mTimer.cancel();
    }


    /**
     * @param pSeekTime change the position to while playing the song
     */
    public void seekMeadiaPlayer(int pSeekTime) {
        mMediaPlayer.seekTo(pSeekTime);
        setTimer();
    }


    /**
     * @return return true if song is playing
     */
    public Boolean pauseSong() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            setTimer();
            return false;

        } else {

            mMediaPlayer.pause();
            mTimer.cancel();
            return true;
        }
    }


    /**
     *
     * @param pIsPressedByUser which will help us to play the first song wen the user next button wen the last song is playing
     */
    public void playNextSong(Boolean pIsPressedByUser) {
        SharedPreferences lSharedPrefrence = getSharedPreferences(UtilityClass.MY_SHARED_PREFRENCE, Context.MODE_PRIVATE);
        String lSongCurrentStat = lSharedPrefrence.getString(UtilityClass.REPEAT_SONG, UtilityClass.REPEAT_SONG_OFF);

        Log.d(TAG, "test playNextSong: ");
        stopTheSong();

        if (lSongCurrentStat.equals(UtilityClass.REPEAT_SONG_OFF)) {
            if (pIsPressedByUser && mSongColumnId == mNumberOfSongs) {
                mSongColumnId=0;
                playTheSong(Integer.parseInt(mSongJDOArrayList.get(mSongColumnId).getmSongId()));
                Log.d(TAG, " test 1: "+mSongColumnId);
            } else if (mSongColumnId < mNumberOfSongs) {
                playTheSong(Integer.parseInt(mSongJDOArrayList.get(++mSongColumnId).getmSongId()));
                Log.d(TAG, " test 2: "+mSongColumnId);
            } else {
                mSongColumnId=0;
                playTheSong(Integer.parseInt(mSongJDOArrayList.get(mSongColumnId).getmSongId()));
                Log.d(TAG, " test 3: "+mSongColumnId);
                mIsAllSongPlayed = true;
                stopSelf();
            }
        } else if (lSongCurrentStat.equals(UtilityClass.REPEAT_SONG_ALL)) {
            if (mSongColumnId == mNumberOfSongs) {
                mSongColumnId = 0;
                Log.d(TAG, " test 4: "+mSongColumnId);
            }else {
                ++mSongColumnId;
                Log.d(TAG, " test 5: "+mSongColumnId+" "+mNumberOfSongs);
            }
            playTheSong(Integer.parseInt(mSongJDOArrayList.get(mSongColumnId).getmSongId()));
            Log.d(TAG, " test 6: "+mSongColumnId);
        } else {
            playTheSong(Integer.parseInt(mSongJDOArrayList.get(++mSongColumnId).getmSongId()));
            Log.d(TAG, " test 7: "+mSongColumnId);
        }
    }

    /**
     * play the previous song
     */
    public void playPreviousSong() {

        if (mSongColumnId > 0) {
            playTheSong(Integer.parseInt(mSongJDOArrayList.get(--mSongColumnId).getmSongId()));
            Log.d(TAG, " test 8: "+mSongColumnId);
        }else {
            mSongColumnId=mNumberOfSongs;
            playTheSong(Integer.parseInt(mSongJDOArrayList.get(mSongColumnId).getmSongId()));
        }
    }

    /**
     * get the value from the local database
     */
    void getSongsFromSQLite() {

        Cursor lGetSongCursor = mSongTable.getSongs();

        if (lGetSongCursor.moveToFirst()) {

            do {
                int lSongsColumnId = Integer.parseInt(lGetSongCursor.getString(lGetSongCursor.getColumnIndex(mSongTable._ID)));
                int lSongsId = Integer.parseInt(lGetSongCursor.getString(lGetSongCursor.getColumnIndex(mSongTable.SONG_ID)));
                mSongTitle = lGetSongCursor.getString(lGetSongCursor.getColumnIndex(mSongTable.SONG_TITLE));
                mSongArtist = lGetSongCursor.getString(lGetSongCursor.getColumnIndex(mSongTable.SONG_ARTIST));
                mSongDuration = lGetSongCursor.getString(lGetSongCursor.getColumnIndex(mSongTable.SONG_DURATION));
                mSongAlbumm = lGetSongCursor.getString(lGetSongCursor.getColumnIndex(mSongTable.SONG_ALBUM));
                mSongImgeUri = lGetSongCursor.getString(lGetSongCursor.getColumnIndex(mSongTable.SONG_IMAGE));
                mSongFavourite = lGetSongCursor.getInt(lGetSongCursor.getColumnIndex(mSongTable.SONG_FAVOURITE));
                mSongJDObject = new SongJDO(lSongsColumnId, "" + lSongsId, mSongTitle, mSongArtist, mSongDuration, mSongAlbumm, mSongImgeUri, mSongFavourite);
                mSongJDOArrayList.add(mSongJDObject);
            } while (lGetSongCursor.moveToNext());
            mNumberOfSongs = mSongJDOArrayList.size()-1;
        }
    }

    /**
     * local binder class which returns the instance of service so we can access the method from the activity
     */
    public class LocalBinder extends Binder {

        public MediaPlayerBoundService getService() {
            return MediaPlayerBoundService.this;
        }
    }

    /**
     *
     * @param pColumnId
     * set the colum id from the activity
     */
    public void setSongColumID(int pColumnId) {
        mSongColumnId = pColumnId;
        Log.d(TAG, " test setSongColumID: "+mSongColumnId);
    }

    /***
     *  send the value to the Activity
     */
    public void sendValuesTotheActivity() {

        Intent mPassSongValueIntent = new Intent(UtilityClass.PASS_SONG_VALUE_INTENT);
        mSongJDObject = mSongJDOArrayList.get(mSongColumnId);
        Log.d(TAG, " test send value: "+mSongColumnId);
        mPassSongValueIntent.putExtra(UtilityClass.SONG_ID, mSongId);
        mPassSongValueIntent.putExtra(UtilityClass.SONG_TITLE, mSongJDObject.getmSongTitel());
        mPassSongValueIntent.putExtra(UtilityClass.SONG_ARTIST, mSongJDObject.getmSongArtist());
        mPassSongValueIntent.putExtra(UtilityClass.SONG_ALBUM, mSongJDObject.getmSongAlbum());
        mPassSongValueIntent.putExtra(UtilityClass.SONG_DURATION, mSongJDObject.getmSongDuration());
        mPassSongValueIntent.putExtra(UtilityClass.SONG_IMAGE, mSongJDObject.getmSongImage());
        mPassSongValueIntent.putExtra(UtilityClass.FAOURTIE_SONG, mSongJDObject.getmFavourite());

        LocalBroadcastManager.getInstance(this).sendBroadcast(mPassSongValueIntent);
    }

    /**
     *
     * @param pFavourite
     * set the favour value from the activity
     */
    public  void setFavourite(int pFavourite){

        mSongJDOArrayList.get(mSongColumnId).setmFavourite(pFavourite);
    }

    /**
     *
     * @return returns the favourite value to the activity for the current song which is playing
     */
    public  int getFavourite(){
        Log.d(TAG, "getSongsFromSQLite  service"+mSongJDOArrayList.get(mSongColumnId).getmSongId());
        return mSongJDOArrayList.get(mSongColumnId).getmFavourite();
    }

    /**
     *
     * @return returns the current playing song id to activity
     */
    public String getSongId(){
        return ""+mSongJDOArrayList.get(mSongColumnId).getmSongId();
    }

    /**
     * reload the datat base once the favourite value is changed
     */
    public void reloadTheDataBase()
    {
     mSongJDOArrayList.clear();
        getSongsFromSQLite();
    }

    /**
     *
     * @param intent
     * @return Ibinder which helps us to interact withe the activity and the service
     */
    @Override
    public IBinder onBind(final Intent intent) {
        Log.e(TAG, "bindservice onBind: =================");
        return mIBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind: =================");
        if(!mMediaPlayer.isPlaying()){
            stopSelf();
        }
        return true;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: =================");
        mMediaPlayer.stop();
        mMediaPlayer.release();
        super.onDestroy();

    }
}
