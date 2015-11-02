package com.example.android.popularmovies2.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 *
 */
public class MovieProvider extends ContentProvider{
    private MovieDbHelper mOpenHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    static final int MOVIES = 100;
    static final int MOVIE_BY_ID = 101;

    private static final SQLiteQueryBuilder sMovieByIdQueryBuilder;

    static{
        sMovieByIdQueryBuilder = new SQLiteQueryBuilder();
        sMovieByIdQueryBuilder.setTables( MovieDbContract.MovieEntry.TABLE_NAME);
    }

    private static final String sIdSettingSelection =
            MovieDbContract.MovieEntry.TABLE_NAME + "." + MovieDbContract.MovieEntry.COLUMN_ID + " = ? ";

    private static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieDbContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, MovieDbContract.PATH_MOVIES, MOVIES);
        matcher.addURI(authority, MovieDbContract.PATH_MOVIES + "/*", MOVIE_BY_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case MOVIES:
                return MovieDbContract.CONTENT_TYPE;
            case MOVIE_BY_ID:
                return MovieDbContract.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {
            case MOVIES:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieDbContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case MOVIE_BY_ID:
                String movieIdSetting = MovieDbContract.MovieEntry.getDbIdFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieDbContract.MovieEntry.TABLE_NAME,
                        projection,
                        sIdSettingSelection,
                        new String[] {movieIdSetting},
                        null,
                        null,
                        sortOrder
                );

                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;
        long _id = db.insert(MovieDbContract.MovieEntry.TABLE_NAME, null, values);
        if ( _id > 0 ) {
            returnUri = MovieDbContract.MovieEntry.buildUriDbId(_id);
        } else {
            throw new android.database.SQLException("Failed to insert row into " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if ( null == selection ) {
            // this makes delete all rows return the number of rows deleted
            selection = "1";
        }

        int rowsDeleted = db.delete(MovieDbContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated = db.update(MovieDbContract.MovieEntry.TABLE_NAME, values, selection,
                selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }

}
