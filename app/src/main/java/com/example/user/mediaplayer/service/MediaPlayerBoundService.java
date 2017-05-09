package com.example.user.mediaplayer.service;

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

import com.example.user.mediaplayer.dataBase.SongTable;
import com.example.user.mediaplayer.utility.UtilityClass;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by user on 06/05/17.
 */


public class MediaPlayerBoundService extends Service {


    private static final String TAG = "MediaPlayerBoundService";

    IBinder mIBinder = new LocalBinder();


    MediaPlayer mMediaPlayer;
    Timer mTimer;

    SongTable mSongTable;
    int mSongId;
    int mSongColumnId;

    Boolean mIsAllSongPlayed = false;

    public class LocalBinder extends Binder {

        public MediaPlayerBoundService getService() {

            return MediaPlayerBoundService.this;

        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: Service =================");

//        LocalBinder lLocalBinder=(LocalBinder)mIBinder;
//        lLocalBinder.getService();
    }

    @Override
    public IBinder onBind(final Intent intent) {


        Log.e(TAG, " onBind: =================");

        return mIBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind: =================");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: =================");
        setValueInSharedPreference(false);
        stopTheSong();
        mTimer.cancel();
        super.onDestroy();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        Log.e(TAG, "onStartCommand: =================");
        setValueInSharedPreference(true);
        mSongTable = new SongTable(this);
        mSongTable.open();

        SharedPreferences lSharedPrefrence = getSharedPreferences(UtilityClass.MY_SHARED_PREFRENCE, MODE_PRIVATE);
        mSongId = lSharedPrefrence.getInt(mSongTable.SONG_ID, 0);
        playTheSong(mSongId);

        return START_STICKY;
    }

    void setValueInSharedPreference(Boolean pIsSongPlaying) {
        SharedPreferences.Editor lSharedPrefrenceEditor = getSharedPreferences(UtilityClass.MY_SHARED_PREFRENCE, Context.MODE_PRIVATE).edit();
        lSharedPrefrenceEditor.putBoolean(UtilityClass.IS_SONG_PLAYING, pIsSongPlaying);
        lSharedPrefrenceEditor.commit();

    }


    public void playTheSong(int pSongId) {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        Cursor lSongDetailCursor = mSongTable.getSongDetails("" + pSongId);
        lSongDetailCursor.moveToFirst();

        Log.d(TAG, "=================playTheSong: " +
                lSongDetailCursor.getString(lSongDetailCursor.getColumnIndex(mSongTable.SONG_TITLE)) +
                lSongDetailCursor.getString(lSongDetailCursor.getColumnIndex(mSongTable.SONG_ARTIST)) +
                lSongDetailCursor.getString(lSongDetailCursor.getColumnIndex(mSongTable.SONG_ALBUM)) +
                lSongDetailCursor.getString(lSongDetailCursor.getColumnIndex(mSongTable.SONG_DURATION)) +
                lSongDetailCursor.getString(lSongDetailCursor.getColumnIndex(mSongTable.SONG_IMAGE)));

        Intent mPassSongValueIntent = new Intent(UtilityClass.PASS_SONG_VALUE_INTENT);

        mSongColumnId = lSongDetailCursor.getInt(lSongDetailCursor.getColumnIndex(mSongTable._ID));

        mPassSongValueIntent.putExtra(UtilityClass.SONG_ID, pSongId);
        mPassSongValueIntent.putExtra(UtilityClass.SONG_TITLE, lSongDetailCursor.getString(lSongDetailCursor.getColumnIndex(mSongTable.SONG_TITLE)));
        mPassSongValueIntent.putExtra(UtilityClass.SONG_ARTIST, lSongDetailCursor.getString(lSongDetailCursor.getColumnIndex(mSongTable.SONG_ARTIST)));
        mPassSongValueIntent.putExtra(UtilityClass.SONG_ALBUM, lSongDetailCursor.getString(lSongDetailCursor.getColumnIndex(mSongTable.SONG_ALBUM)));
        mPassSongValueIntent.putExtra(UtilityClass.SONG_DURATION, lSongDetailCursor.getString(lSongDetailCursor.getColumnIndex(mSongTable.SONG_DURATION)));
        mPassSongValueIntent.putExtra(UtilityClass.SONG_IMAGE, lSongDetailCursor.getString(lSongDetailCursor.getColumnIndex(mSongTable.SONG_IMAGE)));
        mPassSongValueIntent.putExtra(UtilityClass.FAOURTIE_SONG, lSongDetailCursor.getInt(lSongDetailCursor.getColumnIndex(mSongTable.SONG_FAVOURITE)));

        LocalBroadcastManager.getInstance(this).sendBroadcast(mPassSongValueIntent);

        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, pSongId);

