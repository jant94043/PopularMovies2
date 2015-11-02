package com.example.android.popularmovies2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.popularmovies2.data.MovieDbContract.MovieEntry;
/**
 * 
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "movies2.db";
    
    public MovieDbHelper( Context context) {
        super( context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // If order changes, need to update MovieListAdapter.
        final String SQL_CREATE_MOVIES2_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieDbContract.MovieEntry._ID + " INTEGER PRIMARY KEY," +
                MovieEntry.COLUMN_ID + " TEXT UNIQUE NOT NULL, " +
                MovieEntry.COLUMN_BACKDROP_PATH + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_SYNOPSIS + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_VOTE_AVERAGE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_JSON_REVIEWS + " TEXT, " +
                MovieEntry.COLUMN_JSON_TRAILERS + " TEXT" +
                " );";
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES2_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
