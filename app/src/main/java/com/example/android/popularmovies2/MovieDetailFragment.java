package com.example.android.popularmovies2;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.example.android.popularmovies2.data.MovieDbContract;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

/**
 *  Fragment showing movie details.
 */
public class MovieDetailFragment extends Fragment {
    private final String LOG_TAG = MovieDetailFragment.class.getSimpleName();

    private ReviewListAdapter mReviewListAdapter = null;
    private TrailerListAdapter mTrailerListAdapter = null;

    private String mMovieId;
    private MovieListAdapter.MovieItem mMovieItem = null;

    private ListView mTrailersListView = null;
    private CheckBox mStarView = null;

    ShareActionProvider mShareActionProvider = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        TextView originalTitleView = (TextView) rootView.findViewById(R.id.original_title_view);
        TextView releaseDateView = (TextView) rootView.findViewById(R.id.release_date_view);
        ImageView posterView = (ImageView) rootView.findViewById(R.id.poster_image_view);
        TextView voteAverageView = (TextView) rootView.findViewById(R.id.vote_average_view);
        TextView synopsisView = (TextView) rootView.findViewById(R.id.synopsis_view);

        try {
            Bundle b = (savedInstanceState != null) ? savedInstanceState : getArguments();
            HashMap<String, String> hashMap = (HashMap<String, String>)
                            getArguments().getSerializable(MovieListAdapter.MovieItem.BUNDLE_NAME);
            if (hashMap != null) {
                mMovieItem = new MovieListAdapter.MovieItem(hashMap);
            }

        } catch (final ClassCastException e) {
            Log.e(LOG_TAG, e.toString());
        }

        if (mMovieItem == null) {
            return rootView;
        }

        mMovieId = mMovieItem.get(MovieListAdapter.MovieItem.MDB_KEY_ID);
        String originalTitle = mMovieItem.get(MovieListAdapter.MovieItem.MDB_KEY_ORIGINAL_TITLE);
        String releaseDate = mMovieItem.get(MovieListAdapter.MovieItem.MDB_KEY_RELEASE_DATE);
        String posterPath = mMovieItem.get(MovieListAdapter.MovieItem.MDB_KEY_POSTER_PATH);
        String voteAverage = mMovieItem.get(MovieListAdapter.MovieItem.MDB_KEY_VOTE_AVERAGE);
        String synopsis = mMovieItem.get(MovieListAdapter.MovieItem.MDB_KEY_SYNOPSIS);

        originalTitleView.setText(originalTitle);

        int dashPos = releaseDate.indexOf('-');
        String releaseYear = (dashPos >= 0) ? releaseDate.substring(0, dashPos) : releaseDate;
        releaseDateView.setText(releaseYear);

        String posterFullPath = TheMovieDbOrgUrlFactory.makeImage(posterPath);
        Picasso.with(getActivity()).load(posterFullPath).into(posterView);

        voteAverage += "/10";
        voteAverageView.setText(voteAverage);

        synopsisView.setText(synopsis);

        // Reviews
        ListView reviewListView = (ListView) rootView.findViewById(R.id.review_list_view);
        mReviewListAdapter = new ReviewListAdapter(getActivity(), R.id.review_list_view);
        mReviewListAdapter.setListView(reviewListView);
        reviewListView.setAdapter(mReviewListAdapter);


        String jsonReviewsStr = mMovieItem.get(MovieListAdapter.MovieItem.KEY_JSON_REIEWS);
        if (jsonReviewsStr != null) {
            mReviewListAdapter.addFromJson(jsonReviewsStr);
        }

        // Trailers
        mTrailersListView = (ListView) rootView.findViewById(R.id.trailer_list_view);
        mTrailerListAdapter = new TrailerListAdapter(getActivity(), R.id.trailer_list_view);
        mTrailersListView.setAdapter(mTrailerListAdapter);

        String jsonTrailersStr = mMovieItem.get(MovieListAdapter.MovieItem.KEY_JSON_TRAILERS);
        if (jsonTrailersStr != null) {
            mTrailerListAdapter.addFromJson(jsonTrailersStr);
            Utility.setListViewHeightBasedOnItems(mTrailersListView);
        }

        boolean isFave = isFavorite();
        mStarView = (CheckBox) rootView.findViewById(R.id.star);
        mStarView.setChecked(isFave);
        mStarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFavorite(mStarView.isChecked());
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this_ adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_movie_detail, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
    }

    private void setTrailerShareIntent() {
        if((mShareActionProvider != null) && (mTrailerListAdapter != null) &&
                (mTrailerListAdapter.getCount() > 0)) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            String url = mTrailerListAdapter.getItem(0).getTrailerUrl();
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, url);
            mShareActionProvider.setShareIntent( shareIntent);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save Movie info, so we don't need to download reviews+trailer information, when rotating device.
        if( mMovieItem != null) {
            outState.putSerializable(MovieListAdapter.MovieItem.BUNDLE_NAME, mMovieItem.getInfoMap());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if( (mReviewListAdapter != null) && ( mReviewListAdapter.isEmpty())) {
            refreshMovieReviewList();
        }
        if( mTrailerListAdapter != null && mTrailerListAdapter.isEmpty()) {
            refreshMovieTrailerList();
        }
    }

    private void refreshMovieReviewList() {
        DownloadTask downloadTask = new DownloadTask() {
            @Override
            public void onPostExecute(String jsonStr) {
                if(mMovieItem != null) {
                    mMovieItem.put(MovieListAdapter.MovieItem.KEY_JSON_REIEWS, jsonStr);
                }
                mReviewListAdapter.addFromJson(jsonStr);
            }
        };
        downloadTask.execute( TheMovieDbOrgUrlFactory.makeMovieDetailsReviews(mMovieId));
    }

    private void refreshMovieTrailerList() {
        DownloadTask downloadTask = new DownloadTask() {
            @Override
            public void onPostExecute(String jsonStr) {
                if( mMovieItem != null) {
                    mMovieItem.put(MovieListAdapter.MovieItem.KEY_JSON_TRAILERS, jsonStr);
                }
                mTrailerListAdapter.addFromJson(jsonStr);
                setTrailerShareIntent();
                Utility.setListViewHeightBasedOnItems(mTrailersListView);
            }
        };
        downloadTask.execute(TheMovieDbOrgUrlFactory.makeMovieDetailsTrailers(mMovieId));
    }

    private boolean isFavorite() {
        if( mStarView != null) {
            return mStarView.isChecked();
        }
        Cursor cursor;
        try {
            cursor =
                getActivity().getContentResolver().query(MovieDbContract.MovieEntry.CONTENT_URI,
                null, MovieDbContract.MovieEntry.COLUMN_ID + " =  ?", new String[] {mMovieId}, null);
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Is fave db query:" + ex.getMessage());
            return false;
        }
        return cursor != null  && cursor.moveToFirst();

    }

    private void setFavorite( boolean b) {
        if( b) {
            // Insert.
            getActivity().getContentResolver().
                    insert(MovieDbContract.MovieEntry.CONTENT_URI, mMovieItem.toContentValues());
        } else {
            // Delete from database
            getActivity().getContentResolver().
                    delete(MovieDbContract.MovieEntry.CONTENT_URI,
                            MovieDbContract.MovieEntry.COLUMN_ID + " =  ?", new String[] {mMovieId});

        }
    }

}



