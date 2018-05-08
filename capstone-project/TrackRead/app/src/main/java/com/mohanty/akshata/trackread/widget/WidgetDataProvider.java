package com.mohanty.akshata.trackread.widget;

/**
 * Created by Akshata on 28/2/2017.
 */

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.mohanty.akshata.trackread.R;
import com.mohanty.akshata.trackread.data.BooksContract;

import org.json.JSONArray;
import org.json.JSONException;


public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    Context mContext = null;
    Cursor c;

    public WidgetDataProvider(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {
        initData();
    }

    @Override
    public void onDestroy() {
        c.close();
    }

    @Override
    public int getCount() {
        return c.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.collection_widget_item);

        if(c.moveToPosition(position)){
            view.setTextViewText(R.id.wid_title,c.getString(c.getColumnIndexOrThrow(BooksContract.BookEntry.COLUMN_NAME_TITLE)));

            String notes = c.getString(c.getColumnIndexOrThrow(BooksContract.BookEntry.COLUMN_NAME_NOTES));
            try {
                JSONArray notesArr = new JSONArray(notes);
                view.setTextViewText(R.id.wid_notes_count, String.valueOf(notesArr.length()));

            } catch (JSONException e) {
                view.setTextViewText(R.id.wid_notes_count, String.valueOf(0));
                e.printStackTrace();
            }
        }

        return view;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private void initData() {

        ContentResolver mResolver = mContext.getContentResolver();
        c = mResolver.query(BooksContract.buildUriForCurrent(),
                null,
                null,
                null,
                null);

    }

}