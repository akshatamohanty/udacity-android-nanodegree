package com.mohanty.akshata.trackread;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mohanty.akshata.trackread.data.BooksContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class SearchResultsActivity extends AppCompatActivity {

    private static String LOG_TAG = SearchResultsActivity.class.getSimpleName();
    private JSONArray booksArray = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();
        String books = b.getString("books");

        try {
            booksArray = new JSONArray(books);
            displayBooks(booksArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void displayBooks(JSONArray books) throws JSONException {

        // Get ListView object from xml
        final ListView listView = (ListView) findViewById(R.id.results);
        listView.setItemsCanFocus(true);

        // Defined Array values to show in ListView
        String[] values = new String[booksArray.length()];

        for(int i=0; i<booksArray.length(); i++){
            JSONObject book = (JSONObject) booksArray.get(i);
            String title = book.getJSONObject("best_book").getString("title");
            String author = book.getJSONObject("best_book").getJSONObject("author").getString("name");
            String res = title + "\nBy: " + author;
            values[i] = res;
        }

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

                // Add item to database
                try {
                    JSONObject clkBook = (JSONObject) booksArray.get(itemPosition);
                    SaveBook dbTsk = new SaveBook();
                    dbTsk.execute(clkBook);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        });

    }

    private class SaveBook extends AsyncTask<JSONObject, Void, Boolean> {

        String bookId="";

        @Override
        protected Boolean doInBackground(JSONObject... params) {

            try{
                JSONObject book = params[0].getJSONObject("best_book");
                String id = book.getJSONObject("id").getString("content");
                String title = book.getString("title");
                String author = book.getJSONObject("author").getString("name");
                String image_url = book.getString("image_url");

                ContentResolver mResolver = getContentResolver();

                Uri uri = BooksContract.buildUrlForTable();

                ContentValues books_values = new ContentValues();
                books_values.put(BooksContract.BookEntry.COLUMN_NAME_BOOK_ID, id);
                books_values.put(BooksContract.BookEntry.COLUMN_NAME_AUTHOR, author);
                books_values.put(BooksContract.BookEntry.COLUMN_NAME_TITLE, title);
                books_values.put(BooksContract.BookEntry.COLUMN_NAME_DATE_ADDED, (new Date()).toString());
                books_values.put(BooksContract.BookEntry.COLUMN_NAME_IMAGE, image_url);
                books_values.put(BooksContract.BookEntry.COLUMN_NAME_NOTES, "");
                books_values.put(BooksContract.BookEntry.COLUMN_NAME_STATUS, BooksContract.STATUS_CURRENT);
                books_values.put(BooksContract.BookEntry.COLUMN_NAME_CURRENT_PAGE, "0");
                books_values.put(BooksContract.BookEntry.COLUMN_NAME_TOTAL_PAGES, "0");

                if( mResolver.insert(uri, books_values) != null ){
                    return true;
                }

            }
            catch(JSONException e){
                //Log.v(LOG_TAG, "JSON-Exception");
            }

            return false;

        }

        protected void onPostExecute(Boolean result) {

            // Main activity
            Intent obj_intent = new Intent(SearchResultsActivity.this, MainActivity.class);
            Bundle b = new Bundle();

            if(result){
                b.putBoolean("new", true);
                obj_intent.putExtras(b);
            }
            else{
                b.putBoolean("new", true);
                obj_intent.putExtras(b);
            }

            startActivity(obj_intent);

        }


    }


}
