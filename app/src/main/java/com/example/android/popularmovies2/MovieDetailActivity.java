package com.example.android.popularmovies2;

import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.view.MenuItem;


public class MovieDetailActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.movie_detail_frag_title);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            Bundle arguments = intent.getBundleExtra(MovieListAdapter.MovieItem.BUNDLE_NAME);

            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case R.id.action_settings:
                return true;

            case android.R.id.home:
                // Without this, UP button would re-start main activity with savedInstanceState empty.
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
