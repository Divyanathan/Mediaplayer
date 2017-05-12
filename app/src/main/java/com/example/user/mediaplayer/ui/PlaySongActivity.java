package com.example.user.mediaplayer.ui;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.user.mediaplayer.R;
import com.example.user.mediaplayer.dataBase.SongTable;
import com.example.user.mediaplayer.jdo.SongJDO;
import com.example.user.mediaplayer.service.MediaPlayerBoundService;
import com.example.user.mediaplayer.utility.UtilityClass;
import com.squareup.picasso.Picasso;

public class PlaySongActivity extends AppCompatActivity {


    ImageView mSongImage, mSongImageDuplicate, mPreviousBtnImageView, mNextBtnImageView, mPlayBtnImageView, mFavourSongImageView, mRepeatSongImageView;
    TextView mMinuteTextView, mSecondTextView, mSongDurationTextView;
    TextView mSongTitleTextView, mSongArtistTextView;
    SeekBar mSeekBar;
    String mSongTitle, mSongArtist, mSongDuration, mSongAlbumm, mSongImgeUri;
    MediaPlayerBoundService mMeadiaPlayerBoundService;
    SongTable mSongTable;
    int mSeekTimer;
    int mSongId, mSongColumnID, mIsSongFavourite;
    int mCurrentlyPlayingSong;
    Boolean mIsBound;
    Boolean mIsSongIsRunninginBackground;
    Boolean mSongIsPllaying = false;
    SharedPreferences mSharedPrefrence;
    SharedPreferences.Editor mSharedPrefrenceEditor;
    Intent mStartService;
    LinearLayout mPlayerLinearLayout;
    SongJDO mSongJDOobject;
    final String TAG = "PlaySongActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);

        mSongTable = new SongTable(this);
        mSongTable.open();

        Log.e(TAG, "onCreate: PlaySongActivity=================");
        mSongImage = (ImageView) findViewById(R.id.PlaysongImage);
        mSongImageDuplicate = (ImageView) findViewById(R.id.PlaysongImageDuplicate);
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
        mPlayerLinearLayout = (LinearLayout) findViewById(R.id.playerLayout);
        mSeekBar = (SeekBar) findViewById(R.id.progressBar);

        mStartService = new Intent(PlaySongActivity.this, MediaPlayerBoundService.class);
        /**
         * set then song details from the listActivity
         */
        mIsSongIsRunninginBackground = getIntent().getBooleanExtra(UtilityClass.SONG_IS_PLAYING_IN_BACKGROUND, false);
        if (mIsSongIsRunninginBackground) {
            bindService(mStartService, mServiceConnection, Context.BIND_AUTO_CREATE);
        } else {

            mSongJDOobject = (SongJDO) getIntent().getSerializableExtra(UtilityClass.SONG_JDO);
            mSongId = Integer.parseInt(mSongJDOobject.getmSongId());
            mSongColumnID = mSongJDOobject.getmColumnId();
            mSongTitle = mSongJDOobject.getmSongTitel();
            mSongArtist = mSongJDOobject.getmSongArtist();
            mSongDuration = mSongJDOobject.getmSongDuration();
            mSongAlbumm = mSongJDOobject.getmSongAlbum();
            mSongImgeUri = mSongJDOobject.getmSongImage();
            mIsSongFavourite = mSongJDOobject.getmFavourite();
            setDetails();
        }

        /**
         *          set the pay and next button image
         */
        mSharedPrefrence = getSharedPreferences(UtilityClass.MY_SHARED_PREFRENCE, Context.MODE_PRIVATE);
        String lCurrentSongState = mSharedPrefrence.getString(UtilityClass.REPEAT_SONG, UtilityClass.REPEAT_SONG_OFF);

        if (lCurrentSongState.equals(UtilityClass.REPEAT_SONG_OFF)) {
            setRepeatSongImage(R.drawable.repeat_off);
        } else if (lCurrentSongState.equals(UtilityClass.REPEAT_SONG_ONCE)) {
            setRepeatSongImage(R.drawable.repeat_once);
        } else {
            setRepeatSongImage(R.drawable.repeat_all);
        }

