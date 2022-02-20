package com.barak.tabs.notif

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.barak.tabs.app.Singleton.Companion.getInstance
import com.barak.tabs.ui.MainActivity


/**
 * Created by Barak Halevi on 14/02/2022.
 */
class BluetoothConnectionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("barakk", "onReceive:$action")
        when (action) {
            "android.bluetooth.device.action.ACL_CONNECTED" -> {
                Log.d("barakk", "onReceive:ACL_CONNECTED")
                getInstance().service?.let {
                    it.playIfYouCan()
                    Log.d("barakk", "service playIfYouCan")
                }
            }
        }
        if (action=="android.bluetooth.device.action.ACL_DISCONNECTED"){
            getInstance().service?.let {
                it.pause()
                Log.d("barakk", "service pause")
            }
        }
    }

    private fun startApp(context: Context) {
        val i = Intent(context, MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.putExtra(FROM_BLE, true)
        context.startActivity(i)
        Log.d("barakk", "startActivity")
    }

    companion object {
        const val FROM_BLE = "from_ble"
    }
}