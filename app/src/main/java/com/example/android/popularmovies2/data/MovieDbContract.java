package com.example.android.popularmovies2.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 
 */
public class MovieDbContract {
    public static final String CONTENT_AUTHORITY = "com.example.android.popularmovies2.app";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIES = "movies";

    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

    public static final class MovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static String getDbIdFromUri(Uri uri) {
            return uri.getQueryParameter(_ID);
        }

        // Note _id is database _id, not the movie id.
        public static Uri buildUriDbId(long _id) {
            return ContentUris.withAppendedId(CONTENT_URI, _id);
        }

        public static final String TABLE_NAME  = "movies";
        // Columns
        final static public String COLUMN_BACKDROP_PATH = "backdrop_path";
        final static public String COLUMN_ID = "id";
        final static public String COLUMN_ORIGINAL_TITLE = "original_title";
        final static public String COLUMN_POSTER_PATH = "poster_path";
        final static public String COLUMN_RELEASE_DATE = "release_date";
        final static public String COLUMN_SYNOPSIS = "overview";
        final static public String COLUMN_VOTE_AVERAGE = "vote_average";

        final static public String COLUMN_JSON_TRAILERS = "json_trailers";
        final static public String COLUMN_JSON_REVIEWS = "json_reviews";

        
    }

}
