package com.mohanty.akshata.trackread;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;



public class SearchActivity extends AppCompatActivity {

    private String LOG_TAG = SearchActivity.class.getSimpleName();

    private Context mContext;
    private Boolean isConnected;

    private String searchtxt;

    Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // If the screen is rotated, the movies shouldn't change
        if(savedInstanceState != null){
            searchtxt = savedInstanceState.getString("searchText");
            EditText searchText = (EditText) findViewById(R.id.searchText);
            searchText.setText(searchtxt);
        }

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        mContext = this;
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // attach click functionality to the button
        Button search = (Button) findViewById(R.id.searchButton);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {

                    EditText searchText = (EditText) findViewById(R.id.searchText);
                    searchtxt = searchText.getText().toString();
                    if(searchtxt != null && !searchtxt.isEmpty()){
                        SearchBooksTask books = new SearchBooksTask();
                        books.execute(searchtxt.toString());
                    }
                    else{
                        Toast.makeText(mContext, R.string.error_message, Toast.LENGTH_SHORT).show();
                    }

                } else {
                    networkToast();
                }

            }
        });
    }

    // network toast to display if no net connectivity
    public void networkToast(){
        Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putString("searchText", searchtxt);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.i(LOG_TAG, "Setting screen name: " + LOG_TAG);
        mTracker.setScreenName("Image~" + LOG_TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // Async Task to fetch search data
    private class SearchBooksTask extends AsyncTask<String, Void, String>{

        private final String LOG_TAG = SearchBooksTask.class.getSimpleName();

        private void showResults(JSONArray books){
            Toast.makeText(mContext, "Result received", Toast.LENGTH_SHORT).show();

            Intent obj_intent = new Intent(SearchActivity.this, SearchResultsActivity.class);
            Bundle b = new Bundle();
            b.putString("books", books.toString());
            obj_intent.putExtras(b);
            startActivity(obj_intent);
        }

        @Override
        protected String doInBackground(String... searchText) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String booksStr = null;

            try {

                final String GOODREADS_BASE_URL = getResources().getString(R.string.goodreads_url);

                final String APPID_PARAM = "key";
                final String SEARCH_PARAM = "q";

                Uri builtUri = Uri.parse(GOODREADS_BASE_URL).buildUpon()
                        .appendQueryParameter(APPID_PARAM, BuildConfig.GOODREADS_DEVELOPER_KEY)
                        .appendQueryParameter(SEARCH_PARAM, searchText[0])
                        .build();

                URL url = new URL(builtUri.toString());

                Log.d(LOG_TAG, url.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                int count = 0;
                while ((line = reader.readLine()) != null) {
                    count++;
                    // skip the CDATA lines
                    if(count == 5 || count == 6 || count == 9)
                        continue;
                    buffer.append(line);
                }

                if (buffer.length() == 0) {
                    return null;
                }

                booksStr = buffer.toString();

                return booksStr;

            } catch (Exception e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            }
            finally {
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
        }

        @Override
        protected void onPostExecute(String result) {

            Log.v(LOG_TAG, "in on post execute");

            if(result != null && !result.isEmpty() && !result.equals("null")){
                JSONObject jsonObj = null;
                try {
                    jsonObj = XML.toJSONObject(result);
                    Log.d(LOG_TAG, "XML to JSON converted");

                    JSONArray res = jsonObj.getJSONObject("GoodreadsResponse").getJSONObject("search").getJSONObject("results").getJSONArray("work");
                    Log.d("JSON", String.valueOf(res.length()) );

                    showResults(res);

                } catch (JSONException e) {
                    Log.e("JSON exception", e.getMessage());
                    Toast.makeText(mContext, R.string.error_message, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

            }
            else{
                Log.v(LOG_TAG, "Result is null");
            }
        }
    }

}


