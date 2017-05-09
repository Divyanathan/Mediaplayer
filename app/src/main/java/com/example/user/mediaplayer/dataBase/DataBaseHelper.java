package com.example.user.mediaplayer.dataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by user on 06/05/17.
 */

public class DataBaseHelper extends SQLiteOpenHelper {

    public final static String DATA_BASE_NAME="Song.db";

    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATA_BASE_NAME, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase pSqLiteDatabase) {
        SongTable.createTable(pSqLiteDatabase);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
