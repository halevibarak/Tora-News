package com.barak.tabs.app;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.barak.tabs.R;
import com.barak.tabs.notif.NotificationHelper;
import com.barak.tabs.ui.MainActivity;
import com.barak.tabs.widget.PlayerWidget;
import com.barak.tabs.widget_list.ListWidget;

/**
 * Created by Barak Halevi on 24/11/2018.
 */
public class DownloadToExtStrService extends IntentService {


    public static final String DOWNLOAD_TAB = "DOWNLOAD_TAB";
    public static final String DOWNLOAD_ERR = "DOWNLOAD_ERR";
    public static final String DOWNLOAD_TAB_ACTION = "DOWNLOAD_TAB_ACTION";
    private String fileName = "";
    private String downLoadUrl = "";
    private int numMessages;

    public DownloadToExtStrService() {
        super("DownloadToExtStrService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        fileName = intent.getStringExtra(AppUtility.DOWNLOAD_SERVICE_FILE_NAME).replace("?","");
        downLoadUrl = intent.getStringExtra(AppUtility.DOWNLOAD_SERVICE_URL).replace(" ","%20");
        boolean success = AppUtility.downloadFile(downLoadUrl, fileName);
        Intent intentLocal = new Intent(DOWNLOAD_TAB_ACTION);
        intentLocal.putExtra(DOWNLOAD_TAB, true);
        if (success) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                addNotification(App.getInstance());
            } else {
                displayNotification(App.getInstance());
            }
        } else {
            intentLocal.putExtra(DOWNLOAD_ERR, true);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentLocal);
        PlayerWidget.Companion.update(App.getInstance());
        ListWidget.update(App.getInstance());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addNotification(Context context) {
        NotificationHelper noti = new NotificationHelper(App.getInstance());
        Notification.Builder nb = null;
        nb = noti.getNotification1(context.getString(R.string.notif_download_end), fileName);
        nb.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic));
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.putExtra(DOWNLOAD_TAB, true);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        nb.setContentIntent(resultPendingIntent);
        noti.notify(1500, nb);
    }

    protected void displayNotification(Context context) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(App.getInstance());
        mBuilder.setContentTitle(context.getString(R.string.notif_download_end));
        mBuilder.setContentText(fileName);
        mBuilder.setSmallIcon(R.drawable.ic_small);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic));
        mBuilder.setNumber(++numMessages);
        Intent resultIntent = new Intent(App.getInstance(), MainActivity.class);
        resultIntent.putExtra(DOWNLOAD_TAB, true);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(App.getInstance());
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1500, mBuilder.build());
    }


}