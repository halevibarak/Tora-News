package com.barak.tabs.widget_list;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.barak.tabs.R;
import com.barak.tabs.app.AppUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * ListWidgetDataProvider acts as the adapter for the collection view widget,
 * providing RemoteViews to the widget in the getViewAt method.
 */
public class ListWidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = "ListWidgetDataProvider";

    List<String> mCollection = new ArrayList<>();
    String pachageName = "";

    public ListWidgetDataProvider(Context context, Intent intent) {
        pachageName = context.getPackageName();
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
        mCollection = null;
        pachageName = null;
    }

    @Override
    public int getCount() {
        return mCollection.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews view = new RemoteViews(pachageName, R.layout.local_list_item);
        view.setTextViewText(R.id.local_title_item, mCollection.get(position).replace(".mp3", ""));
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra("widget_local", mCollection.get(position));
        view.setOnClickFillInIntent(R.id.local_title_item, fillInIntent);
        view.setOnClickFillInIntent(R.id.local_imgplay, fillInIntent);
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
        mCollection.clear();
        String[] files = AppUtility.getMainExternalFolder().list();
        if (files != null && files.length > 0) {
            for (String str : files) {
                mCollection.add(str);
            }
        } else {
            mCollection.add("אין הורדות");
        }

    }

}
