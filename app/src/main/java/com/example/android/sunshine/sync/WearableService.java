package com.example.android.sunshine.sync;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.android.sunshine.DetailActivity;
import com.example.android.sunshine.MainActivity;
import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.utilities.SunshineWeatherUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Akshata on 11/1/2017.
 */

public class WearableService extends WearableListenerService {

    String LOG_TAG = WearableService.class.getSimpleName();
    String path = "/weather-info";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(path)) {
            final String message = new String(messageEvent.getData());
            Log.v("myTag", "Message path received on app is: " + messageEvent.getPath());
            Log.v("myTag", "Message received on app is: " + message);

            updateWatchFace();
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }

    private void updateWatchFace(){

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        ConnectionResult connectionResult =
                googleApiClient.blockingConnect(30, TimeUnit.SECONDS);

        if (!connectionResult.isSuccess()) {
            return;
        }


        Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;
                /* Sort order: Ascending by date */
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
                /*
                 * A SELECTION in SQL declares which rows you'd like to return. In our case, we
                 * want all weather data from today onwards that is stored in our weather table.
                 * We created a handy method to do that in our WeatherEntry class.
                 */
        String selection = WeatherContract.WeatherEntry.getSqlSelectForTodayOnwards();

        // query the provider
        Cursor mCursor = getContentResolver().query(forecastQueryUri,
                MainActivity.MAIN_FORECAST_PROJECTION,
                selection,
                null,
                sortOrder);

        if(mCursor != null){
            mCursor.moveToFirst();

            //Log.v(LOG_TAG, SunshineWeatherUtils.formatTemperature(this, mCursor.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP)) );

            PutDataMapRequest weatherMapRequest = PutDataMapRequest.create(path);
            DataMap weatherDataMap = weatherMapRequest.getDataMap();

            weatherDataMap.putString("high", SunshineWeatherUtils.formatTemperature(this, mCursor.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP)) );
            weatherDataMap.putString("low", SunshineWeatherUtils.formatTemperature(this, mCursor.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP) ));
            weatherDataMap.putInt("weatherId", mCursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID) );

            // to trigger onDataChanged everytime
            weatherDataMap.putString("timestamp", new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date())  );

            PutDataRequest weatherRequest = weatherMapRequest.asPutDataRequest();
            Wearable.DataApi.putDataItem(googleApiClient, weatherRequest);

            mCursor.close();
        }


    }

}
