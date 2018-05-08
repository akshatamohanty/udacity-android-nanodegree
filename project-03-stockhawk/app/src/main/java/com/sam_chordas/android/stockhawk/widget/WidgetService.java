package com.sam_chordas.android.stockhawk.widget;


import android.content.Intent;
import android.widget.RemoteViewsService;


/**
 * Created by Akshata on 7/9/2016.
 */
public class WidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetDataProvider(this, intent);
    }

}