        try {
            mMediaPlayer.setDataSource(getApplicationContext(), contentUri);
            mMediaPlayer.prepareAsync();
//                    mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer pMediaPlayer) {

                Log.d(TAG, "onPrepared: =================");
                pMediaPlayer.start();
                if (mIsAllSongPlayed) {
                    pauseSong();
                    Intent mPassSongValueIntent = new Intent(UtilityClass.PASS_SONG_VALUE_INTENT);
                    mPassSongValueIntent.putExtra(UtilityClass.IS_ALL_SONG_PLAYED, true);

                    LocalBroadcastManager.getInstance(MediaPlayerBoundService.this).sendBroadcast(mPassSongValueIntent);
                    mIsAllSongPlayed = false;
                }
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                Boolean lIsPressedByUser = false;
                SharedPreferences lSharedPrefrence = getSharedPreferences(UtilityClass.MY_SHARED_PREFRENCE, Context.MODE_PRIVATE);
                String lSongCurrentStat = lSharedPrefrence.getString(UtilityClass.REPEAT_SONG, UtilityClass.REPEAT_SONG_OFF);
                if (lSongCurrentStat.equals(UtilityClass.REPEAT_SONG_ONCE)) {
                    playTheSong(mSongId);
                } else {
                    playNextSong(lIsPressedByUser);
                }

            }
        });

        setTimer();

    }

    void setTimer() {

        mTimer = new Timer();
        mTimer.schedule(new MyTimer(), 1000, 1000);

    }

    public class MyTimer extends TimerTask {

        @Override
        public void run() {

            Log.e(TAG, "run: =================");

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

    public void stopTheSong() {
        mMediaPlayer.stop();
        mMediaPlayer.release();
    }


    /**
     * @param pSeekTime change the position to while playing the song
     */
    public void seekMeadiaPlayer(int pSeekTime) {
        mMediaPlayer.seekTo(pSeekTime);
    }


    /**
     * @return return true if song is playing
     */
    public Boolean pauseSong() {
        if (!mMediaPlayer.isPlaying()) {

            mMediaPlayer.start();
            return false;

        } else {

            mMediaPlayer.pause();
            return true;
        }
    }


    void getSongId(int pSongCoumnId) {

        stopTheSong();
        Cursor lSongIdCursor = mSongTable.getSongId("" + pSongCoumnId);
        if (lSongIdCursor.moveToFirst()) {
            playTheSong(lSongIdCursor.getInt(0));
        }
    }


    public void playNextSong(Boolean pIsPressedByUser) {

        SharedPreferences lSharedPrefrence = getSharedPreferences(UtilityClass.MY_SHARED_PREFRENCE, Context.MODE_PRIVATE);
        String lSongCurrentStat = lSharedPrefrence.getString(UtilityClass.REPEAT_SONG, UtilityClass.REPEAT_SONG_OFF);

        int lLastSong = mSongTable.getTheLastSongId();
        if (lSongCurrentStat.equals(UtilityClass.REPEAT_SONG_OFF)) {

            mTimer.cancel();

            if (pIsPressedByUser && mSongColumnId == lLastSong) {

                getSongId(1);

            } else if (mSongColumnId < lLastSong) {

                getSongId(mSongColumnId + 1);
            } else {
                getSongId(1);
                mIsAllSongPlayed = true;
            }
        } else if (lSongCurrentStat.equals(UtilityClass.REPEAT_SONG_ALL)) {
            mTimer.cancel();
            if (mSongColumnId == lLastSong) {
                mSongColumnId = 0;

            }
            getSongId(mSongColumnId + 1);
        }


    }

    public void playPreviousSong() {

        if (mSongColumnId > 1) {
            getSongId(mSongColumnId - 1);
            mTimer.cancel();
        }
    }


}
