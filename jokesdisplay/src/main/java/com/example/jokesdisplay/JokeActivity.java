package com.example.jokesdisplay;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class JokeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joke);

        Bundle extras = getIntent().getExtras();
        String joke;

        if (extras != null) {
            joke = extras.getString("joke");

            TextView view = (TextView) findViewById(R.id.jokeView);
            view.setText(joke);
        }



    }
}
