package com.barak.tabs.notif;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;
import static com.barak.tabs.ui.ArticleModel.NOTIF_ALLOW;

/**
 * Created by Barak Halevi on 11/08/2020.
 */

class BootrBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(NOTIF_ALLOW, MODE_PRIVATE);
            Toast.makeText(context," Boot",Toast.LENGTH_LONG).show();
            Intent intent_ = new Intent(context, BroadcastService.class);
            context.startService(intent_);
    }
}