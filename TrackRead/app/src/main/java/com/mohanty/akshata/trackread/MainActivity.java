package com.mohanty.akshata.trackread;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.mohanty.akshata.trackread.data.BooksContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 5;
    private static String LOG_TAG = MainActivity.class.getSimpleName();

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try{
            Bundle b = getIntent().getExtras();
            String book = b.getString("book");
        }catch(Exception e){
            // do something
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        getSupportLoaderManager().initLoader(0, null, this );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_addbook:
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_statistics:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {

        Uri contentURI;
        switch(i){
            case 0:
                contentURI = BooksContract.buildUrlForTable();
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

        // Change cursor of Adapter to visualize data in RecyclerView
        //Log.v(LOG_TAG, "onLoadFinished old:" + String.valueOf(mPagerAdapter.getItemCount()));

        //mPagerAdapter.changeCursor(data);
        //mPagerAdapter.notifyDataSetChanged();

        Log.v(LOG_TAG, "onLoadFinished new:" + String.valueOf(data.getCount()));


    }

    @Override
    public void onLoaderReset(Loader loader) {
        //mPagerAdapter.changeCursor(null);
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        Cursor cursor;

        @Override
        public Fragment getItem(int position) {
            return new ScreenSlidePageFragment();
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        public void changeCursor(Cursor data){
            cursor = data;
        }

        public int getItemCount(){ return cursor.getCount(); };
    }


}

