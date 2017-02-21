package com.mohanty.akshata.trackread.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Akshata on 21/2/2017.
 */

public final class BooksContract {

    public static final String CONTENT_AUTHORITY = "com.mohanty.akshata.trackread";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String TABLE_NAME_POPULAR = "books";

    public BooksContract() {}

    /* Inner classes that defines the table contents */
    public static abstract class MovieEntry implements BaseColumns {

        public static final String COLUMN_NAME_MOVIE_ID = "movie_id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_SYNOPSIS = "synopsis";
        public static final String COLUMN_NAME_POSTER = "poster";
        public static final String COLUMN_NAME_RATING = "rating";
        public static final String COLUMN_NAME_RELEASE = "release";

        public static final String COLUMN_NAME_VIDEOS = "videos";
        public static final String COLUMN_NAME_REVIEWS = "reviews";

    }

    // Helper functions for Uri building
    public static final Uri buildUriForPopular(){
        return BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME_POPULAR).build();
    }

    public static final Uri buildUriForTopRated(){
        return BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME_TOP_RATED).build();
    }

    public static final Uri buildUriForSaved(){
        return BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME_SAVED).build();
    }

    public static final Uri buildUriForPopularMovieDetails(String movieTitle){
        return BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME_POPULAR).appendPath(movieTitle).build();
    }

    public static final Uri buildUriForTopRatedMovieDetails(String movieTitle){
        return BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME_TOP_RATED).appendPath(movieTitle).build();
    }

    public static final Uri buildUriForSavedMovieDetails(String movieTitle){
        return BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME_SAVED).appendPath(movieTitle).build();
    }

}



