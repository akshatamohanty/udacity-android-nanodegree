package com.android.akshatamohanty.popularmovies;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Akshata on 25/8/2016.
 */
public class VideosFragment extends Fragment {

    final String LOG_TAG = this.getClass().getSimpleName();

    public VideosFragment(){
        //Log.v(LOG_TAG, "Created");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Log.v(LOG_TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_videos, container, false);
    }

    public void update(ArrayList<JSONObject> list) throws JSONException {

        //Log.v(LOG_TAG, "Videos Update called");
        LinearLayout layout = (LinearLayout) getView().findViewById(R.id.videosLinearLayout);

        if(layout != null){
            layout.removeAllViews();

            Log.v(LOG_TAG, String.valueOf(list.size()) + " reviews");

            int prevTextViewId = 0;

            for(int i = 0; i < list.size(); i++)
            {

                View child = getActivity().getLayoutInflater().inflate(R.layout.single_item_video, null);

                final TextView textView = (TextView) child.findViewById(R.id.single_video);
                textView.setText(list.get(i).getString("name"));

                final String url = "https://www.youtube.com/watch?v=" + list.get(i).getString("key");

                //final ImageButton button = (ImageButton) child.findViewById(R.id.go_to_video);
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    }
                });

                layout.addView(child);

            }
        }

    }
}