        /**
         *  start the  service
         */
        if (!getIntent().getBooleanExtra(UtilityClass.SONG_IS_PLAYING_IN_BACKGROUND, false)) {
            if (!isServiceIsRunnig(MediaPlayerBoundService.class)) {
                Log.d(TAG, "Serivice is not running: ");
                mSongIsPllaying = true;
                bindService(mStartService, mServiceConnection, Context.BIND_AUTO_CREATE);
                startService(mStartService);
            } else {
                bindService(mStartService, mServiceConnection, Context.BIND_AUTO_CREATE);
                Log.d(TAG, "bindservice: " + mSongId + " " + mSongColumnID);
            }
        }

        /**
         * Registring the Broadcast Reciver
         */
        LocalBroadcastManager.getInstance(this).registerReceiver(mSongBroadCastReciver, new IntentFilter(UtilityClass.PASS_SONG_VALUE_INTENT));
        /**
         *          set the seek bar listener
         */
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSeekTimer = progress;
                mSecondTextView.setText("" + ((progress / 1000) % 60));
                mMinuteTextView.setText("" + (progress / 60000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mMeadiaPlayerBoundService.stopTimer();
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
                mSongImageDuplicate.setVisibility(View.VISIBLE);
                setTheSongImage(mSongImgeUri);

                Boolean lIsPressedByUser = true;
                mSeekBar.setProgress(0);
                mMeadiaPlayerBoundService.stopTheSong();
                mMeadiaPlayerBoundService.playNextSong(lIsPressedByUser);
                mMeadiaPlayerBoundService.sendValuesTotheActivity();

                Animation lGoingOutAnimation = AnimationUtils.loadAnimation(PlaySongActivity.this, R.anim.slide_out_right);
                Animation lComingInAnimation = AnimationUtils.loadAnimation(PlaySongActivity.this, R.anim.slide_in_right);
                mSongImageDuplicate.setAnimation(lGoingOutAnimation);
                mSongImage.setAnimation(lComingInAnimation);
                mSongImageDuplicate.setVisibility(View.INVISIBLE);


            }
        });
        /**
         *      play The Previous Song
         */
        mPreviousBtnImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTheSongImage(mSongImgeUri);
                mSongImageDuplicate.setVisibility(View.VISIBLE);

                mSeekBar.setProgress(0);
                mMeadiaPlayerBoundService.stopTheSong();
                mMeadiaPlayerBoundService.playPreviousSong();
                mMeadiaPlayerBoundService.sendValuesTotheActivity();

                Animation lGoingOutAnimation = AnimationUtils.loadAnimation(PlaySongActivity.this, R.anim.slide_out_leftt);
                Animation lComingInAnimation = AnimationUtils.loadAnimation(PlaySongActivity.this, R.anim.slide_in_left);
                mSongImageDuplicate.setAnimation(lGoingOutAnimation);
                mSongImage.setAnimation(lComingInAnimation);
                mSongImageDuplicate.setVisibility(View.INVISIBLE);

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

                if (mMeadiaPlayerBoundService.getFavourite() == 0) {
                    setFavoreSongImage(R.drawable.favourite_song);
                    mIsSongFavourite = 1;
                } else {
                    setFavoreSongImage(R.drawable.favourite);
                    mIsSongFavourite = 0;
                }
                mMeadiaPlayerBoundService.setFavourite(mIsSongFavourite);
                mSongTable.updateFavroutieSong("" + mMeadiaPlayerBoundService.getSongId(), mIsSongFavourite);

                Log.d(TAG, "favourite: preference ");
                SharedPreferences.Editor lSharedPrefrence = getSharedPreferences(UtilityClass.MY_SHARED_PREFRENCE, Context.MODE_PRIVATE).edit();
                lSharedPrefrence.putBoolean(UtilityClass.IS_DATABASE_UPDATED, true);
                lSharedPrefrence.commit();

            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    /**
     * BroadCast Reciver to recive the song Details form service
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

            } else if (pIntent.getBooleanExtra(UtilityClass.IS_ALL_SONG_PLAYED, false)) {
                setPlayBtnImage(R.drawable.play);
            } else {
                Log.e(TAG, "onReceive: ");
                mSongId = pIntent.getIntExtra(UtilityClass.SONG_ID, 1);
                mSongTitle = pIntent.getStringExtra(UtilityClass.SONG_TITLE);
                mSongArtist = pIntent.getStringExtra(UtilityClass.SONG_ARTIST);
                mSongDuration = pIntent.getStringExtra(UtilityClass.SONG_DURATION);
                mSongAlbumm = pIntent.getStringExtra(UtilityClass.SONG_ALBUM);
                mSongImgeUri = pIntent.getStringExtra(UtilityClass.SONG_IMAGE);
                mIsSongFavourite = pIntent.getIntExtra(UtilityClass.FAOURTIE_SONG, 0);

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
                .into(mSongImage);

        mSeekBar.setMax(lSongDuration);
        if (mIsSongFavourite == 0) {
            setFavoreSongImage(R.drawable.favourite);
        } else {
            setFavoreSongImage(R.drawable.favourite_song);
        }
    }

    /**
     * @param pImageId image id to set
     */
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

    /**
     * @param pImageId set the favourite image
     */
    void setFavoreSongImage(int pImageId) {
        Picasso.with(PlaySongActivity.this)
                .load("load default image")
                .placeholder(pImageId)
                .resize(200, 200)
                .into(mFavourSongImageView);
    }

    /**
     * @param pImageUrl set the Song image wen we press for the next song so that we can do animation transition
     */
    void setTheSongImage(String pImageUrl) {
        Picasso.with(PlaySongActivity.this)
                .load(pImageUrl)
                .placeholder(R.drawable.song)
                .into(mSongImageDuplicate);
    }

    /**
     * service connection for bind service
     */
    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            MediaPlayerBoundService.LocalBinder lLocalBinder = (MediaPlayerBoundService.LocalBinder) service;
            mMeadiaPlayerBoundService = lLocalBinder.getService();
            mCurrentlyPlayingSong = getSharedPreferences(UtilityClass.MY_SHARED_PREFRENCE, Context.MODE_PRIVATE).getInt(UtilityClass.CURRENTLY_PLAYING_SONG, -1);

            /**
             * check the condition to keep the song playing if it is already playing
             */
            if (mIsSongIsRunninginBackground || mSongId == mCurrentlyPlayingSong) {
                Boolean isDataBaseUpdated = getSharedPreferences(UtilityClass.MY_SHARED_PREFRENCE, Context.MODE_PRIVATE).getBoolean(UtilityClass.IS_DATABASE_UPDATED, false);
                if (isDataBaseUpdated) {
                    Log.d(TAG, "shared preference ");
                    mMeadiaPlayerBoundService.reloadTheDataBase();
                    getSharedPreferences(UtilityClass.MY_SHARED_PREFRENCE, Context.MODE_PRIVATE).edit()
                            .putBoolean(UtilityClass.IS_DATABASE_UPDATED, false)
                            .commit();
                }
                mMeadiaPlayerBoundService.sendValuesTotheActivity();
            } else if (!mSongIsPllaying) {
                /**
                 * stop the old song and play the new song
                 */
                mMeadiaPlayerBoundService.stopTheSong();
                mMeadiaPlayerBoundService.playTheSong(mSongId);
                mMeadiaPlayerBoundService.setSongColumID(mSongColumnID - 1);
            } else {
                /**
                 * play the new song wen the applicaiton is started
                 */
                mMeadiaPlayerBoundService.playTheSong(mSongId);
                mMeadiaPlayerBoundService.setSongColumID(mSongColumnID - 1);
            }

            Log.d(TAG, " test setSongColumID: " + mSongColumnID);

            mIsBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsBound = false;
        }
    };


    @Override
    protected void onDestroy() {
        unbindService(mServiceConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mSongBroadCastReciver);
        mSongTable.close();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent lIntent = new Intent();
        setResult(RESULT_OK, lIntent);
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_leftt);

    }

    /**
     * chek the service is running or not
     */
    boolean isServiceIsRunnig(Class<?> pServicName) {
        ActivityManager lActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo lRunningServiceInfo : lActivityManager.getRunningServices(Integer.MAX_VALUE)) {

            if (lRunningServiceInfo.service.getClassName().equals(pServicName.getName())) {
                return true;
            }
        }
        return false;
    }


}
