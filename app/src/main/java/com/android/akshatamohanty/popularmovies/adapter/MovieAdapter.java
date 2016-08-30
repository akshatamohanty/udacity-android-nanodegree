package com.android.akshatamohanty.popularmovies.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.akshatamohanty.popularmovies.DetailActivity;
import com.android.akshatamohanty.popularmovies.DetailActivityFragment;
import com.android.akshatamohanty.popularmovies.MainActivity;
import com.android.akshatamohanty.popularmovies.MainActivityFragment;
import com.android.akshatamohanty.popularmovies.R;
import com.android.akshatamohanty.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by skyfishjy on 10/31/14.
 */
public class MovieAdapter extends CursorRecyclerViewAdapter<MovieAdapter.ViewHolder>{

    private String LOG_TAG = this.getClass().getSimpleName();
    private MovieDetailsCallback callback;

    private int selectedPos = 0;

    public MovieAdapter(Context context, Cursor cursor){
        super(context,cursor);
    }

    public void setCallback(MovieDetailsCallback callback){

        this.callback = callback;
    }

    public interface MovieDetailsCallback {

        void showDetails(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        public ImageView movie_poster;
        private Context parent_context;
        public JSONObject movieInfo = new JSONObject();

        public ViewHolder(View v, Context ctxt) {
            super(v);
            parent_context = ctxt;
            movie_poster = (ImageView) v.findViewById(R.id.movie_poster);

            v.setOnClickListener(this);
        }

        public void setMovieInfo(JSONObject info){
            movieInfo = info;
        }


        public Context getContext(){
            return parent_context;
        }

        @Override
        public void onClick(View view) {
            if(callback != null) {
                selectedPos = getLayoutPosition();
                notifyItemChanged(selectedPos);

                callback.showDetails(selectedPos);
            }
        }
    }

    @Override
    public MovieAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view, parent, false);

        return new ViewHolder(v, parent.getContext());
    }

    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor){
        // do nothing
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        String movie_poster = "poster_path";
        JSONObject movie;

        holder.itemView.setSelected(selectedPos == position);

        try {

            movie = this.getItem(position);
            holder.setMovieInfo(movie);

            String baseURL = "http://image.tmdb.org/t/p";
            String size = "/w185";
            String pictureURL = movie.getString(movie_poster);

            Picasso.with(holder.getContext())
                    .load(baseURL.concat(size).concat(pictureURL))
                    .into(holder.movie_poster);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getItem(int position) {

        JSONObject movieInfo = new JSONObject();
        Cursor cursor = this.getCursor();

        if(cursor!=null && cursor.isClosed() != true && cursor.getCount()!= 0 && cursor.moveToPosition(position) != false) {
            // Load data from dataCursor and return it...
            try
            {
                int did = cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_NAME_MOVIE_ID);
                int dtitle = cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_NAME_TITLE);
                int dposter = cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_NAME_POSTER);
                int drating = cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_NAME_RATING);
                int drelease = cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_NAME_RELEASE);
                int dsynopsis = cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_NAME_SYNOPSIS);

                movieInfo.put("id", cursor.getString(did));
                movieInfo.put("title", cursor.getString(dtitle));
                movieInfo.put("poster_path", cursor.getString(dposter));
                movieInfo.put("release_date", cursor.getString(drelease));
                movieInfo.put("vote_average", cursor.getString(drating));
                movieInfo.put("overview", cursor.getString(dsynopsis));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return movieInfo;
        }
        else{
            Log.e(LOG_TAG, "Cursor is null or count 0 in Adapter");
            return null;
        }

    }

    @Override
    public int getItemCount(){

       if(getCursor() == null || getCursor().isClosed())
           return 0;
       else
           return getCursor().getCount();
    }

}