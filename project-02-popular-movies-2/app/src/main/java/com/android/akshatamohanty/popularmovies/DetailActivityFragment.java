package com.android.akshatamohanty.popularmovies;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Movie;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.support.v7.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.android.akshatamohanty.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by Akshata on 21/8/2016.
 */
public class DetailActivityFragment extends Fragment{

    final String LOG_TAG = this.getClass().getSimpleName();
    private ShareActionProvider mShareActionProvider;
    private Intent shareIntent = new Intent(Intent.ACTION_SEND);
    JSONObject mInfo;

    public DetailActivityFragment(){

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        if( getArguments() != null ) {
            updateUI();
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_detail, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.share_movie_details);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        setShareIntent();

    }

    // Call to update the share intent
    private void setShareIntent() {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
            Log.v(LOG_TAG, "Intent Updated");
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    public void updateUI(){

            JSONObject movieInfo = new JSONObject();
            Set<String> keys = getArguments().keySet();
            for (String key : keys) {
                try {
                    movieInfo.put(key, getArguments().get(key));
                } catch(JSONException e) {
                    Log.e(LOG_TAG, e.toString());
                }
            }

            updateUI( movieInfo );
    }

    public void updateUI(final JSONObject movieInfo){

        if(movieInfo == null && getView() != null){
            getView().setVisibility(View.GONE);
            Log.v(LOG_TAG, "No movies to display - Setting details fragment to invisible");
            return;
        }

        try{

            getView().setVisibility(View.VISIBLE);

            mInfo = movieInfo;

            TextView movie_title =  (TextView) getView().findViewById(R.id.movie_title);
            TextView movie_plotsynopsis =  (TextView) getView().findViewById(R.id.movie_plotsynopsis);
            TextView movie_rating =  (TextView) getView().findViewById(R.id.movie_rating);
            TextView movie_releaseDate =  (TextView) getView().findViewById(R.id.movie_releaseDate);
            ImageView movie_poster =  (ImageView) getView().findViewById(R.id.movie_poster);

            final String id = movieInfo.getString(getString(R.string.movie_id));
            final String title = movieInfo.getString(getString(R.string.movie_title));
            final String synopsis = movieInfo.getString(getString(R.string.movie_plot));
            final String rating = movieInfo.getString(getString(R.string.movie_rating));
            final String release = movieInfo.getString(getString(R.string.movie_release));
            final String poster = movieInfo.getString(getString(R.string.movie_poster));

            movie_title.setText(title);
            movie_plotsynopsis.setText(synopsis);
            movie_rating.setText(String.format(getResources().getString(R.string.formatted_rating), rating));
            movie_releaseDate.setText(release);

            String baseURL = "http://image.tmdb.org/t/p";
            String size = "/w185";

            Picasso.with(getActivity()).load(baseURL.concat(size).concat(poster)).into(movie_poster);

            // This will check the status of the movie (saved or unsaved) and attach corresponding listener to button
            ConfigureButton btnTask = new ConfigureButton();
            btnTask.execute();

            AdditionalDetails videosTsk = new AdditionalDetails();
            AdditionalDetails reviewsTsk = new AdditionalDetails();
            videosTsk.execute(id, "videos");
            reviewsTsk.execute(id, "reviews");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void updateAdditionalDetails(String view, String jsonStr) throws JSONException {

        JSONObject result = new JSONObject(jsonStr);
        JSONArray entries = result.getJSONArray("results");

        ArrayList<JSONObject> list = new ArrayList<>();
        for (int i=0; i<entries.length(); i++) {
            list.add(entries.getJSONObject(i));
        }

        if(entries.length() > 0){
            if(view.equals("reviews")){

                ReviewsFragment reviewsFrag = (ReviewsFragment) getChildFragmentManager().findFragmentById(R.id.fragment_reviews);

                if(reviewsFrag != null)
                    reviewsFrag.update(list);
                else{
                    Log.v(LOG_TAG, "Reviews fragment is absent");
                }


            }

            if(view.equals("videos")){

                VideosFragment videosFrag = (VideosFragment) getChildFragmentManager()
                        .findFragmentById(R.id.fragment_videos);
                if(videosFrag != null)
                    videosFrag.update(list);
                else
                    Log.v(LOG_TAG, "Videos fragment is absent");

                // update shareIntent to share the first movie video

                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, VideosFragment.getYoutubeURL(list.get(0).getString("key")).toString());

                setShareIntent();
            }
        }

    }

    private class AdditionalDetails extends AsyncTask<String, Void, Boolean> {

        private String jsonStr = null;
        private String criteria = null;

        @Override
        protected Boolean doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {

                final String FORECAST_BASE_URL =
                        "http://api.themoviedb.org/3/movie/";
                final String APPID_PARAM = "api_key";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendPath(params[0])
                        .appendPath(params[1])
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

                jsonStr = buffer.toString();

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

            criteria= params[1];

            return true;
        }

        protected void onPostExecute(Boolean result) {

            if(result)
                try {
                    updateAdditionalDetails(criteria, jsonStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

        }
    }


    /*
     * Classes for Database Manipulations
     */
    private class ConfigureButton extends AsyncTask<Void, Void, Boolean>{


        @Override
        protected Boolean doInBackground(Void... params) {

            try{

                String title = mInfo.getString(getString(R.string.movie_title));

                ContentResolver mResolver = getActivity().getContentResolver();
                Uri getMovieDetails = MovieContract.buildUriForSavedMovieDetails(title);

                Cursor cursor = mResolver.query(getMovieDetails, null, null, null, null);

                if(cursor.getCount()>0){
                    cursor.close();
                    return true;
                }
                else{
                    cursor.close();
                    return false;
                }
            }
            catch(JSONException e){

            }

            //default
            return false;
        }

        protected void onPostExecute(Boolean result) {

            Button action_button =  (Button) getActivity().findViewById(R.id.save);

            if(result){
                action_button.setText(R.string.delete_label);
                action_button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        DeleteMovie dbTsk = new DeleteMovie();
                        dbTsk.execute();

                    }
                });
            }
            else{
                action_button.setText(R.string.save_label);
                action_button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        SaveMovie dbTsk = new SaveMovie();
                        dbTsk.execute();
                    }
                });
            }

        }
    }


    private class SaveMovie extends AsyncTask<Void, Void, Boolean>{

        String title="";

        @Override
        protected Boolean doInBackground(Void... params) {

            try{
                String id = mInfo.getString(getString(R.string.movie_id));
                title = mInfo.getString(getString(R.string.movie_title));
                String synopsis = mInfo.getString(getString(R.string.movie_plot));
                String rating = mInfo.getString(getString(R.string.movie_rating));
                String release = mInfo.getString(getString(R.string.movie_release));
                String poster = mInfo.getString(getString(R.string.movie_poster));


                ContentResolver mResolver = getActivity().getContentResolver();

                Uri uri = MovieContract.buildUriForSaved();


                ContentValues movie_values = new ContentValues();
                movie_values.put(MovieContract.MovieEntry.COLUMN_NAME_MOVIE_ID, id);
                movie_values.put(MovieContract.MovieEntry.COLUMN_NAME_TITLE, title );
                movie_values.put(MovieContract.MovieEntry.COLUMN_NAME_SYNOPSIS, synopsis );
                movie_values.put(MovieContract.MovieEntry.COLUMN_NAME_POSTER, poster ) ;
                movie_values.put(MovieContract.MovieEntry.COLUMN_NAME_RATING, rating );
                movie_values.put(MovieContract.MovieEntry.COLUMN_NAME_RELEASE, release );

                if( mResolver.insert(uri, movie_values) != null ){
                    return true;
                }

            }
            catch(JSONException e){
                Log.v(LOG_TAG, "JSON-Exception");
            }

            return false;

        }

        protected void onPostExecute(Boolean result) {

            if(result){
                ConfigureButton btnTask = new ConfigureButton();
                btnTask.execute();
                Toast.makeText( getActivity().getBaseContext(),
                        String.format(getResources().getString(R.string.success_string_save), title),
                        Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText( getActivity().getBaseContext(),
                        String.format(getResources().getString(R.string.error_string_save), title),
                        Toast.LENGTH_SHORT).show();

        }


    }

    private class DeleteMovie extends AsyncTask<Void, Void, Boolean>{

        String title = "";
        @Override
        protected Boolean doInBackground(Void... params) {

            try{

                title = mInfo.getString(getString(R.string.movie_title));

                ContentResolver mResolver = getActivity().getContentResolver();

                Uri uri = MovieContract.buildUriForSavedMovieDetails(title);

                mResolver.delete(uri, null, null);

                return true;
            }
            catch(JSONException e){
                Log.v(LOG_TAG, "JSON-Exception");
            }

            return false;

        }

        protected void onPostExecute(Boolean result) {

            if(result){
                ConfigureButton btnTask = new ConfigureButton();
                btnTask.execute();
                Toast.makeText( getActivity().getBaseContext(),
                        String.format(getResources().getString(R.string.success_string_delete), title),
                        Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText( getActivity().getBaseContext(),
                        String.format(getResources().getString(R.string.error_string_delete), title),
                        Toast.LENGTH_SHORT).show();

        }

    }

}
