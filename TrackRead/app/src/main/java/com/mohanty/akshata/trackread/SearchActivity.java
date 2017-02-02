package com.mohanty.akshata.trackread;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;



public class SearchActivity extends AppCompatActivity {

    private Context mContext;
    private Boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mContext = this;
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();


        // attach click functionality to the button
        Button search = (Button) findViewById(R.id.searchButton);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {

                    EditText searchText = (EditText) findViewById(R.id.searchText);
                    //Toast.makeText(mContext, searchText.getText(), Toast.LENGTH_SHORT).show();
                    SearchBooksTask books = new SearchBooksTask();
                    books.execute(searchText.getText().toString());

                } else {
                    networkToast();
                }

            }
        });
    }

    public void networkToast(){
        Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
    }

    private class SearchBooksTask extends AsyncTask<String, Void, String>{

        private final String LOG_TAG = SearchBooksTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... searchText) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String booksStr = null;

            try {

                final String GOODREADS_BASE_URL =
                        "https://www.goodreads.com/search/index.xml";
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
                    buffer.append(line);
                }

                if (buffer.length() == 0) {
                    return null;
                }

                booksStr = buffer.toString();
                Log.d(LOG_TAG, booksStr.substring( booksStr.length() - 50 ) );

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
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.v(LOG_TAG, "in on post execute");
            Toast.makeText(mContext, "Result received", Toast.LENGTH_SHORT).show();

            /*JSONObject jsonObj = null;
            try {
                jsonObj = XML.toJSONObject(result);
                Log.d(LOG_TAG, "XML to JSON converted");
            } catch (JSONException e) {
                Log.e("JSON exception", e.getMessage());
                e.printStackTrace();
            }

            Log.d("JSON", jsonObj.toString());*/
        }
    }

}


