package com.android.akshatamohanty.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.graphics.Movie;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.android.akshatamohanty.popularmovies.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.android.akshatamohanty.popularmovies.R;
import com.android.akshatamohanty.popularmovies.data.MovieContract;

/**
 * Created by Akshata on 3/8/2016.
 */

public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {

    public final static String LOG_TAG = MovieSyncAdapter.class.getSimpleName();

    private String popular;
    private String rating;

    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        popular = context.getString(R.string.criteria_popular);
        rating = context.getString(R.string.criteria_rating);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        Log.v(LOG_TAG, "onPerformSync Called.");

        // Two tables for top_rated and popular movies are refreshed
        getDataFromOnlineDatabase( popular );
        getDataFromOnlineDatabase( rating );
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);

    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {

        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder()
                    .syncPeriodic(syncInterval, flexTime)
                    .setSyncAdapter(account, authority)
                    .build();
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

        }
        return newAccount;
    }

    /**
     * Connects to database online, gets the results and inserts / updates the local database
     */
    private boolean getDataFromOnlineDatabase(String criteria){

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String moviesJsonStr = null;

        try {

            final String FORECAST_BASE_URL =
                    "http://api.themoviedb.org/3/movie/";
            final String APPID_PARAM = "api_key";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendPath(criteria)
                    .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIE_DATABASE_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return false;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return false;
            }
            moviesJsonStr = buffer.toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            return insertDataIntoDatabase(moviesJsonStr, criteria);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Helper function to update the database
     */
    private boolean insertDataIntoDatabase(String moviesJsonStr, String table)
            throws JSONException {

        //Log.v(LOG_TAG, moviesJsonStr);
        ContentResolver mResolver = getContext().getContentResolver();

        JSONObject result = new JSONObject(moviesJsonStr);
        JSONArray movies = result.getJSONArray("results");

        // Default uri value is top_rated
        Uri uri = MovieContract.buildUriForTopRated();
        if( table.equalsIgnoreCase("popular") )
            uri = MovieContract.buildUriForPopular();

        // clear the table
        mResolver.delete(uri, null, null);

        ContentValues[] cv = new ContentValues[movies.length()];

        // insert the values
        for(int i=0; i < movies.length(); i++){

            JSONObject movieInfo = movies.getJSONObject(i);

            ContentValues movie_values = new ContentValues();
            movie_values.put(MovieContract.MovieEntry.COLUMN_NAME_MOVIE_ID, movieInfo.getString("id") );
            movie_values.put(MovieContract.MovieEntry.COLUMN_NAME_TITLE, movieInfo.getString("title") );
            movie_values.put(MovieContract.MovieEntry.COLUMN_NAME_SYNOPSIS, movieInfo.getString("overview")  );
            movie_values.put(MovieContract.MovieEntry.COLUMN_NAME_POSTER, movieInfo.getString("poster_path") )  ;
            movie_values.put(MovieContract.MovieEntry.COLUMN_NAME_RATING, movieInfo.getString("vote_average")  );
            movie_values.put(MovieContract.MovieEntry.COLUMN_NAME_RELEASE, movieInfo.getString("release_date")  );

            cv[i] = movie_values;

        }

        mResolver.bulkInsert(uri, cv);

        return true;
    }

}

