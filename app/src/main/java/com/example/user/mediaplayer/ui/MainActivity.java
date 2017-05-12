package com.example.user.mediaplayer.ui;

import android.app.ActivityManager;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.example.user.mediaplayer.R;
import com.example.user.mediaplayer.adapter.SongRecyclerAdapter;
import com.example.user.mediaplayer.dataBase.SongTable;
import com.example.user.mediaplayer.jdo.SongJDO;
import com.example.user.mediaplayer.service.MediaPlayerBoundService;
import com.example.user.mediaplayer.utility.UtilityClass;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    RecyclerView mRecyclerView;

    ArrayList<SongJDO> mSongJDOArrayList = new ArrayList<>();
    ArrayList<SongJDO> mSearchSongArryList = new ArrayList<>();
    String mSongId, mSongTitle, mSongArtist, mSongDuration, mSongAlbumm, mSongAbumId, mSongImgeUri, mSongColumnId;
    int mSongFavourite;
    final String IS_PERMISSION_GRANTED = "is_permission_granted";
    final int REQUEST_CODE_TO_GET_PERMISSION = 1;
    final int ID_TO_START_CALLBACK = 1;
    final int REQUEST_CODE_FOR_INTENT = 1;
    SongTable mSongTable;
    SongJDO mSongJDObject;
    SharedPreferences mSharedPrefrence;
    SongRecyclerAdapter mSongRecyclerAdapter;
    LinearLayoutManager mRecyclerAdapterLinearLayoutManager;
    boolean mIsSearchingSongs = false;


    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.mediaPlayerRecylerView);
        mRecyclerAdapterLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerAdapterLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mRecyclerAdapterLinearLayoutManager);

        mSongTable = new SongTable(this);
        mSongTable.open();

        /**
         * register the broadcast reciver
         */
        LocalBroadcastManager.getInstance(this).registerReceiver(mSongReceiver,new IntentFilter(UtilityClass.SEND_BROADCAST_TO_RECYLER_LIST));

        /**
         * check the song is playing before starting the activity
         */
        Boolean lIsSongPlayingAlready = getIntent().getBooleanExtra(UtilityClass.PENDING_INTENT, false);
        if (lIsSongPlayingAlready || isMediaPlayerServiceRunning(MediaPlayerBoundService.class)) {

            Log.d("MAIN ACTIVITY", "===========start the service: ");
            SharedPreferences lSharedPref = getSharedPreferences(UtilityClass.MY_SHARED_PREFRENCE, Context.MODE_PRIVATE);
            SharedPreferences.Editor lSharedPrefEditor = lSharedPref.edit();
            lSharedPrefEditor.putBoolean(UtilityClass.SONG_IS_ALREADY_PLAYING, true);
            lSharedPrefEditor.commit();

            Intent lPlaySongActivity = new Intent(MainActivity.this, PlaySongActivity.class);
            lPlaySongActivity.putExtra(UtilityClass.SONG_IS_PLAYING_IN_BACKGROUND, true);
            startActivityForResult(lPlaySongActivity, REQUEST_CODE_FOR_INTENT);

        }


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
        if (requestCode == REQUEST_CODE_FOR_INTENT) {
            if (resultCode == RESULT_OK) {
                mSongJDOArrayList.clear();
                mSearchSongArryList.clear();
                getSongsFromSQLite();
                mSearchSongArryList.addAll(mSongJDOArrayList);

                int lsongPosition=getSharedPreferences(UtilityClass.MY_SHARED_PREFRENCE,Context.MODE_PRIVATE).getInt(UtilityClass.SONG_POSITION_SET_COLOR,-1);


                mSongRecyclerAdapter.getSongDetails(lsongPosition).setmIsThisSongPlaying(true);
                mSongRecyclerAdapter.notifyItemChanged(lsongPosition);
//                mSearchSongArryList.get(lsongPosition).setmIsThisSongPlaying(true);

                for(SongJDO lSongJddo:mSearchSongArryList){
                    if(lSongJddo.getIsmIsThisSongPlaying()){
                        Log.d(TAG, "onActivityResult: song_is_playing loop "+lSongJddo.getmSongId()+" "+lSongJddo.getmSongTitel());
                    }
                }
                mSongRecyclerAdapter.notifyDataSetChanged();
                Log.d(TAG, "onActivityResult: song_is_playing "+lsongPosition);
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
                mSongColumnId = lGetSongCursor.getString(lGetSongCursor.getColumnIndex(mSongTable._ID));
                mSongId = lGetSongCursor.getString(lGetSongCursor.getColumnIndex(mSongTable.SONG_ID));
                mSongTitle = lGetSongCursor.getString(lGetSongCursor.getColumnIndex(mSongTable.SONG_TITLE));
                mSongArtist = lGetSongCursor.getString(lGetSongCursor.getColumnIndex(mSongTable.SONG_ARTIST));
                mSongDuration = lGetSongCursor.getString(lGetSongCursor.getColumnIndex(mSongTable.SONG_DURATION));
                mSongAlbumm = lGetSongCursor.getString(lGetSongCursor.getColumnIndex(mSongTable.SONG_ALBUM));
                mSongImgeUri = lGetSongCursor.getString(lGetSongCursor.getColumnIndex(mSongTable.SONG_IMAGE));
                mSongFavourite = lGetSongCursor.getInt(lGetSongCursor.getColumnIndex(mSongTable.SONG_FAVOURITE));
                Boolean lIsSongIsPlaying=false;
                mSongJDObject = new SongJDO(Integer.parseInt(mSongColumnId), mSongId, mSongTitle, mSongArtist, mSongDuration, mSongAlbumm, mSongImgeUri, mSongFavourite,lIsSongIsPlaying);
                mSongJDOArrayList.add(mSongJDObject);
                Log.d(TAG, "getSongsFromSQLite:  activity"+mSongTitle+" "+mSongFavourite);
            } while (lGetSongCursor.moveToNext());
        }


        /**
         *          set the adapter for Recycler View
         */

        if (mSongRecyclerAdapter == null) {
            mSearchSongArryList.addAll(mSongJDOArrayList);
            mSongRecyclerAdapter = new SongRecyclerAdapter(this, mSearchSongArryList);
            mRecyclerView.setAdapter(mSongRecyclerAdapter);
        } else {
            Log.e(TAG, "searching songs: ");
            mSongRecyclerAdapter.notifyDataSetChanged();
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

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_TO_GET_PERMISSION);
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

    /**
     * @param pServiceClass send the servi name to know wheather is it running or not
     * @return return true if it's running
     */
    boolean isMediaPlayerServiceRunning(Class<?> pServiceClass) {
        ActivityManager lActivityManger = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo lRunningService : lActivityManger.getRunningServices(Integer.MAX_VALUE)) {
            Log.d(TAG, "isMediaPlayerServiceRunning: "+lRunningService.service.getClassName());
            if (pServiceClass.getName().equals(lRunningService.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param menu
     * @return
     * search view listener
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        SearchView lSearchButton = (SearchView) menu.findItem(R.id.searcButton).getActionView();
        lSearchButton.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String pTextToSearch) {
                Log.d("search", "onQueryTextSubmit: " + pTextToSearch);
                return false;
            }


            @Override
            public boolean onQueryTextChange(String pTextToSearch) {
                Log.d("search", "onQueryTextChange: " + pTextToSearch);
                mSearchSongArryList.clear();
                if (pTextToSearch.length() > 0) {
                    for (SongJDO lJDO : mSongJDOArrayList) {
                        if (lJDO.getmSongTitel().toLowerCase().contains(pTextToSearch.toLowerCase()) || lJDO.getmSongAlbum().toLowerCase().contains(pTextToSearch.toLowerCase()))
                            mSearchSongArryList.add(lJDO);
                    }
                    Log.d("search", "onQueryTextChange: " + pTextToSearch);
                    mIsSearchingSongs = true;
                } else {
                    mSearchSongArryList.addAll(mSongJDOArrayList);
                    mIsSearchingSongs = false;
                }
                mSongRecyclerAdapter.notifyDataSetChanged();
                return false;
            }
        });
        return true;
    }

    /**
     *
     * @param pView
     * on_Item_click listener for Recycler view
     */

    public void onRowClick(View pView) {
        int lPosition = mRecyclerView.getChildLayoutPosition(pView);
        mSongJDObject = mSongRecyclerAdapter.getSongDetails(lPosition);


        Intent lPlaySongIntent = new Intent(MainActivity.this, PlaySongActivity.class);
        lPlaySongIntent.putExtra(UtilityClass.SONG_JDO, mSongJDObject);
        startActivityForResult(lPlaySongIntent, REQUEST_CODE_FOR_INTENT);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);

        mSearchSongArryList.get(lPosition).setmIsThisSongPlaying(true);
        mSongRecyclerAdapter.notifyItemChanged(lPosition);

        mSharedPrefrence = getSharedPreferences(UtilityClass.MY_SHARED_PREFRENCE, MODE_PRIVATE);
        SharedPreferences.Editor lSharedPrefreceEditor = mSharedPrefrence.edit();
        lSharedPrefreceEditor.putInt(UtilityClass.SONG_ID, Integer.parseInt(mSongJDObject.getmSongId()));
        lSharedPrefreceEditor.commit();
        Log.d(TAG, "onRowClick: ");

    }

    /**
     *
     * @param pView
     * on_click listener for favourite button
     */
    public void onFavriteItemClick(View pView) {

        int lFavourite;
        String lSongid;

        int lPosition = mRecyclerView.getChildLayoutPosition((View) pView.getParent().getParent().getParent().getParent());
        mSongJDObject = mSongRecyclerAdapter.getSongDetails(lPosition);
        lFavourite = mSongJDObject.getmFavourite();
        lSongid = mSongJDObject.getmSongId();

        if (lFavourite == 1) {
            lFavourite = 0;
        } else {
            lFavourite = 1;
        }

        mSearchSongArryList.get(lPosition).setmFavourite(lFavourite);
        mSongTable.updateFavroutieSong(lSongid, lFavourite);
        mSongRecyclerAdapter.notifyItemChanged(lPosition);

        Log.d(TAG, "onFavriteItemClick: preference ");
        SharedPreferences.Editor lSharedPrefrence=getSharedPreferences(UtilityClass.MY_SHARED_PREFRENCE,Context.MODE_PRIVATE).edit();
        lSharedPrefrence.putBoolean(UtilityClass.IS_DATABASE_UPDATED,true);
        lSharedPrefrence.commit();

    }

    /**
     * changing the color for list item for the song which is playing
     */
    void chageColorForSong(int pSongPositionToChangeColor){


        int lPosition=getSharedPreferences(UtilityClass.MY_SHARED_PREFRENCE,Context.MODE_PRIVATE).getInt(UtilityClass.SONG_POSITION_SET_COLOR,mSongJDOArrayList.size());
        mSearchSongArryList.clear();
        mSearchSongArryList.addAll(mSongJDOArrayList);

        SongJDO lSong = mSearchSongArryList.get(lPosition);
        lSong.setmIsThisSongPlaying(true);
        mSearchSongArryList.set(lPosition, lSong);

        mSongRecyclerAdapter.notifyDataSetChanged();
//        mSongRecyclerAdapter.notifyItemChanged(lPosition);

        Log.d(TAG, "changeColorForSong:  song_is_playing"+lPosition+"  "+pSongPositionToChangeColor);

    }

    /**
     *  broadcast receive to receive the currt position to chang highlight the song which is playing
     */
    BroadcastReceiver mSongReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int lSongPositionToChangeColor=intent.getIntExtra(UtilityClass.SONG_COLUMN_ID,mSongJDOArrayList.size());
            chageColorForSong(lSongPositionToChangeColor);
        }
    };


    /**
     * unregister the broadcast reciver
     */
    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mSongReceiver);
        mSongTable.close();
        super.onDestroy();
    }
}
