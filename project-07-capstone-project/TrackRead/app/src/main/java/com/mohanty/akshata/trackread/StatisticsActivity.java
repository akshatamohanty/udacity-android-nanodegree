package com.mohanty.akshata.trackread;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mohanty.akshata.trackread.data.BooksContract;

import java.util.ArrayList;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor>  {

    private static String LOG_TAG = StatisticsActivity.class.getSimpleName();
    ArrayAdapter bookAdapter;
    ListView archivedContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportLoaderManager().initLoader(1, null, this );
    }

    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {

        Uri contentURI;
        switch(i){
            case 0:
                contentURI = BooksContract.buildUriForCurrent();
                break;
            case 1:
                contentURI = BooksContract.buildUriForArchived();
                break;
            default:
                contentURI = BooksContract.buildUrlForTable();
                break;
        }

        Log.v(LOG_TAG, "onCreateLoader-" +contentURI.toString());
        Loader loader = new CursorLoader(this, contentURI, null, null, null, null);

        return loader;
    }

    public void onLoadFinished(Loader loader, Cursor data) {

        archivedContainer = (ListView) findViewById(R.id.archived_container);
        //Log.v(LOG_TAG, "Items in database: " + String.valueOf(data.getCount()));

        if(data.getCount() > 0){

            List<String> list = new ArrayList<String>();

            for(int i=0; i<data.getCount(); i++){
                data.moveToPosition(i);
                list.add(data.getString(data.getColumnIndex(BooksContract.BookEntry.COLUMN_NAME_TITLE)) + " - By " +
                         data.getString(data.getColumnIndex(BooksContract.BookEntry.COLUMN_NAME_AUTHOR))
                );

            }


            bookAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
            archivedContainer.setAdapter(bookAdapter);
            archivedContainer.setItemsCanFocus(true);
        }
        else{
            Toast.makeText(this, "No books found in archives.", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onLoaderReset(Loader loader) {
        if(bookAdapter == null)
            return;
        else if(bookAdapter.getCount() > 0)
            bookAdapter.clear();
    }



}
