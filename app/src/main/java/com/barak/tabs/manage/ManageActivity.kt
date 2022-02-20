package com.barak.tabs.manage

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barak.tabs.R
import com.barak.tabs.app.App
import com.barak.tabs.model.MyTab
import com.barak.tabs.notif.AlarmUtils
import com.barak.tabs.notif.BluetoothConnectionReceiver
import com.barak.tabs.notif.BootComplete
import com.barak.tabs.notif.MyBroadcastReceiver
import com.barak.tabs.ui.ArticleModel
import pub.devrel.easypermissions.EasyPermissions
import java.util.*

class ManageActivity : AppCompatActivity() {
    private var mRecyclerView: RecyclerView? = null
    private var mPages: ArrayList<MyTab>? = null
    private var articleAdapter: TabManageAdapter? = null
    private lateinit var chkIos: CheckBox
    private lateinit var chkStart: CheckBox
    private var prefs: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage)
        mRecyclerView = findViewById(R.id.contact_list)
        chkIos = findViewById(R.id.button_notif)
        chkStart = findViewById(R.id.button_automate)
        mPages = App.getTabs()
        if (checkFirstRun()) {
            findViewById<View>(R.id.first).visibility = View.VISIBLE
        }
        mRecyclerView?.layoutManager = LinearLayoutManager(this)
        mRecyclerView?.itemAnimator = DefaultItemAnimator()
        mRecyclerView?.setHasFixedSize(true)
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(mRecyclerView)
        articleAdapter = TabManageAdapter(mPages)
        mRecyclerView?.adapter = articleAdapter
        prefs = getSharedPreferences(ArticleModel.NOTIF_ALLOW, MODE_PRIVATE)
        prefs?.let { prefs->
            val allow = prefs.getBoolean(ArticleModel.NOTIF_ALLOW, false)
            val allowStart = prefs.getBoolean(ArticleModel.START_ALLOW, false)
            if (allow) {
                startAlert()
                chkIos.isChecked = true
            } else {
                chkIos.isChecked = false
            }
            chkIos.setOnClickListener { v: View ->
                val editor = prefs.edit()
                if ((v as CheckBox).isChecked) {
                    editor.putBoolean(ArticleModel.NOTIF_ALLOW, true)
                    startAlert()
                } else {
                    editor.putBoolean(ArticleModel.NOTIF_ALLOW, false)
                    val intent = Intent(this@ManageActivity, MyBroadcastReceiver::class.java)
                    AlarmUtils.cancelAllAlarms(this, intent)
                }
                editor.apply()
            }
            if (allowStart) {
                chkStart.setChecked(true)
            } else {
                chkStart.setChecked(false)
            }
            chkStart.setOnClickListener { v: View ->
                val editor = prefs.edit()
                if ((v as CheckBox).isChecked) {
                    editor.putBoolean(ArticleModel.START_ALLOW, true)
                    BootComplete.startRegisterReceiver(this, BluetoothConnectionReceiver())
                    askSystem()
                } else {
                    editor.putBoolean(ArticleModel.START_ALLOW, false)
                }
                editor.apply()
            }
        }

    }

    private fun askSystem() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!EasyPermissions.hasPermissions(this, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)) {
                EasyPermissions.requestPermissions(this, "נדרש הרשאת בלוטוס", 770,Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT);
            }
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                EasyPermissions.requestPermissions(this, "נדרש הרשאת בלוטוס", 770,Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
    }

    private fun startAlert() {
        val intent = Intent(this, MyBroadcastReceiver::class.java)
        AlarmUtils.cancelAllAlarms(this, intent)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        if (calendar[Calendar.HOUR_OF_DAY] >= NOTIF_HOUR) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        var day: Int
        calendar[Calendar.MINUTE] = NOTIF_MINUT
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.HOUR_OF_DAY] = NOTIF_HOUR
        var id = 0
        for (i in 0..14) {
            day = calendar[Calendar.DAY_OF_WEEK]
            if (day >= 0 && day <= 5) {
                AlarmUtils.addAlarm(this, intent, id, calendar.timeInMillis)
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            id++
        }
    }

    var simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object :
        ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.DOWN or ItemTouchHelper.UP
        ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
            val position = viewHolder.adapterPosition
            if (App.getVisTabs().size == 1 && articleAdapter!!.getItem(position).ismVisibility()) {
                articleAdapter!!.notifyDataSetChanged()
                return
            }
            mPages!!.remove(articleAdapter!!.getItem(position))
            App.setStringArrayPref_(mPages)
            articleAdapter!!.notifyDataSetChanged()
        }
    }

    fun checkFirstRun(): Boolean {
        val isFirstRun =
            getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isManageFirstRun", true)
        if (isFirstRun) {
            getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .edit()
                .putBoolean("isManageFirstRun", false)
                .apply()
        }
        return isFirstRun
    }

    companion object {
        const val NOTIF_HOUR = 17
        const val NOTIF_MINUT = 0
    }

    val BLUETOOTH_PERMISSIONS_S =
        arrayOf<String>(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}