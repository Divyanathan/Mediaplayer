package com.example.user.mediaplayer.ui;

import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import com.example.user.mediaplayer.R;
import com.example.user.mediaplayer.adapter.SongRecyclerAdapter;
import com.example.user.mediaplayer.dataBase.SongTable;
import com.example.user.mediaplayer.jdo.SongJDO;
import com.example.user.mediaplayer.listener.OnClick;
import com.example.user.mediaplayer.listener.ReCyclerItemClickListener;
import com.example.user.mediaplayer.service.MediaPlayerBoundService;
import com.example.user.mediaplayer.service.MediaPlayerBoundService.LocalBinder;
import com.example.user.mediaplayer.utility.UtilityClass;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    RecyclerView mRecyclerView;

    ArrayList<SongJDO> mSongJDOArrayList = new ArrayList<>();

    String mSongId, mSongTitle, mSongArtist, mSongDuration, mSongAlbumm, mSongAbumId, mSongImgeUri,mSongColumnId;

    int  mSongFavourite;

    final String IS_PERMISSION_GRANTED = "is_permission_granted";
    MediaPlayer mMediaPlayer;

    final int REQUEST_CODE_TO_GET_PERMISSION = 1;
    final int ID_TO_START_CALLBACK = 1;

    final int REQUEST_CODE_FOR_INTENT = 1;

    Intent mStartService;

    MediaPlayerBoundService mMediaPlayerBoundService;

    SongTable mSongTable;
    SongJDO mSongJDObject;

    SharedPreferences mSharedPrefrence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.mediaPlayerRecylerView);

        mSongTable = new SongTable(this);
        mSongTable.open();
        mRecyclerView.addOnItemTouchListener(new ReCyclerItemClickListener(this, new OnClick() {
            @Override
            public void onItemClick(View v, int postion) {


                Intent lPlaySongIntent = new Intent(MainActivity.this, PlaySongActivity.class);


                startActivityForResult(lPlaySongIntent,REQUEST_CODE_FOR_INTENT);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_leftt);




                mSharedPrefrence=getSharedPreferences(UtilityClass.MY_SHARED_PREFRENCE,MODE_PRIVATE);
                SharedPreferences.Editor lSharedPrefreceEditor=mSharedPrefrence.edit();
                lSharedPrefreceEditor.putInt(mSongTable.SONG_ID,Integer.parseInt(mSongJDOArrayList.get(postion).getmSongId()));
                lSharedPrefreceEditor.commit();

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

            getLoaderManager().initLoader(ID_TO_START_CALLBACK, null, this);

        }



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_CODE_FOR_INTENT) {
            if (resultCode == RESULT_OK) {
                mSongJDOArrayList.clear();
                getSongsFromSQLite();
                Log.d("ojdsf", "-------------------onActivityResult: ");
            }
        }
    }

    /**
     * get the song from local db
     */

    void getSongsFromSQLite() {

        Cursor lGetSongCursor = mSongTable.getSongs();

        if (lGetSongCursor.moveToFirst()) {


            do {
                mSongColumnId=lGetSongCursor.getString(lGetSongCursor.getColumnIndex(mSongTable._ID));
                mSongId = lGetSongCursor.getString(lGetSongCursor.getColumnIndex(mSongTable.SONG_ID));
                mSongTitle = lGetSongCursor.getString(lGetSongCursor.getColumnIndex(mSongTable.SONG_TITLE));
                mSongArtist = lGetSongCursor.getString(lGetSongCursor.getColumnIndex(mSongTable.SONG_ARTIST));
                mSongDuration = lGetSongCursor.getString(lGetSongCursor.getColumnIndex(mSongTable.SONG_DURATION));
                mSongAlbumm = lGetSongCursor.getString(lGetSongCursor.getColumnIndex(mSongTable.SONG_ALBUM));
                mSongImgeUri = lGetSongCursor.getString(lGetSongCursor.getColumnIndex(mSongTable.SONG_IMAGE));
                mSongFavourite=lGetSongCursor.getInt(lGetSongCursor.getColumnIndex(mSongTable.SONG_FAVOURITE));

                mSongJDObject = new SongJDO(mSongId, mSongTitle, mSongArtist, mSongDuration, mSongAlbumm, mSongImgeUri, mSongFavourite );
                mSongJDOArrayList.add(mSongJDObject);
            } while (lGetSongCursor.moveToNext());
        }


        /**
         *          set the adapter for Recycler View
         */

        SongRecyclerAdapter lSongRecyclerAdapter = new SongRecyclerAdapter(this, mSongJDOArrayList);

        LinearLayoutManager lLinearLayoutManager = new LinearLayoutManager(this);
        lLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(lLinearLayoutManager);
        mRecyclerView.setAdapter(lSongRecyclerAdapter);


    }


    /**
     * connection to the bound service from the Activity
     */
    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder pIBinderService) {

            Log.e("MediaPlayerBoundService", "onServiceConnected: ");

            LocalBinder lLocalbinder = (LocalBinder) pIBinderService;

            mMediaPlayerBoundService = lLocalbinder.getService();


            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.e("MediaPlayerBoundService", "onCompletion: ");

//                    mMediaPlayer.start();
                    Log.e("MediaPlayerBoundService", "started again: ");

                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("MediaPlayerBoundService", "onServiceDisconnected: ");

        }
    };


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


                if (pLoaderCursor.moveToFirst()) {

                    do {

                        mSongId = pLoaderCursor.getString(pLoaderCursor.getColumnIndex(MediaStore.Audio.Media._ID));
                        mSongTitle = pLoaderCursor.getString(pLoaderCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                        mSongArtist = pLoaderCursor.getString(pLoaderCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        mSongDuration = pLoaderCursor.getString(pLoaderCursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                        mSongAbumId = pLoaderCursor.getString(pLoaderCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                        mSongAlbumm = pLoaderCursor.getString(pLoaderCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));

                        mSongImgeUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), Long.parseLong(mSongAbumId)).toString();


                        if (!mSongTable.isSongExist(mSongId)) {
                            mSongTable.addSongs(mSongId, mSongAlbumm, mSongTitle, mSongArtist, mSongDuration, mSongImgeUri);
                        }

                    } while (pLoaderCursor.moveToNext());


                }
                getSongsFromSQLite();

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
