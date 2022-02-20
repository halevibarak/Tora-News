/* Copyright (C) 2016-2019 Julian Andres Klode <jak@jak-linux.org>
 *
 * Derived from AdBuster:
 * Copyright (C) 2016 Daniel Brodie <dbrodie@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Contributions shall also be provided under any later versions of the
 * GPL.
 */
package com.barak.tabs.notif;

import static android.content.Context.MODE_PRIVATE;
import static com.barak.tabs.ui.ArticleModel.NOTIF_ALLOW;
import static com.barak.tabs.ui.ArticleModel.START_ALLOW;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;

public class BootComplete extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("barakk", "BootComplete");
        SharedPreferences prefs = context.getSharedPreferences(NOTIF_ALLOW, MODE_PRIVATE);
        boolean allow = prefs.getBoolean(START_ALLOW, false);
        if (allow){
            startRegisterReceiver(context, new BluetoothConnectionReceiver());
        }

    }

    public static void startRegisterReceiver(Context context, BluetoothConnectionReceiver receiver) {
        Log.d("barakk", "registerReceiver");
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter1.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter1.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        context.registerReceiver(receiver, filter1);
    }


}
