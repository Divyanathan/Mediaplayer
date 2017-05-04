package com.example.user.mediaplayer.ui;

import android.content.ContentUris;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.user.mediaplayer.R;
import com.example.user.mediaplayer.custom.RoundedTransformation;
import com.example.user.mediaplayer.jdo.SongJDO;
import com.example.user.mediaplayer.utility.UtilityClass;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import java.util.logging.LogRecord;

public class PlaySongActivity extends AppCompatActivity {


    ImageView mSongImage, mPreviousBtnImageView, mNextBtnImageView, mPlayBtnImageView;

    TextView mMinuteTextView, mSecondTextView, mSongDurationTextView;

    TextView mSongTitleTextView, mSongArtistTextView;

    SeekBar mSeekBar;

    Handler mHandler = new Handler();
    int mItemPostion;
    int mSeekTimer;

    final String TAG = "PlaySongActivity";
    ArrayList<SongJDO> mSongJDOArrayList;
    SongJDO mSongJDOobj;

    MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);

        mSongImage = (ImageView) findViewById(R.id.PlaysongImage);
        mPreviousBtnImageView = (ImageView) findViewById(R.id.previousBtnImageView);
        mNextBtnImageView = (ImageView) findViewById(R.id.nextBtnImageView);
        mPlayBtnImageView = (ImageView) findViewById(R.id.playBtnImageView);


        mMinuteTextView = (TextView) findViewById(R.id.minuteTextView);
        mSecondTextView = (TextView) findViewById(R.id.SongSecondsTextView);
        mSongDurationTextView = (TextView) findViewById(R.id.durationTextView);

        mSongTitleTextView = (TextView) findViewById(R.id.titleTextView);
        mSongArtistTextView = (TextView) findViewById(R.id.artistTextView);




        mItemPostion = getIntent().getIntExtra(UtilityClass.RECYCLER_ITEM_POSITION, 0);

        mSongJDOArrayList = (ArrayList<SongJDO>) getIntent().getSerializableExtra(UtilityClass.SONG_JDO_ARRAY_LIST);

        mSeekBar = (SeekBar) findViewById(R.id.progressBar);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if(mMediaPlayer != null && fromUser){
                    mSeekTimer=progress;
                    mSecondTextView.setText(""+((progress/1000)%60));
                    mMinuteTextView.setText(""+(progress/60000));
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                mMediaPlayer.seekTo(mSeekTimer);
            }
        });

        /**
         *          start the song
         */

        playSong(mItemPostion);


        /**
         *              play/pause button
         */


        mPlayBtnImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mMediaPlayer.isPlaying()) {

                    mMediaPlayer.start();
                    setPlayBtnImage(R.drawable.pause);

                } else {

                    setPlayBtnImage(R.drawable.play);
                    mMediaPlayer.pause();

                }

            }
        });


        /**
         *          play next Song
         */
        mNextBtnImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stopTheSong();

                if (++mItemPostion == mSongJDOArrayList.size()) {
                    mItemPostion = 0;
                }
                playSong(mItemPostion);


            }
        });


        /**
         *      play The Previous Song
         */

        mPreviousBtnImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                stopTheSong();


                if (mItemPostion == 0) {
                    mItemPostion = mSongJDOArrayList.size();
                }
                playSong(--mItemPostion);


            }
        });


        /**
         *          set the Seek bar progress
         */

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mMediaPlayer != null) {

                    if (mMediaPlayer.isPlaying()) {
                        int lMinute, lSeconds;
                        lMinute = Integer.parseInt(mMinuteTextView.getText().toString());
                        lSeconds = Integer.parseInt(mSecondTextView.getText().toString());
                        if (lSeconds == 59) {
                            mSecondTextView.setText("00");
                            mMinuteTextView.setText("" + (lMinute + 1));
                        } else {
                            mSecondTextView.setText("" + (lSeconds + 1));
                        }

                        int lCurrentTime = (mMediaPlayer.getCurrentPosition());
                        mSeekBar.setProgress(lCurrentTime);
                        Log.e(TAG, "run:  " + mMediaPlayer.getCurrentPosition() / 1000 + "\n" + mMediaPlayer.getDuration());
                    }


                    mHandler.postDelayed(this, 1000);
                }


            }
        });

    }



    /**
     * Stop the Song
     */

    void stopTheSong() {
        mMediaPlayer.stop();
        mMediaPlayer.release();
    }

    /***
     *
     * @param pSongPosition Song Position in the RecyclerView List
     *
     *                      This method is used play a particular song
     */

    void playSong(int pSongPosition) {



        mMinuteTextView.setText("0");
        mSecondTextView.setText("0");

        mSeekBar.setProgress(0);

        setPlayBtnImage(R.drawable.pause);

        SongJDO lSongJDo = mSongJDOArrayList.get(pSongPosition);

        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(lSongJDo.getmSongId()));

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer pMediaPlayer) {

                pMediaPlayer.start();
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                if (++mItemPostion != mSongJDOArrayList.size()) {
                    playSong(mItemPostion);
                }

            }
        });

        try {
            mMediaPlayer.setDataSource(getApplicationContext(), contentUri);
            mMediaPlayer.prepareAsync();
//                    mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


        mSongTitleTextView.setText(lSongJDo.getmSongTitel());

        mSongArtistTextView.setText(lSongJDo.getmSongArtist());

        int lSongDuration = Integer.parseInt(lSongJDo.getmSongDuration());
        mSongDurationTextView.setText("" + (lSongDuration) / 60000 + ":" + ((lSongDuration) / 1000) % 60);

        mSeekBar.setMax(Integer.parseInt(lSongJDo.getmSongDuration()));

    }


    /**
     * set the play Button Image as Play/Pause
     */

    void setPlayBtnImage(int pImageId) {


        Picasso.with(PlaySongActivity.this)
                .load("load default image")
                .placeholder(pImageId)
                .resize(200, 200)
                .transform(new RoundedTransformation(100, 1))
                .into(mPlayBtnImageView);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        stopTheSong();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right);
    }
}
