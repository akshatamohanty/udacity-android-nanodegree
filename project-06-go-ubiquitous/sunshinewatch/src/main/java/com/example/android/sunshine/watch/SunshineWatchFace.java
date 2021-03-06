    /*
     * Copyright (C) 2014 The Android Open Source Project
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *      http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */

    package com.example.android.sunshine.watch;

    import android.content.BroadcastReceiver;
    import android.content.Context;
    import android.content.Intent;
    import android.content.IntentFilter;
    import android.content.res.Resources;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.graphics.Canvas;
    import android.graphics.Color;
    import android.graphics.Paint;
    import android.graphics.Rect;
    import android.graphics.Typeface;
    import android.os.Bundle;
    import android.os.Handler;
    import android.os.Message;
    import android.support.annotation.NonNull;
    import android.support.annotation.Nullable;
    import android.support.v4.content.LocalBroadcastManager;
    import android.support.wearable.watchface.CanvasWatchFaceService;
    import android.support.wearable.watchface.WatchFaceStyle;
    import android.util.Log;
    import android.view.SurfaceHolder;
    import android.view.WindowInsets;
    import android.widget.Toast;

    import com.example.android.sunshine.R;
    import com.google.android.gms.common.ConnectionResult;
    import com.google.android.gms.common.api.GoogleApiClient;
    import com.google.android.gms.common.api.PendingResult;
    import com.google.android.gms.common.api.ResultCallback;
    import com.google.android.gms.wearable.DataApi;
    import com.google.android.gms.wearable.DataEvent;
    import com.google.android.gms.wearable.DataEventBuffer;
    import com.google.android.gms.wearable.DataItem;
    import com.google.android.gms.wearable.DataMap;
    import com.google.android.gms.wearable.DataMapItem;
    import com.google.android.gms.wearable.MessageApi;
    import com.google.android.gms.wearable.MessageEvent;
    import com.google.android.gms.wearable.Node;
    import com.google.android.gms.wearable.NodeApi;
    import com.google.android.gms.wearable.PutDataMapRequest;
    import com.google.android.gms.wearable.PutDataRequest;
    import com.google.android.gms.wearable.Wearable;

    import java.lang.ref.WeakReference;
    import java.text.DateFormat;
    import java.util.Calendar;
    import java.util.Date;
    import java.util.TimeZone;
    import java.util.concurrent.TimeUnit;

    /**
     * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
     * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
     */
    public class SunshineWatchFace extends CanvasWatchFaceService {
        private static final Typeface NORMAL_TYPEFACE =
                Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

        /**
         * Update rate in milliseconds for interactive mode. We update once a second since seconds are
         * displayed in interactive mode.
         */
        private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

        /**
         * Handler message id for updating the time periodically in interactive mode.
         */
        private static final int MSG_UPDATE_TIME = 0;

        private String LOG_TAG = "Watch Face Service";
        String message = "Hello App from Watch";
        String path = "/weather-info";


        @Override
        public Engine onCreateEngine() {
            return new Engine();
        }

        private static class EngineHandler extends Handler {
            private final WeakReference<SunshineWatchFace.Engine> mWeakReference;

            public EngineHandler(SunshineWatchFace.Engine reference) {
                mWeakReference = new WeakReference<>(reference);
            }

            @Override
            public void handleMessage(Message msg) {
                SunshineWatchFace.Engine engine = mWeakReference.get();
                if (engine != null) {
                    switch (msg.what) {
                        case MSG_UPDATE_TIME:
                            engine.handleUpdateTimeMessage();
                            break;
                    }
                }
            }
        }

        private class Engine extends CanvasWatchFaceService.Engine implements DataApi.DataListener,
                GoogleApiClient.ConnectionCallbacks,
                GoogleApiClient.OnConnectionFailedListener {

                private GoogleApiClient mGoogleApiClient;


                final Handler mUpdateTimeHandler = new EngineHandler(this);
                boolean mRegisteredTimeZoneReceiver = false;
                Paint mBackgroundPaint;
                Paint mTextPaint;
                Paint mSubTextPaint;
                Paint mSmallTextPaint;

                private String mHighTemp = "0";
                private String mLowTemp = "0";
                private int mWeatherId = 200;

                boolean mAmbient;
                Calendar mCalendar;
                final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        mCalendar.setTimeZone(TimeZone.getDefault());
                        invalidate();
                    }
                };
                float mXOffset;
                float mYOffset;

                /**
                 * Whether the display supports fewer bits for each color in ambient mode. When true, we
                 * disable anti-aliasing in ambient mode.
                 */
                boolean mLowBitAmbient;

                @Override
                public void onCreate (SurfaceHolder holder){
                    super.onCreate(holder);

                    mGoogleApiClient = new GoogleApiClient.Builder(SunshineWatchFace.this)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .addApi(Wearable.API)
                            .build();
                    mGoogleApiClient.connect();

                    setWatchFaceStyle(new WatchFaceStyle.Builder(SunshineWatchFace.this)
                        .setHideStatusBar(true)
                        .setCardProgressMode(WatchFaceStyle.PROGRESS_MODE_NONE)
                        .setAcceptsTapEvents(true)
                        .build());

                Resources resources = SunshineWatchFace.this.getResources();

                mBackgroundPaint = new Paint();
                mBackgroundPaint.setColor(resources.getColor(R.color.background));

                mTextPaint = new Paint();
                mTextPaint = createTextPaint(resources.getColor(R.color.digital_text));

                mSubTextPaint = new Paint();
                    mSubTextPaint = createTextPaint(resources.getColor(R.color.sub_text));

                    mSmallTextPaint = new Paint();
                    mSmallTextPaint = createTextPaint(resources.getColor(R.color.sub_text));

                    mCalendar = Calendar.getInstance();
                }

            @Override
            public void onDestroy () {
                mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
                super.onDestroy();
            }

            private Paint createTextPaint(int textColor) {
                Paint paint = new Paint();
                paint.setColor(textColor);
                paint.setTypeface(NORMAL_TYPEFACE);
                paint.setAntiAlias(true);
                return paint;
            }

            @Override
            public void onVisibilityChanged(boolean visible) {
                super.onVisibilityChanged(visible);

                if (visible) {
                    registerReceiver();

                    // Update time zone in case it changed while we weren't visible.
                    mCalendar.setTimeZone(TimeZone.getDefault());
                    invalidate();
                } else {
                    unregisterReceiver();
                }

                // Whether the timer should be running depends on whether we're visible (as well as
                // whether we're in ambient mode), so we may need to start or stop the timer.
                updateTimer();
            }


            private void registerReceiver() {
                if (mRegisteredTimeZoneReceiver) {
                    return;
                }
                mRegisteredTimeZoneReceiver = true;
                IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
                SunshineWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
            }

            private void unregisterReceiver() {
                if (!mRegisteredTimeZoneReceiver) {
                    return;
                }
                mRegisteredTimeZoneReceiver = false;
                SunshineWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
            }

            @Override
            public void onApplyWindowInsets(WindowInsets insets) {
                super.onApplyWindowInsets(insets);

                // Load resources that have alternate values for round watches.
                Resources resources = SunshineWatchFace.this.getResources();
                boolean isRound = insets.isRound();

                mXOffset = resources.getDimension(isRound
                        ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);

                float textSize = resources.getDimension(isRound
                        ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);
                float subSize = resources.getDimension(isRound
                        ? R.dimen.sub_text_size_round : R.dimen.sub_text_size);
                float smallSize = resources.getDimension(isRound
                        ? R.dimen.small_text_size_round : R.dimen.small_text_size);

                mTextPaint.setTextSize(textSize);
                mSubTextPaint.setTextSize(subSize);
                mSmallTextPaint.setTextSize(smallSize);
            }

            @Override
            public void onPropertiesChanged(Bundle properties) {
                super.onPropertiesChanged(properties);
                mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            }

            @Override
            public void onTimeTick() {
                super.onTimeTick();
                invalidate();
            }

            @Override
            public void onAmbientModeChanged(boolean inAmbientMode) {
                super.onAmbientModeChanged(inAmbientMode);
                if (mAmbient != inAmbientMode) {
                    mAmbient = inAmbientMode;
                    if (mLowBitAmbient) {
                        mTextPaint.setAntiAlias(!inAmbientMode);
                    }
                    invalidate();
                }

                // Whether the timer should be running depends on whether we're visible (as well as
                // whether we're in ambient mode), so we may need to start or stop the timer.
                updateTimer();
            }

            /**
             * Captures tap event (and tap type) and toggles the background color if the user finishes
             * a tap.
             */
            @Override
            public void onTapCommand(int tapType, int x, int y, long eventTime) {
                switch (tapType) {
                    case TAP_TYPE_TOUCH:
                        // The user has started touching the screen.
                        break;
                    case TAP_TYPE_TOUCH_CANCEL:
                        // The user has started a different gesture or otherwise cancelled the tap.
                        break;
                    case TAP_TYPE_TAP:
                        // The user has completed the tap gesture.
                        // TODO: Add code to handle the tap gesture.
                        new SendToDataLayerThread(path, message).start();
                        Toast.makeText(getApplicationContext(), R.string.message, Toast.LENGTH_SHORT)
                                .show();
                        break;
                }
                invalidate();
            }

            @Override
            public void onDraw(Canvas canvas, Rect bounds) {

                Log.v(LOG_TAG, "Drawing Canvas");

                mXOffset = bounds.width() / 3;
                mYOffset = bounds.height() / 3;

                float yMargin = bounds.height() / 8;

                // Draw the background.
                if (isInAmbientMode()) {
                    canvas.drawColor(Color.BLACK);
                } else {
                    canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
                }

                // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
                long now = System.currentTimeMillis();
                mCalendar.setTimeInMillis(now);

                String text = mAmbient
                        ? String.format("%02d:%02d", mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE))
                        : String.format("%02d:%02d", mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), mCalendar.get(Calendar.SECOND));

                //Postion text in center
                canvas.drawText(text, bounds.centerX() - (mTextPaint.measureText(text) / 2), mYOffset, mTextPaint);

                Bitmap weatherIcon = BitmapFactory.decodeResource(getResources(), Utility.getIconResourceForWeatherCondition(mWeatherId));
                int scale = 60;
                Bitmap weather = Bitmap.createScaledBitmap(weatherIcon, scale, scale, true);
                if (!mAmbient) {

                    // print the date
                    String dateString = DateFormat.getDateTimeInstance().format(new Date());
                    dateString = dateString.substring(0, 11).toUpperCase();
                    canvas.drawText(dateString, bounds.centerX()- (mSmallTextPaint.measureText(dateString) / 2), mYOffset + yMargin, mSmallTextPaint);

                    // draw a line
                    canvas.drawLine(bounds.centerX()-bounds.width()/4, bounds.centerY(), bounds.centerX() + bounds.width()/4, bounds.centerY(), mSmallTextPaint);

                    // print the temp
                    String temps =  getString(R.string.format_temperature, mHighTemp) + " " + getString(R.string.format_temperature, mLowTemp);
                    canvas.drawText(temps, bounds.centerX()  - (mSubTextPaint.measureText(temps) / 2) , mYOffset + 4*yMargin, mSubTextPaint);

                    // print the image
                    canvas.drawBitmap(weather, bounds.centerX() - weather.getWidth()/2 , mYOffset + yMargin + weather.getHeight()/3, mTextPaint);


                }

            }

            /**
             * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
             * or stops it if it shouldn't be running but currently is.
             */
            private void updateTimer() {
                mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
                if (shouldTimerBeRunning()) {
                    mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
                }
            }

            /**
             * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
             * only run when we're visible and in interactive mode.
             */
            private boolean shouldTimerBeRunning() {
                return isVisible() && !isInAmbientMode();
            }

            /**
             * Handle updating the time periodically in interactive mode.
             */
            private void handleUpdateTimeMessage() {
                invalidate();
                if (shouldTimerBeRunning()) {
                    long timeMs = System.currentTimeMillis();
                    long delayMs = INTERACTIVE_UPDATE_RATE_MS
                            - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                    mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                }
            }


            @Override
            public void onConnected(@Nullable Bundle bundle) {
                Log.d(LOG_TAG, "Google Api client CONNECTED");
                Wearable.DataApi.addListener(mGoogleApiClient, this);

                Log.d(LOG_TAG, "Google Api client CONNECTED");
                new SendToDataLayerThread(path, message).start();
            }

            @Override
            public void onConnectionSuspended(int reason) {
                Log.d(LOG_TAG, "Google Api Client Connection SUSPENDED " + reason);
            }

            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Log.d(LOG_TAG, "Google Api client Connection FAILED " + connectionResult);
            }

            @Override
            public void onDataChanged(DataEventBuffer dataEventBuffer) {

                Log.v(LOG_TAG, "inside onDataChanged ");

                for (DataEvent dataEvent : dataEventBuffer) {

                    if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {

                        DataItem dataItem = dataEvent.getDataItem();

                        Log.d(LOG_TAG, DataMapItem.fromDataItem(dataItem).getDataMap().toString());

                        if (dataItem.getUri().getPath().equals(path)) {

                            DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();
                            mHighTemp = dataMap.getString("high");
                            mLowTemp = dataMap.getString("low");
                            mWeatherId = dataMap.getInt("weatherId");


                            mHighTemp = String.format("%s", String.valueOf(mHighTemp));
                            mLowTemp = String.format("%s", String.valueOf(mLowTemp));

                            Log.d(LOG_TAG, "High: " + mHighTemp + " Low: " + mLowTemp);
                            invalidate();
                        }
                    }
                }

            }

            class SendToDataLayerThread extends Thread{
                String path;
                String message;

                // Constructor to send a message to the data layer
                SendToDataLayerThread(String p, String msg) {
                    path = p;
                    message = msg;
                }

                public void run() {
                    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                    for (Node node : nodes.getNodes()) {
                        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, message.getBytes()).await();
                        if (result.getStatus().isSuccess()) {
                            Log.v("myTag", "Message: {" + message + "} sent to: " + node.getDisplayName());
                        }
                        else {
                            // Log an error
                            Log.v("myTag", "ERROR: failed to send Message");
                        }
                    }
                }
            }

        }


    }

