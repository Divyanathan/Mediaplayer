package com.example.user.mediaplayer.ui;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.user.mediaplayer.R;
import com.example.user.mediaplayer.custom.RoundedTransformation;
import com.example.user.mediaplayer.dataBase.SongTable;
import com.example.user.mediaplayer.service.MediaPlayerBoundService;
import com.example.user.mediaplayer.utility.UtilityClass;
import com.squareup.picasso.Picasso;

public class PlaySongActivity extends AppCompatActivity {


    ImageView mSongImage, mPreviousBtnImageView, mNextBtnImageView, mPlayBtnImageView, mFavourSongImageView, mRepeatSongImageView;

    TextView mMinuteTextView, mSecondTextView, mSongDurationTextView;

    TextView mSongTitleTextView, mSongArtistTextView;

    SeekBar mSeekBar;


    String mSongTitle, mSongArtist, mSongDuration, mSongAlbumm, mSongImgeUri;

    MediaPlayerBoundService mMeadiaPlayerBoundService;

    SongTable mSongTable;
    int mSeekTimer;
    int mSongId,mIsSongFavourite;

    final String TAG = "PlaySongActivity";


    SharedPreferences mSharedPrefrence;
    SharedPreferences.Editor mSharedPrefrenceEditor;
    Intent mStartService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);

        mSongTable=new SongTable(this);
        mSongTable.open();

        Log.e(TAG, "onCreate: PlaySongActivity=================");
        mSongImage = (ImageView) findViewById(R.id.PlaysongImage);
        mPreviousBtnImageView = (ImageView) findViewById(R.id.previousBtnImageView);
        mNextBtnImageView = (ImageView) findViewById(R.id.nextBtnImageView);
        mPlayBtnImageView = (ImageView) findViewById(R.id.playBtnImageView);

        mRepeatSongImageView = (ImageView) findViewById(R.id.repeatSong);
        mFavourSongImageView = (ImageView) findViewById(R.id.favourtieSong);


        mMinuteTextView = (TextView) findViewById(R.id.minuteTextView);
        mSecondTextView = (TextView) findViewById(R.id.SongSecondsTextView);
        mSongDurationTextView = (TextView) findViewById(R.id.durationTextView);

        mSongTitleTextView = (TextView) findViewById(R.id.titleTextView);
        mSongArtistTextView = (TextView) findViewById(R.id.artistTextView);

        mStartService = new Intent(PlaySongActivity.this, MediaPlayerBoundService.class);

        mSharedPrefrence = getSharedPreferences(UtilityClass.MY_SHARED_PREFRENCE, Context.MODE_PRIVATE);

        String lCurrentSongState=mSharedPrefrence.getString(UtilityClass.REPEAT_SONG,UtilityClass.REPEAT_SONG_OFF);

        if (lCurrentSongState.equals(UtilityClass.REPEAT_SONG_OFF)) {

            setRepeatSongImage(R.drawable.repeat_off);

        } else if (lCurrentSongState.equals(UtilityClass.REPEAT_SONG_ONCE)) {

            setRepeatSongImage(R.drawable.repeat_once);

        } else {

            setRepeatSongImage(R.drawable.repeat_all);

        }


        if (isMediaPlayerServiceRunning(MediaPlayerBoundService.class)) {

            Log.d(TAG, "=======Service is running: ");
            stopService(mStartService);

        }
        Log.d(TAG, "===========start the service: ");
        startService(mStartService);
        bindService(mStartService, mServiceConnection, Context.BIND_AUTO_CREATE);


        /**
         *          Registring the Broadcast Reciver
         */
        LocalBroadcastManager.getInstance(this).registerReceiver(mSongBroadCastReciver,
                new IntentFilter(UtilityClass.PASS_SONG_VALUE_INTENT));

        mSeekBar = (SeekBar) findViewById(R.id.progressBar);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                mSeekTimer = progress;
                mSecondTextView.setText("" + ((progress / 1000) % 60));
                mMinuteTextView.setText("" + (progress / 60000));


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                mMeadiaPlayerBoundService.seekMeadiaPlayer(mSeekTimer);
            }
        });


        /**
         *              play/pause button
         */


        mPlayBtnImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mMeadiaPlayerBoundService.pauseSong()) {
                    setPlayBtnImage(R.drawable.pause);
                } else {

                    setPlayBtnImage(R.drawable.play);
                }

            }
        });


        /**
         *          play next Song
         */
        mNextBtnImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Boolean lIsPressedByUser=true;
                mSeekBar.setProgress(0);
                mMeadiaPlayerBoundService.playNextSong(lIsPressedByUser);

            }
        });


        /**
         *      play The Previous Song
         */

        mPreviousBtnImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mSeekBar.setProgress(0);
                mMeadiaPlayerBoundService.playPreviousSong();


            }
        });

        /**
         *      click event For rpeatSong
         */
        mRepeatSongImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String lCurrenctStat = mSharedPrefrence.getString(UtilityClass.REPEAT_SONG, UtilityClass.REPEAT_SONG_OFF);
                mSharedPrefrenceEditor = mSharedPrefrence.edit();
                if (lCurrenctStat.equals(UtilityClass.REPEAT_SONG_OFF)) {

                    setRepeatSongImage(R.drawable.repeat_once);
                    mSharedPrefrenceEditor.putString(UtilityClass.REPEAT_SONG, UtilityClass.REPEAT_SONG_ONCE);

                } else if (lCurrenctStat.equals(UtilityClass.REPEAT_SONG_ONCE)) {
                    setRepeatSongImage(R.drawable.repeat_all);
                    mSharedPrefrenceEditor.putString(UtilityClass.REPEAT_SONG, UtilityClass.REPEAT_SONG_ALL);
                } else {
                    setRepeatSongImage(R.drawable.repeat_off);
                    mSharedPrefrenceEditor.putString(UtilityClass.REPEAT_SONG, UtilityClass.REPEAT_SONG_OFF);
                }
                mSharedPrefrenceEditor.commit();

            }
        });

        /**
         *          click Event for  favourite song
         */
        mFavourSongImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (mSongTable.getFavrotieSong(""+mSongId)==0){
                    setFavoreSongImage(R.drawable.favourite_song);
                    mIsSongFavourite=1;
                    mSongTable.updateFavroutieSong(""+mSongId,1);
                }else {
                    setFavoreSongImage(R.drawable.favourite);
                    mIsSongFavourite=0;
                    mSongTable.updateFavroutieSong(""+mSongId,0);
                }

            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    /**
     * BroadCast Reciver to recive the song Details
     */


    BroadcastReceiver mSongBroadCastReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent pIntent) {


            if (pIntent.getBooleanExtra(UtilityClass.SENDING_SONG_TIME, false)) {


                int lSongCurrentPosition = pIntent.getIntExtra(UtilityClass.SENDING_SONG_POSITION, 0);
                mSeekBar.setProgress(lSongCurrentPosition);
                mMinuteTextView.setText("" + lSongCurrentPosition / 60000);
                mSecondTextView.setText("" + (lSongCurrentPosition / 1000) % 60);
                Log.d(TAG, "+++++++++++++++run:set Duration: " + (lSongCurrentPosition / 60) + " sec" + (lSongCurrentPosition / 60) % 60);

            } else if(pIntent.getBooleanExtra(UtilityClass.IS_ALL_SONG_PLAYED,false)){

                setPlayBtnImage(R.drawable.play);

            }
            else {
                Log.e(TAG, "onReceive: ");
                mSongId=pIntent.getIntExtra(UtilityClass.SONG_ID,1);
                mSongTitle = pIntent.getStringExtra(UtilityClass.SONG_TITLE);
                mSongArtist = pIntent.getStringExtra(UtilityClass.SONG_ARTIST);
                mSongDuration = pIntent.getStringExtra(UtilityClass.SONG_DURATION);
                mSongAlbumm = pIntent.getStringExtra(UtilityClass.SONG_ALBUM);
                mSongImgeUri = pIntent.getStringExtra(UtilityClass.SONG_IMAGE);
                mIsSongFavourite=pIntent.getIntExtra(UtilityClass.FAOURTIE_SONG,0);

                mSeekBar.setProgress(0);
                setDetails();
            }


        }
    };

    /**
     * Set the song details
     */
    void setDetails() {
        Log.e(TAG, "setDetails: ");
        mSongTitleTextView.setText(mSongTitle);
        mSongArtistTextView.setText(mSongAlbumm);
        int lSongDuration = Integer.parseInt(mSongDuration);
        mSongDurationTextView.setText("" + (lSongDuration / 60000) + ":" + (lSongDuration / 1000) % 60);

        Picasso.with(PlaySongActivity.this)
                .load(mSongImgeUri)
                .placeholder(R.drawable.song)
                .resize(200, 200)
                .into(mSongImage);

        mSeekBar.setMax(lSongDuration);
        if (mIsSongFavourite==0){
            setFavoreSongImage(R.drawable.favourite);
        }else {
            setFavoreSongImage(R.drawable.favourite_song);
        }
    }


    void setRepeatSongImage(int pImageId) {
        Picasso.with(PlaySongActivity.this)
                .load(pImageId)
                .resize(200, 200)
                .into(mRepeatSongImageView);
    }

    /**
     * set the play Button Image as Play/Pause
     */

    void setPlayBtnImage(int pImageId) {


        Picasso.with(PlaySongActivity.this)
                .load("load default image")
                .placeholder(pImageId)
                .resize(200, 200)
                .into(mPlayBtnImageView);
    }
    void setFavoreSongImage(int pImageId) {


        Picasso.with(PlaySongActivity.this)
                .load("load default image")
                .placeholder(pImageId)
                .resize(200, 200)
                .into(mFavourSongImageView);
    }

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            MediaPlayerBoundService.LocalBinder lLocalBinder = (MediaPlayerBoundService.LocalBinder) service;
            mMeadiaPlayerBoundService = lLocalBinder.getService();


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    protected void onDestroy() {


        unbindService(mServiceConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mSongBroadCastReciver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        Intent lIntent=new Intent();
        setResult(RESULT_OK,lIntent);
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);

    }

    boolean isMediaPlayerServiceRunning(Class<?> pServiceClass) {

        ActivityManager lActivityManger = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo lRunningService : lActivityManger.getRunningServices(Integer.MAX_VALUE)) {

            if (pServiceClass.getName().equals(lRunningService.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
