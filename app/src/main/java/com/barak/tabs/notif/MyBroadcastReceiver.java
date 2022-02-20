package com.barak.tabs.notif;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.barak.tabs.R;
import com.barak.tabs.ui.MainActivity;


/**
 * Created by Barak Halevi on 22/10/2018.
 */
public class MyBroadcastReceiver extends BroadcastReceiver {
    private int numMessages;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            addNotification(context);
        } else {
            displayNotification(context);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addNotification(Context context) {
        NotificationHelper noti = new NotificationHelper(context);
        Notification.Builder nb = null;
        nb = noti.getNotification1(context.getString(R.string.notif_text), context.getString(R.string.notif_text_toch));
        nb.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.ic));
        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE);
        nb.setContentIntent(resultPendingIntent);
        noti.notify(1200, nb);
    }

    protected void displayNotification(Context context) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle(context.getString(R.string.notif_text));
        mBuilder.setContentText(context.getString(R.string.notif_text_toch));
        mBuilder.setSmallIcon(R.drawable.ic_small);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.ic));
        mBuilder.setNumber(++numMessages);
        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1200, mBuilder.build());
    }
}
