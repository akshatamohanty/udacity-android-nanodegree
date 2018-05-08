package com.android.akshatamohanty.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    private JSONArray mDataset;
    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder
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

        @Override
        public void onClick(View view) {

            Intent intent = new Intent( parent_context , DetailActivity.class);
            try {
                intent.putExtra("mTitle", movieInfo.getString("title"));
                intent.putExtra("mSynopsis", movieInfo.getString("overview"));
                intent.putExtra("mPoster", movieInfo.getString("poster_path"));
                intent.putExtra("mRating", movieInfo.getString("vote_average"));
                intent.putExtra("mRelease", movieInfo.getString("release_date"));

                parent_context.startActivity(intent);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public MovieAdapter(Context ref_context, JSONArray myDataset) {
        context = ref_context;
        mDataset = myDataset;
    }

    public void updateDataset( JSONArray dataset ){
        mDataset = dataset;
        notifyDataSetChanged();
    }

    @Override
    public MovieAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view, parent, false);

        return new ViewHolder(v, context);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        String movie_poster = "poster_path";
        JSONObject movie = null;

        try {
            movie = mDataset.getJSONObject(position);
            holder.setMovieInfo(movie);

            String baseURL = "http://image.tmdb.org/t/p";
            String size = "/w185";
            String pictureURL = movie.getString(movie_poster);

            Picasso.with(context)
                    .load(baseURL.concat(size).concat(pictureURL))
                    .into(holder.movie_poster);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.length();
    }

}
