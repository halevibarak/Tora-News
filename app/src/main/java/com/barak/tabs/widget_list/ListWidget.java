package com.barak.tabs.widget_list;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import android.widget.RemoteViews;

import com.barak.tabs.R;
import com.barak.tabs.app.App;
import com.barak.tabs.service.Mp3ServiceImpl;
import com.barak.tabs.ui.MainActivity;

import static com.barak.tabs.service.Mp3ServiceImpl.ACAO_PLAY;
import static com.barak.tabs.service.Mp3ServiceImpl.EXTRA_ACAO;

/**
 * Created by Barak Halevi on 05/12/2018.
 */
public class ListWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        update(App.getInstance());
//        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_list);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//            setRemoteAdapter(context, views);
//        } else {
//            setRemoteAdapterV11(context, views);
//        }
//        Intent mainIntent = new Intent(App.getInstance(), MainActivity.class);
//        mainIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent pitMain = PendingIntent.getActivity(context, 4, mainIntent, 0);
//        views.setOnClickPendingIntent(R.id.widget_layout_main, pitMain);
//
//        Intent templateIntent = new Intent(context, Mp3ServiceImpl.class);
//        templateIntent.putExtra(EXTRA_ACAO, ACAO_PLAY);
//        templateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
//        PendingIntent templatePendingIntent =PendingIntent.getService(context, 1, templateIntent, 0);
//        views.setPendingIntentTemplate(R.id.widget_list,
//                templatePendingIntent);
//        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


    public static void update(Context context) {
        if (context==null)return;
        ComponentName playerWidget = new ComponentName(context, ListWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(App.getInstance());
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_list);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setRemoteAdapter(context, views);
        } else {
            setRemoteAdapterV11(context, views);
        }
        Intent mainIntent = new Intent(App.getInstance(), MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pitMain = PendingIntent.getActivity(context, 4, mainIntent, 0);
        views.setOnClickPendingIntent(R.id.widget_layout_main, pitMain);

        Intent templateIntent = new Intent(context, Mp3ServiceImpl.class);
        templateIntent.putExtra(EXTRA_ACAO, ACAO_PLAY);
        PendingIntent templatePendingIntent =PendingIntent.getService(context, 1, templateIntent, 0);
        views.setPendingIntentTemplate(R.id.widget_list,
                templatePendingIntent);
        int appWidgetIds[] = manager.getAppWidgetIds(
                new ComponentName(context, ListWidget.class));
        manager.notifyAppWidgetViewDataChanged(appWidgetIds,R.id.widget_list);
        manager.updateAppWidget(playerWidget, views);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(R.id.widget_list,
                new Intent(context, ListWidgetService.class));
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @SuppressWarnings("deprecation")
    private static void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(0, R.id.widget_list,
                new Intent(context, ListWidgetService.class));
    }

}
