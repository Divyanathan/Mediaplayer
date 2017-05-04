package com.example.user.mediaplayer.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.user.mediaplayer.R;

public class PlaySongActivity extends AppCompatActivity {


    ImageView mSongImage, mPreviousBtnImageView, mNextBtnImageView, mPlayBtnImageView;

    TextView mMinuteTextView, mSecondTextView, mSongDurationTextView;

    TextView mSongTitleTextView, mSongArtistTextView;

    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);

        mSongImage              = (ImageView) findViewById(R.id.PlaysongImage);
        mPreviousBtnImageView   = (ImageView) findViewById(R.id.previousBtnImageView);
        mNextBtnImageView       = (ImageView) findViewById(R.id.nextBtnImageView);
        mPlayBtnImageView       = (ImageView) findViewById(R.id.playBtnImageView);


        mMinuteTextView         = (TextView) findViewById(R.id.minuteTextView);
        mSecondTextView         = (TextView) findViewById(R.id.SongSecondsTextView);
        mSongDurationTextView   = (TextView) findViewById(R.id.durationTextView);

        mSongTitleTextView      = (TextView) findViewById(R.id.titleTextView);
        mSongArtistTextView     = (TextView) findViewById(R.id.artistTextView);

        mProgressBar            = (ProgressBar) findViewById(R.id.progressBar);



    }
}
