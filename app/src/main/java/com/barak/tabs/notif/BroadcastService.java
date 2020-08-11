package com.barak.tabs.notif;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.barak.tabs.app.Singleton;
import com.barak.tabs.ui.MainActivity;

/**
 * Created by Barak Halevi on 11/08/2020.
 */

public class BroadcastService extends Service {


    public static final String FROM_BLE = "from_ble";
    private BroadcastReceiver receiver;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        registerReceiver();
    }

    @Override
    public void onDestroy() {

    }

    public static void checkStartVpnOnBoot(Context context) {
        Intent intent_ = new Intent(context, BroadcastService.class);
        context.startService(intent_);
        return;
    }


    private void registerReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                    Singleton.Companion.getInstance().setPlayList(null);
                    Intent dialogIntent = new Intent(getApplicationContext(), MainActivity.class);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    dialogIntent.putExtra(FROM_BLE, true);
                    startActivity(dialogIntent);
                    Toast.makeText(getApplicationContext(), "startActivity", Toast.LENGTH_LONG).show();

                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    if (Singleton.Companion.getInstance().getService() != null) {
                        Singleton.Companion.getInstance().getService().stop();
                    }
                }
            }

        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(receiver, filter);


    }
}