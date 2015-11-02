package com.example.android.popularmovies2;

import android.net.Uri;

/**
 * Construct the URL for the Movie DB query
 * Possible parameters are available, at
 * https://www.themoviedb.org/documentation/api
 */
public class TheMovieDbOrgUrlFactory {

    private final static String SEARCH_AUTH = "api.themoviedb.org";
    private final static String IMAGE_AUTH = "image.tmdb.org";
    private final static String[] SEARCH_APPEND_PATHS = { "3", "discover","movie"};
    private final static String[] DETAIL_APPEND_PATHS = { "3","movie"};
    private final static String[] IMAGE_APPEND_PATHS = { "t", "p", "w342"};
    private final static String API_KEY_NAME  = "api_key";
    private final static String API_KEY_VALUE = TheMovieDbApiKey.getKey();
    private final static String SORT_BY_NAME  = "sort_by";
    private final static String SORT_BY_POPULAR_VALUE = "popularity.desc";
    private final static String SORT_BY_HIGHEST_RATING_VALUE = "vote_average.desc";
    private final static String TRAILERS = "trailers";
    private final static String REVIEWS = "reviews";



    static public String makeSearch( boolean isPopular) {

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
            .authority(SEARCH_AUTH);
        for( String aP : SEARCH_APPEND_PATHS) {
            builder.appendPath(aP);
        }
        String sortByValue = isPopular ? SORT_BY_POPULAR_VALUE: SORT_BY_HIGHEST_RATING_VALUE;
        builder.appendQueryParameter(SORT_BY_NAME, sortByValue)
            .appendQueryParameter(API_KEY_NAME, API_KEY_VALUE);
        return builder.build().toString();
    }

    // http://api.themoviedb.org/3/movie/{id}/trailers?api_key=API_KEY_VALUE
    static public String makeMovieDetailsTrailers( String id) {
        return makeMovieDetails( id, TRAILERS);
    }
    // http://api.themoviedb.org/3/movie/{id}/reviews?api_key=API_KEY_VALUE
    static public String makeMovieDetailsReviews( String id) {
        return makeMovieDetails(id, REVIEWS);
    }

    static private String makeMovieDetails( String id, String type) {

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http").authority(SEARCH_AUTH);
        for( String aP : DETAIL_APPEND_PATHS) {
            builder.appendPath(aP);
        }
        builder.appendPath(id).appendPath(type);
        builder.appendQueryParameter(API_KEY_NAME, API_KEY_VALUE);
        return builder.build().toString();
    }

    static public String makeImage( String imagePath) {

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http").authority(IMAGE_AUTH);
        for( String aP : IMAGE_APPEND_PATHS) {
            builder.appendPath(aP);
        }
        builder.appendEncodedPath( imagePath)
                .appendQueryParameter(API_KEY_NAME, API_KEY_VALUE);
        return builder.build().toString();
    }
}
