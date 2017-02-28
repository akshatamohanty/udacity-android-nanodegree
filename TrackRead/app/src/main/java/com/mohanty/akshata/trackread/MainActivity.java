package com.mohanty.akshata.trackread;

import android.content.ContentValues;
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
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mohanty.akshata.trackread.data.BooksContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static String LOG_TAG = MainActivity.class.getSimpleName();

    private int pos = 0;
    private boolean result = false;
    private ViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;

    Tracker mTracker;
    private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if(savedInstanceState != null){
            pos = savedInstanceState.getInt("pos");
        }

        // Google Analytics
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        // Admob
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);


        try{
            Bundle b = getIntent().getExtras();
            result = b.getBoolean("new");
        }catch(Exception e){
            // do something
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

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
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("AddBook")
                        .build());
                Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(searchIntent);
                break;
            case R.id.menu_archive:
                addBookToArchive();
            case R.id.menu_statistics:
                Intent statsIntent = new Intent(MainActivity.this, StatisticsActivity.class);
                startActivity(statsIntent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addBookToArchive(){
        ContentValues args = new ContentValues();
        args.put(BooksContract.BookEntry.COLUMN_NAME_STATUS, BooksContract.STATUS_ARCHIVED);
        int currentPage = mPager.getCurrentItem();
        String bookId = mPagerAdapter.getBookId(currentPage);
        getContentResolver().update(
                BooksContract.buildUriForBookDetails(bookId),
                args, null, null);

        // restart the cursor

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("key", mPager.getCurrentItem());
    }

    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {

        Uri contentURI;
        switch(i){
            case 0:
                contentURI = BooksContract.buildUriForCurrent();
                break;
            default:
                contentURI = BooksContract.buildUrlForTable();
                break;
        }

        //Log.v(LOG_TAG, "onCreateLoader-" +contentURI.toString());
        Loader loader = new CursorLoader(this, contentURI, null, null, null, null);

        return loader;
    }

    public void onLoadFinished(Loader loader, Cursor data) {

        mPager = (ViewPager) findViewById(R.id.pager);

        //Log.v(LOG_TAG, "Items in database: " + String.valueOf(data.getCount()));

        if(data.getCount() > 0){
            mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
            mPagerAdapter.setCursor(data);
            mPager.setAdapter(mPagerAdapter);

            mPagerAdapter.notifyDataSetChanged();

            if(mPagerAdapter.getCount() >= pos){
                mPager.setCurrentItem(pos);
            }

            if(result)
                mPager.setCurrentItem(mPagerAdapter.getItemCount());
        }
        else{
            //Toast.makeText(this, "No books found in database.", Toast.LENGTH_LONG).show();
            mPager.setVisibility(View.INVISIBLE);
            findViewById(R.id.no_books_container).setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onLoaderReset(Loader loader) {
        if(mPagerAdapter != null)
            mPagerAdapter.setCursor(null);
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
        //Log.i(LOG_TAG, "Setting screen name: " + LOG_TAG);
        mTracker.setScreenName("Image~" + LOG_TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }



    /**
     * A simple pager adapter
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        Cursor cursor;

        @Override
        public Fragment getItem(int position) {

            //Log.v(LOG_TAG, String.valueOf(position));

            cursor.moveToPosition(position);
            String bookId = cursor.getString(cursor.getColumnIndex(BooksContract.BookEntry.COLUMN_NAME_BOOK_ID));
            String title = cursor.getString(cursor.getColumnIndex(BooksContract.BookEntry.COLUMN_NAME_TITLE));
            String author = cursor.getString(cursor.getColumnIndex(BooksContract.BookEntry.COLUMN_NAME_AUTHOR));
            String image = cursor.getString(cursor.getColumnIndex(BooksContract.BookEntry.COLUMN_NAME_IMAGE));
            String date = cursor.getString(cursor.getColumnIndex(BooksContract.BookEntry.COLUMN_NAME_DATE_ADDED));
            String notes = cursor.getString(cursor.getColumnIndex(BooksContract.BookEntry.COLUMN_NAME_NOTES));

            return ScreenSlidePageFragment.newInstance(position, bookId, title, author, image, date, notes);
        }

        public String getBookId(int position){
            cursor.moveToPosition(position);
            return cursor.getString(cursor.getColumnIndex(BooksContract.BookEntry.COLUMN_NAME_BOOK_ID));
        }


        @Override
        public int getCount() {
            return getItemCount();
        }

        public void setCursor(Cursor data){
            cursor = data;
        }

        public int getItemCount(){
            if(cursor == null)
                return 0;
            else{
                return cursor.getCount();
            }
        };
    }

}

