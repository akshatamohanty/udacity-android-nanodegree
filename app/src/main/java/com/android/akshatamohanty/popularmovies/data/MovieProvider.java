package com.android.akshatamohanty.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Content Provider
 * Created by Akshata on 31/7/2016.
 */
public class MovieProvider extends ContentProvider {

        String LOG_TAG = this.getClass().getSimpleName();

        private MovieDBHelper mDBHelper;

        static final int POPULAR = 101;
        static final int POPULAR_ITEM = 1011;
        static final int TOP_RATED = 102;
        static final int TOP_RATED_ITEM = 1021;
        static final int SAVED = 103;
        static final int SAVED_ITEM = 1031;


        private static final UriMatcher uriMatcher =  new UriMatcher(UriMatcher.NO_MATCH);
        static{
            uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.TABLE_NAME_POPULAR, POPULAR);
            uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY,  MovieContract.TABLE_NAME_POPULAR + "/*", POPULAR_ITEM);

            uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.TABLE_NAME_TOP_RATED, TOP_RATED);
            uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.TABLE_NAME_TOP_RATED + "/*", TOP_RATED_ITEM);

            uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.TABLE_NAME_SAVED, SAVED);
            uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.TABLE_NAME_SAVED + "/*", SAVED_ITEM);
        }

        public boolean onCreate() {

            Log.v(LOG_TAG, "Content Provider created.");

            mDBHelper = new MovieDBHelper( getContext() );

            return true;
        }

        @Nullable
        @Override
        public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {

            Cursor cursor = null;

            String movieTitle;

            switch (uriMatcher.match(uri)) {

                case POPULAR:
                    cursor = mDBHelper.getAllMovies(MovieContract.TABLE_NAME_POPULAR);
                    break;
                case POPULAR_ITEM:
                    movieTitle = uri.getLastPathSegment();
                    cursor = mDBHelper.getMovieDetails(MovieContract.TABLE_NAME_POPULAR, movieTitle);
                    break;

                case TOP_RATED:
                    cursor = mDBHelper.getAllMovies(MovieContract.TABLE_NAME_TOP_RATED);
                    break;
                case TOP_RATED_ITEM:
                    movieTitle = uri.getLastPathSegment();
                    cursor = mDBHelper.getMovieDetails(MovieContract.TABLE_NAME_TOP_RATED, movieTitle);
                    break;

                case SAVED:
                    cursor = mDBHelper.getAllMovies(MovieContract.TABLE_NAME_SAVED);
                    break;
                case SAVED_ITEM:
                    movieTitle = uri.getLastPathSegment();
                    cursor = mDBHelper.getMovieDetails(MovieContract.TABLE_NAME_SAVED, movieTitle);
                    break;

                default:
                    Log.v("Invalid-URI", uri.toString());

            }

            return cursor;
        }

        public String getType(Uri uri){
            return null;
        }

        @Nullable
        @Override
        public Uri insert(Uri uri, ContentValues contentValues) {

            String tableName = uri.getLastPathSegment();

            mDBHelper.addMovie(tableName, contentValues);

            return null;
        }

        @Override
        public int delete(Uri uri, String s, String[] strings) {

            String movieTitle;

            switch (uriMatcher.match(uri)) {

                case POPULAR:
                    mDBHelper.deleteAllMovies(MovieContract.TABLE_NAME_POPULAR);
                    break;
                case POPULAR_ITEM:
                    movieTitle = uri.getLastPathSegment();
                    mDBHelper.deleteMovie(MovieContract.TABLE_NAME_POPULAR, movieTitle);
                    break;

                case TOP_RATED:
                    mDBHelper.deleteAllMovies(MovieContract.TABLE_NAME_TOP_RATED);
                    break;
                case TOP_RATED_ITEM:
                    movieTitle = uri.getLastPathSegment();
                    mDBHelper.deleteMovie(MovieContract.TABLE_NAME_TOP_RATED, movieTitle);
                    break;

                case SAVED:
                    mDBHelper.deleteAllMovies(MovieContract.TABLE_NAME_SAVED);
                    break;
                case SAVED_ITEM:
                    movieTitle = uri.getLastPathSegment();
                    mDBHelper.deleteMovie(MovieContract.TABLE_NAME_SAVED, movieTitle);
                    break;

                default:
                    Log.v("Invalid-URI", uri.toString());

            }

            return 0;
        }

        @Override
        public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
            return 0;
        }

}