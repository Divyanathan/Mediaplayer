package com.example.user.mediaplayer.dataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by user on 06/05/17.
 */

public class SongTable {


    public static final String SONG_TABLE = "song_table";
    public static final String _ID = "_id";
    public static final String SONG_ID = "song_id";
    public static final String SONG_ALBUM = "album";
    public static final String SONG_TITLE = "title";
    public static final String SONG_ARTIST = "artist";
    public static final String SONG_IMAGE = "image";
    public static final String SONG_DURATION = "duration";
    public static final String SONG_FAVOURITE = "favourite";




    DataBaseHelper mDataBaseHelper;
    SQLiteDatabase mDataBase;
    Context mContext;

    /**
     * @param mContext context from the activity
     */
    public SongTable(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Opening an tabele
     */
    public void open() {

        mDataBaseHelper = new DataBaseHelper(mContext, null, null, 1);
        mDataBase = mDataBaseHelper.getWritableDatabase();

    }


    /**
     * create table
     */
    public static void createTable(SQLiteDatabase pDataBase) {

        String lCreatTableQuery = "create table " + SONG_TABLE + "(" +
                _ID + " integer primary key autoincrement," +
                SONG_ID + " text ," +
                SONG_ALBUM + " text ," +
                SONG_TITLE + " text ," +
                SONG_ARTIST + " text ," +
                SONG_DURATION + " text ," +
                SONG_IMAGE + " text," +
                SONG_FAVOURITE + " integer DEFAULT 0" +
                ");";

        pDataBase.execSQL(lCreatTableQuery);

    }

    /**
     * @param pSongId
     * @param pSongAlbum
     * @param pTitle
     * @param pArtist
     * @param pDuration
     * @param pSongImage add the songs to table
     */
    public void addSongs(String pSongId, String pSongAlbum, String pTitle, String pArtist, String pDuration, String pSongImage) {

        ContentValues lContactValue = new ContentValues();
        lContactValue.put(SONG_ID, pSongId);
        lContactValue.put(SONG_ALBUM, pSongAlbum);
        lContactValue.put(SONG_TITLE, pTitle);
        lContactValue.put(SONG_ARTIST, pArtist);
        lContactValue.put(SONG_DURATION, pDuration);
        lContactValue.put(SONG_IMAGE, pSongImage);

        mDataBase.insert(SONG_TABLE, null, lContactValue);
    }

    /**
     * @return return the cursor to the ui with all the data
     */

    public Cursor getSongs() {
        return mDataBase.query(SONG_TABLE, null, null, null, null, null, SONG_TITLE + " ASC");
    }


    /**
     * close the table
     */


    public void close() {
        mDataBaseHelper.close();
    }

    public boolean isSongExist(String pSongId) {
        Cursor lCursor = mDataBase.rawQuery("SELECT * FROM " + SONG_TABLE + " WHERE " + SONG_ID + "='" + pSongId + "'", null);

        if (lCursor.moveToFirst()) {
            return true;

        } else {
            return false;

        }
    }

    public Cursor getSongDetails(String pSongId) {
        return mDataBase.rawQuery("SELECT * FROM " + SONG_TABLE + " WHERE " + SONG_ID + "='" + pSongId + "'", null);

    }

    public Cursor getSongId(String pSongId) {
        return mDataBase.rawQuery("SELECT " + SONG_ID + " FROM " + SONG_TABLE + " WHERE " + _ID + "=" + pSongId + "", null);

    }

    public int getTheLastSongId() {
        Cursor lLastSongCursor = mDataBase.rawQuery("SELECT MAX(" + _ID + ") FROM " + SONG_TABLE, null);
        if (lLastSongCursor.moveToFirst()) {
            return lLastSongCursor.getInt(0);
        } else {
            return 0;
        }
    }

    public int getFavrotieSong(String pSongId) {
        Cursor lGetFavrite= mDataBase.rawQuery("SELECT " + SONG_FAVOURITE + " FROM " + SONG_TABLE + " WHERE " + SONG_ID + "=" + pSongId + "", null);

        if (lGetFavrite.moveToFirst()){
            return lGetFavrite.getInt(0);
        }
        return 0;

    }
    public void updateFavroutieSong(String pSongId,int pFavour) {
        ContentValues lContentValues = new ContentValues();
        lContentValues.put(SONG_FAVOURITE, pFavour);
        mDataBase.update(SONG_TABLE,lContentValues,SONG_ID+ " = ?", new String[]{pSongId});

    }
}
