package com.example.user.mediaplayer.ui;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.example.user.mediaplayer.R;
import com.example.user.mediaplayer.adapter.SongRecyclerAdapter;
import com.example.user.mediaplayer.jdo.SongJDO;
import com.example.user.mediaplayer.listener.ReCyclerItemClickListener;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    RecyclerView mRecyclerView;

    ArrayList<SongJDO> mSongJDOArrayList = new ArrayList<>();

    final String IS_PERMISSION_GRANTED = "is_permission_granted";
    MediaPlayer mMediaPlayer;

    final int REQUEST_CODE_TO_GET_PERMISSION = 1;
    final int ID_TO_START_CALLBACK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.mediaPlayerRecylerView);


        mRecyclerView.addOnItemTouchListener(new ReCyclerItemClickListener(this, new ReCyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int postion) {
                SongJDO lSongJDo = mSongJDOArrayList.get(postion);

                Uri contentUri = ContentUris.withAppendedId(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(lSongJDo.getmSongId()));

                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                try {
                    mMediaPlayer.setDataSource(getApplicationContext(), contentUri);
                    mMediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mMediaPlayer.start();

            }
        }));


        SharedPreferences sharedPrefs = getSharedPreferences(IS_PERMISSION_GRANTED, MODE_PRIVATE);

        String lIsDataRetrived = sharedPrefs.getString("granted", "no");

        if (lIsDataRetrived.equals("no")) {

            SharedPreferences.Editor editor = getSharedPreferences(IS_PERMISSION_GRANTED, MODE_PRIVATE).edit();

            editor.putString("granted", "yes");
            editor.commit();

            ensurePermission();


        } else {

//            getTheSong();

            getLoaderManager().initLoader(ID_TO_START_CALLBACK, null, this);

        }


    }


    /**
     * @param pId
     * @param pArgs
     * @return CallBack Methods
     */

    @Override
    public Loader<Cursor> onCreateLoader(int pId, Bundle pArgs) {
        if (pId == ID_TO_START_CALLBACK) {

            return new CursorLoader(this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.TITLE + " ASC");
        }
        return null;
    }


    /***
     *
     * @param pLoader
     * @param pLoaderCursor         Cursor to retrive the value form content provider
     *
     *                              Set the Recycler view Adapter
     */

    @Override
    public void onLoadFinished(Loader<Cursor> pLoader, Cursor pLoaderCursor) {

        switch (pLoader.getId()) {

            case ID_TO_START_CALLBACK:

                String mSongId, mSongTitle, mSongArtist,mSongDuration;

                SongJDO lSongJDObject;

                if (pLoaderCursor.moveToFirst()) {

                    do {

                        mSongId = pLoaderCursor.getString(pLoaderCursor.getColumnIndex(MediaStore.Audio.Media._ID));
                        mSongTitle = pLoaderCursor.getString(pLoaderCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                        mSongArtist = pLoaderCursor.getString(pLoaderCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        mSongDuration=pLoaderCursor.getString(pLoaderCursor.getColumnIndex(MediaStore.Audio.Media.DURATION));


                        lSongJDObject = new SongJDO(mSongId, mSongTitle, mSongArtist,mSongDuration);
                        mSongJDOArrayList.add(lSongJDObject);


                    } while (pLoaderCursor.moveToNext());

                    SongRecyclerAdapter lSongRecyclerAdapter = new SongRecyclerAdapter(this, mSongJDOArrayList);

                    LinearLayoutManager lLinearLayoutManager = new LinearLayoutManager(this);
                    lLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

                    mRecyclerView.setLayoutManager(lLinearLayoutManager);
                    mRecyclerView.setAdapter(lSongRecyclerAdapter);


                }


        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    /**
     * Ensure permission to access the audio
     */

    void ensurePermission() {

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {


            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_TO_GET_PERMISSION);


            }
        } else {
            getLoaderManager().initLoader(ID_TO_START_CALLBACK, null, this);
        }
    }

    /**
     * @param pRequestCode
     * @param pPermissions
     * @param pGrantResults This method  will get call wen the user click allow to access the audio
     */
    public void onRequestPermissionsResult(int pRequestCode, String pPermissions[], int[] pGrantResults) {
        switch (pRequestCode) {
            case REQUEST_CODE_TO_GET_PERMISSION: {

                if (pGrantResults.length > 0
                        && pGrantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    getLoaderManager().initLoader(ID_TO_START_CALLBACK, null, this);

                } else {

                }
                return;
            }

        }
    }

    /**
     * init the callback loader on OnRestart
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        getLoaderManager().initLoader(ID_TO_START_CALLBACK, null, this);
    }
}
