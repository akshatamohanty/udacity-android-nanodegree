package com.mohanty.akshata.trackread;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchResultsActivity extends AppCompatActivity {

    private static String LOG_TAG = SearchResultsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        Bundle b = getIntent().getExtras();
        String books = b.getString("books");

        JSONArray booksArray = null;
        try {
            booksArray = new JSONArray(books);
            Log.v(LOG_TAG, String.valueOf(booksArray.length()));
            displayBooks(booksArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void displayBooks(JSONArray books) throws JSONException {

        // Get ListView object from xml
        final ListView listView = (ListView) findViewById(R.id.results);

        // Defined Array values to show in ListView
        String[] values = new String[books.length()];

        for(int i=0; i<books.length(); i++){
            JSONObject book = (JSONObject) books.get(i);
            String title = book.getJSONObject("best_book").getString("title");
            String author = book.getJSONObject("best_book").getJSONObject("author").getString("name");
            String img = book.getJSONObject("best_book").getString("image_url");
            String res = title + " " + author + " " + img;
            Log.v(LOG_TAG, res);
            values[i] = res;
        }

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);


        // Assign adapter to ListView
        listView.setAdapter(adapter);

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition     = position;

                // ListView Clicked item value
                String  itemValue    = (String) listView.getItemAtPosition(position);

                // Show Alert
                Toast.makeText(getApplicationContext(),
                        "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
                        .show();

                // Main activity
                Intent obj_intent = new Intent(SearchResultsActivity.this, MainActivity.class);
                Bundle b = new Bundle();
                b.putString("book", itemValue);
                obj_intent.putExtras(b);
                startActivity(obj_intent);

            }

        });

    }


}
