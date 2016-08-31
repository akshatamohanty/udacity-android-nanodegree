package com.android.akshatamohanty.popularmovies;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.UiThread;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;

import com.android.akshatamohanty.popularmovies.data.MovieContract;
import com.android.akshatamohanty.popularmovies.adapter.MovieAdapter;
import com.android.akshatamohanty.popularmovies.sync.MovieSyncAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, MovieAdapter.MovieDetailsCallback{

    final String LOG_TAG = this.getClass().getSimpleName();

    private boolean mDualPane;
    private int optSelected = 0;
    private int movieSelected = 0;

    private Context mContext = getActivity();

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private MovieAdapter mAdapter;

    public MainActivityFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        View displayFrag = getActivity().findViewById(R.id.fragment_details);
        if(displayFrag != null) {
            mDualPane = true;
            mLayoutManager = new GridLayoutManager(mContext, 3);
        }
        else {
            mDualPane = false;
            mLayoutManager = new GridLayoutManager(mContext, 2);
        }

        // Recycler View Adapter Implementation
        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MovieAdapter(mContext, null);
        mAdapter.setCallback( this );
        mRecyclerView.setAdapter(mAdapter);


        // initialize the loader
        getLoaderManager().initLoader(optSelected, null, this);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // If the screen is rotated, the movies shouldn't change
        if(savedInstanceState != null){
            optSelected = savedInstanceState.getInt("optSelected");
            movieSelected = savedInstanceState.getInt("movieSelected");
        }


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putInt("optSelected", optSelected);
        outState.putInt("movieSelected", movieSelected);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_toprated:
                optSelected = 0;
                break;
            case R.id.menu_popular:
                optSelected = 1;
                break;
            case R.id.menu_saved:
                optSelected = 2;
                break;
        }

        ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.progress_spinner);
        progressBar.setVisibility(View.VISIBLE);

        getLoaderManager().restartLoader(optSelected, null, this);

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_main, container, false);
    }


    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {

        Uri contentURI;
        switch(i){
            case 0:
                contentURI = MovieContract.buildUriForTopRated();
                break;
            case 1:
                contentURI = MovieContract.buildUriForPopular();
                break;
            case 2:
                contentURI = MovieContract.buildUriForSaved();
                break;
            default:
                contentURI = MovieContract.buildUriForTopRated();
                break;
        }

        Log.v(LOG_TAG, "onCreateLoader-" +contentURI.toString());
        Loader loader = new CursorLoader(getActivity(), contentURI, null, null, null, null);

        return loader;
    }

    public void onLoadFinished(Loader loader, Cursor data) {

        // Change cursor of Adapter to visualize data in RecyclerView
        Log.v(LOG_TAG, "onLoadFinished old:" + String.valueOf(mAdapter.getItemCount()));

        mAdapter.changeCursor(data);
        mAdapter.notifyDataSetChanged();

        Log.v(LOG_TAG, "onLoadFinished new:" + String.valueOf(mAdapter.getItemCount()));

        if ( mDualPane  ) {

            int items = mAdapter.getItemCount();
            if (items == 0 || items < movieSelected)
                showDetails(0);
            else
                showDetails(movieSelected);
        }

        if(loader.getId() == 3 || mAdapter.getItemCount()>0){
            ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.progress_spinner);
            progressBar.setVisibility(View.GONE);
        }

    }

    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public void showDetails(int position) {

        movieSelected = position;
        JSONObject movieInfo = mAdapter.getItem(position);

        if ( !mDualPane  ) {
            // DisplayFragment (Fragment B) is not in the layout (handset layout),
            // so start DisplayActivity (Activity B)
            // and pass it the info about the selected item
            Intent intent = new Intent( getActivity() , DetailActivity.class);
            try {
                intent.putExtra("id", movieInfo.getString("id"));
                intent.putExtra("title", movieInfo.getString("title"));
                intent.putExtra("overview", movieInfo.getString("overview"));
                intent.putExtra("poster_path", movieInfo.getString("poster_path"));
                intent.putExtra("vote_average", movieInfo.getString("vote_average"));
                intent.putExtra("release_date", movieInfo.getString("release_date"));

                startActivity(intent);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {

            DetailActivityFragment displayFrag = (DetailActivityFragment) getFragmentManager()
                    .findFragmentById(R.id.fragment_details);

            // updateUI will handle case of movieInfo being null
            displayFrag.updateUI( movieInfo );
        }
    }

    /*
     * Syncs data only if data is more than half a day old
     */
    public static void syncData(Activity activity){
        final String PREFS_NAME = "updateLogFile";

        SharedPreferences updates = activity.getSharedPreferences(PREFS_NAME, 0);

        Long today = new Date(System.currentTimeMillis()).getTime();
        Long lastUpdated = updates.getLong("lastUpdate", 0);

        // check difference in number of milliseconds between the two updates
        // Half a day - 43200s
        if (today - lastUpdated > 43200000) {

            // Sync when data is outdated
            // Log.v(LOG_TAG, "Sync Called");
            MovieSyncAdapter.syncImmediately( activity );

            // record the fact that the app has been started at least once
            updates.edit().putLong("lastUpdate", today).apply();
            ProgressBar progressBar = (ProgressBar) activity.findViewById(R.id.progress_spinner);
            progressBar.setVisibility(View.VISIBLE);
        }
        else
            Log.v("MainActivityFragment", "Database already up-to-date");
    }

}
