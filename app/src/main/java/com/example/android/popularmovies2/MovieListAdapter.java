package com.example.android.popularmovies2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.android.popularmovies2.data.MovieDbContract;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 */
public class MovieListAdapter extends ArrayAdapter<MovieListAdapter.MovieItem> {


    public MovieListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(getContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }
        MovieItem item = getItem( position);
        String backdropPath = item.get(MovieItem.MDB_KEY_BACKDROP_PATH);
        String backDropFullPath = TheMovieDbOrgUrlFactory.makeImage(backdropPath);
        Picasso.with(getContext()).load(backDropFullPath).into(imageView);

        return imageView;
    }

    public void assignFromJson(String jsonStr) {
        clear();
        addAll(MovieItem.getItemsFromJson(jsonStr));
    }

    public void assignFromDB() {
        clear();
        Cursor cursor = getContext().getContentResolver().
                query(MovieDbContract.MovieEntry.CONTENT_URI, null, null, null, null);
        addAll(MovieItem.getItemsFromCursor(cursor));
    }

    static public class MovieItem {

        // Favorite status is not kept in MovieItem.
        // In detail view, favorite status is kept by the 'checkbox' associated by favorite.
        // Otherwise, if item exists in the database, it is a favorite.

        final static public String MDB_KEY_BACKDROP_PATH = "backdrop_path";
        final static public String MDB_KEY_ID = "id";
        final static public String MDB_KEY_ORIGINAL_TITLE = "original_title";
        final static public String MDB_KEY_POSTER_PATH = "poster_path";
        final static public String MDB_KEY_RELEASE_DATE = "release_date";
        final static public String MDB_KEY_SYNOPSIS = "overview";
        final static public String MDB_KEY_VOTE_AVERAGE = "vote_average";

        final static public String KEY_JSON_TRAILERS = "json_trailers";
        final static public String KEY_JSON_REIEWS = "json_reviews";

        final static private String MDB_RESULT_LIST = "results";

        final static public String BUNDLE_NAME = "MovieItemAsHm";

        // Keep PROJECTION and MDB_KEY_LIST in same order.

        final static public String[] MDB_KEY_LIST = {
                MDB_KEY_BACKDROP_PATH,
                MDB_KEY_ID,
                MDB_KEY_ORIGINAL_TITLE,
                MDB_KEY_POSTER_PATH,
                MDB_KEY_RELEASE_DATE,
                MDB_KEY_SYNOPSIS,
                MDB_KEY_VOTE_AVERAGE,
        };

        //  Same order as MovieDbHelper defines database columns.
        final static String[] MDB_KEY_BY_DB_COLUMN_ORDER = {
                null,                    MovieDbContract.MovieEntry._ID,
                MDB_KEY_ID,              MovieDbContract.MovieEntry.COLUMN_ID,
                MDB_KEY_BACKDROP_PATH,   MovieDbContract.MovieEntry.COLUMN_BACKDROP_PATH,
                MDB_KEY_ORIGINAL_TITLE,  MovieDbContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
                MDB_KEY_POSTER_PATH,     MovieDbContract.MovieEntry.COLUMN_POSTER_PATH,
                MDB_KEY_RELEASE_DATE,    MovieDbContract.MovieEntry.COLUMN_RELEASE_DATE,
                MDB_KEY_SYNOPSIS,        MovieDbContract.MovieEntry.COLUMN_SYNOPSIS,
                MDB_KEY_VOTE_AVERAGE,    MovieDbContract.MovieEntry.COLUMN_VOTE_AVERAGE,
                KEY_JSON_REIEWS,         MovieDbContract.MovieEntry.COLUMN_JSON_REVIEWS,
                KEY_JSON_TRAILERS,       MovieDbContract.MovieEntry.COLUMN_JSON_TRAILERS,
        };

        private HashMap<String, String> mInfo = new HashMap<>(MDB_KEY_LIST.length);

        public MovieItem(HashMap<String, String> info) {
            mInfo = info;
        }

        public String get(String key) {
            return mInfo.get(key);
        }

        public void put(String key, String value) {
            mInfo.put( key, value);
        }

        public HashMap<String, String> getInfoMap() {
            return mInfo;
        }

        public ContentValues toContentValues() {
            ContentValues contentValues = new ContentValues(MDB_KEY_BY_DB_COLUMN_ORDER.length);
            for( int ii = 2; ii+1 < MDB_KEY_BY_DB_COLUMN_ORDER.length; ii+=2) {
                contentValues.put( MDB_KEY_BY_DB_COLUMN_ORDER[ii+1],
                        mInfo.get(MDB_KEY_BY_DB_COLUMN_ORDER[ii]));
            }
            return contentValues;
        }

        static MovieItem[] getItemsFromCursor(Cursor cursor) {
            ArrayList<MovieItem> items = new ArrayList<>();
            while( cursor.moveToNext()) {
                HashMap<String,String> hashMap = new HashMap<>(MDB_KEY_BY_DB_COLUMN_ORDER.length);
                // Skip the first '_ID' column,
                for( int ii = 2; ii+1 < MDB_KEY_BY_DB_COLUMN_ORDER.length; ii+=2) {
                    String value = cursor.getString(ii/2);
                    if( value != null) {
                        hashMap.put( MDB_KEY_BY_DB_COLUMN_ORDER[ii], value);
                    }
                }
                items.add(new MovieItem(hashMap));
            }
            return items.toArray(new MovieItem[items.size()]);

        }

        static MovieItem[] getItemsFromJson(String jsonStr) {
            ArrayList<HashMap<String, String>> infoList =
                    Utility.getDataFromJson(jsonStr, MDB_RESULT_LIST, MDB_KEY_LIST);
            MovieItem[] items = new MovieItem[infoList.size()];
            for (int ii = 0; ii < items.length; ii++) {
                items[ii] = new MovieItem(infoList.get(ii));
            }
            return items;
        }

    }
}
