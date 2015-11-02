package com.example.android.popularmovies2;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.popularmovies2.data.MovieDbContract;

/**
 * Fragment showing grid of popular movies.
 */
public class MainFragment extends Fragment {

    private final String LOG_TAG = MovieDetailFragment.class.getSimpleName();

    private MovieListAdapter mMovieListAdapter;
    private String[] mSortOrderList;
    private int mSortOrderIndex = 0;
    private String mFavoritesChoiceStr;
    private String mPopularChoiceStr;
    private String mJsonMovieData = "";

    private ProgressBar mProgress;
    private TextView    mProgressTextView;

    interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         * null value means select nothing, i.e remove detailed fragment
         */
        void onItemSelected(Bundle movieItemBundle);
    }

    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSortOrderList = getResources().getStringArray(R.array.sort_order_array);
        mFavoritesChoiceStr = getResources().getString(R.string.favorites_choice);
        mPopularChoiceStr = getResources().getString(R.string.popular_choice);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Setup grid
        GridView gridview = (GridView) rootView.findViewById(R.id.gridview);
        mMovieListAdapter = new MovieListAdapter( getActivity(), R.id.gridview);
        gridview.setAdapter(mMovieListAdapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieListAdapter.MovieItem item = mMovieListAdapter.getItem(position);
                Bundle bundle = new Bundle();
                bundle.putSerializable(MovieListAdapter.MovieItem.BUNDLE_NAME, item.getInfoMap());
                ((Callback)getActivity()).onItemSelected(bundle);
            }
        });

        if (savedInstanceState != null) {
            mSortOrderIndex = savedInstanceState.getInt("sortOrderIndex", 0) % mSortOrderList.length;
            if (!isFavoriteChosen()) {
                String jsonMovieData = savedInstanceState.getString("jsonMovieData", "");
                updateFragmentWithJsonData(jsonMovieData);
            }
        }

        // Setup sort order spinner
        Spinner spinner = (Spinner) rootView.findViewById(R.id.sort_spinner);

        // Progress
        mProgress = (ProgressBar)rootView.findViewById(R.id.progressBar);
        mProgressTextView = (TextView)rootView.findViewById(R.id.progressTextView);

        // Movie sort type Adapter
        ArrayAdapter<String> sortOrderAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, mSortOrderList);
        sortOrderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(sortOrderAdapter);
        spinner.setSelection(mSortOrderIndex);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (mSortOrderIndex != pos) {
                    mSortOrderIndex = pos;
                    refreshMovieList();
                    // null means, remove detailed fragment.
                    ((Callback)getActivity()).onItemSelected(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("sortOrderIndex", mSortOrderIndex);
        if( mJsonMovieData != null) {
            outState.putString("jsonMovieData", mJsonMovieData);
        }
    }

    private void setProgressOn( boolean progressOn) {
        mProgress.setVisibility(progressOn ? View.VISIBLE : View.GONE);
        mProgressTextView.setVisibility(progressOn ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Favorites are read from local database, later on "onResume()"
        if( mMovieListAdapter.isEmpty() && ! isFavoriteChosen()) {
            refreshMovieList();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getContentResolver().unregisterContentObserver(mFavoriteDbObserver);
    }

    @Override
    public void onResume() {
        super.onResume();
        // The content observer helps for two pane display, to remove/add favorite when star
        //  button is checked/unchecked in the detailed pane.
        getActivity().getContentResolver().registerContentObserver(MovieDbContract.MovieEntry.CONTENT_URI,
                true, mFavoriteDbObserver);
        // For handling one pane case, reload here "onResume()" from the local database,
        //  in case of change among favorites, when resuming after detailed activity hit back button.
        if (isFavoriteChosen()){
            mMovieListAdapter.assignFromDB();
        }
    }

    private void setJsonMovieData(String jsonMovieData) {
        this.mJsonMovieData = jsonMovieData;
    }

    boolean isFavoriteChosen() {
        return getSortOrder().equalsIgnoreCase(mFavoritesChoiceStr);
    }

    boolean isPopularChosen() {
        return getSortOrder().equalsIgnoreCase(mPopularChoiceStr);
    }

    private void refreshMovieList() {

        setJsonMovieData(null);
        if (isFavoriteChosen()) {
            mMovieListAdapter.assignFromDB();
            return;
        }

        DownloadTask downloadTask = new DownloadTask() {
            @Override
            public void onPostExecute(String jsonStr) {
                setProgressOn(false);
                updateFragmentWithJsonData(jsonStr);
            }
        };
        setProgressOn(true);
        downloadTask.execute( TheMovieDbOrgUrlFactory.makeSearch(isPopularChosen()));
    }

    private String getSortOrder() {
        return mSortOrderList[mSortOrderIndex];
    }

    private void updateFragmentWithJsonData( String jsonMovieDB) {
        mMovieListAdapter.assignFromJson(jsonMovieDB);
        setJsonMovieData(jsonMovieDB);
    }

    FavoriteDbObserver mFavoriteDbObserver = new FavoriteDbObserver( new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            //  Redisplay Favorite list if has changed.
            if (isFavoriteChosen()){
                mMovieListAdapter.assignFromDB();
            }
        }

    };

    private abstract class FavoriteDbObserver extends ContentObserver {
        public FavoriteDbObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }
        @Override
        public abstract void onChange(boolean selfChange, Uri uri);
    }

}
