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

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import com.barak.tabs.app.Singleton;
import com.barak.tabs.ui.MainActivity;

import static android.content.Context.MODE_PRIVATE;
import static com.barak.tabs.ui.ArticleViewModel.NOTIF_ALLOW;

public class BootComplete extends BroadcastReceiver {

    BroadcastService mSMSreceiver = new BroadcastService();

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(NOTIF_ALLOW, MODE_PRIVATE);
        boolean allow = prefs.getBoolean(NOTIF_ALLOW, false);
        if (allow){
            mSMSreceiver = new BroadcastService();
            checkStartVpnOnBoot(context,mSMSreceiver);
        }

    }

    public static void checkStartVpnOnBoot(Context context, BroadcastService mSMSreceiver) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        context.getApplicationContext().registerReceiver(mSMSreceiver, filter);
    }

    public static class BroadcastService extends BroadcastReceiver {

        public static final String FROM_BLE = "from_ble";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                SharedPreferences prefs = context.getSharedPreferences(NOTIF_ALLOW, MODE_PRIVATE);
                boolean allow = prefs.getBoolean(NOTIF_ALLOW, false);
                if (allow){
                    Intent i = new Intent(context, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra(FROM_BLE, true);
                    context.startActivity(i);
                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                if (Singleton.INSTANCE.getService() != null) {
                    Singleton.INSTANCE.getService().stop();
                }
            }
        }
    }
}
