package com.android.akshatamohanty.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set the details view
        Intent intent = getIntent();

        TextView movie_title =  (TextView) findViewById(R.id.movie_title);
        TextView movie_plotsynopsis =  (TextView) findViewById(R.id.movie_plotsynopsis);
        TextView movie_rating =  (TextView) findViewById(R.id.movie_rating);
        TextView movie_releaseDate =  (TextView) findViewById(R.id.movie_releaseDate);
        ImageView movie_poster =  (ImageView) findViewById(R.id.movie_poster);

        String title = intent.getStringExtra("mTitle");
        String synopsis = intent.getStringExtra("mSynopsis");
        String rating = intent.getStringExtra("mRating");
        String release = intent.getStringExtra("mRelease");
        String poster = intent.getStringExtra("mPoster");

        movie_title.setText(title);
        movie_plotsynopsis.setText(synopsis);
        movie_rating.setText(rating);
        movie_releaseDate.setText(release);

        String baseURL = "http://image.tmdb.org/t/p";
        String size = "/w185";

        Picasso.with(this).load(baseURL.concat(size).concat(poster)).into(movie_poster);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

}