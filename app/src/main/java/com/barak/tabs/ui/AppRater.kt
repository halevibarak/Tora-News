package com.barak.tabs.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.DisplayMetrics
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.barak.tabs.R

/**
 * Created by Barak Halevi on 30/11/2018.
 */
object AppRater {
    private const val APP_PACKAGE = "com.barak.tabs"// Package Name

    private const val DAYS_UNTIL_PROMPT = 3//Min number of days
    private const val LAUNCHES_UNTIL_PROMPT = 7//Min number of launches
    private const val DONT_SHOW_AGAIN = "DONT_SHOW_AGAIN"//Min number of launches
    private const val DATE_FIRST_LAUNCH = "DATE_FIRST_LAUNCH"//Min number of launches
    private const val LAUNCH_COUNT = "LAUNCH_COUNT"//Min number of launches

    fun app_launched(mContext: Context) {
        val prefs = mContext.getSharedPreferences("apprater", 0)
        if (prefs.getBoolean(DONT_SHOW_AGAIN, false)) {
            return
        }

        val editor = prefs.edit()

        // Increment launch counter
        val launchCount = prefs.getLong(LAUNCH_COUNT, 0) + 1
        editor.putLong(LAUNCH_COUNT, launchCount)
        // Get date of first launch
        var dateFirstLaunch  = prefs.getLong(DATE_FIRST_LAUNCH, 0)
        if (dateFirstLaunch < 1) {
            dateFirstLaunch = System.currentTimeMillis()
            editor.putLong(DATE_FIRST_LAUNCH, dateFirstLaunch)
        }

        // Wait at least n days before opening
        if (launchCount >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= dateFirstLaunch!! + DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000) {
                showRateDialog(mContext, editor)
            }
        }

        editor.apply()
    }

    fun showRateDialog(mContext: Context, editor: SharedPreferences.Editor?) {

        val dialog = Dialog(mContext)
        val ll = LinearLayout(mContext)
        ll.orientation = LinearLayout.VERTICAL

        val tv = TextView(mContext)
        tv.text = mContext.resources.getString(R.string.please_rate)
        tv.width = convertDpToPixel(260f, mContext).toInt()
        tv.setLines(3)
        tv.setPadding(50, 50, 50, 50)
        ll.addView(tv)

        val b1 = Button(mContext)
        b1.text = mContext.resources.getString(R.string.please_rate_b1)
        b1.setBackgroundResource(R.drawable.button)
        b1.setPadding(50, 50, 50, 50)
        b1.setTextColor(mContext.resources.getColor(R.color.white))

        b1.setOnClickListener { v ->
            if (editor != null) {
                editor.putBoolean(DONT_SHOW_AGAIN, true)
                editor.apply()
            }
            mContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$APP_PACKAGE")))
            dialog.dismiss()
        }
        ll.addView(b1)

        val b2 = Button(mContext)
        b2.setPadding(50, 50, 50, 50)
        b2.setBackgroundResource(R.drawable.button)
        b2.setTextColor(mContext.resources.getColor(R.color.white))

        b2.text = mContext.resources.getString(R.string.please_rate_b2)
        b2.setOnClickListener { v -> dialog.dismiss() }
        ll.addView(b2)

        val b3 = Button(mContext)
        b3.text = mContext.resources.getString(R.string.please_rate_b3)
        b3.setTextColor(mContext.resources.getColor(R.color.white))
        b3.setPadding(50, 50, 50, 50)
        b3.setBackgroundResource(R.drawable.button)

        b3.setOnClickListener { v ->
            if (editor != null) {
                editor.putBoolean(DONT_SHOW_AGAIN, true)
                editor.apply()
            }
            dialog.dismiss()
        }
        ll.addView(b3)

        dialog.setContentView(ll)
        dialog.show()
    }

    fun convertDpToPixel(dp: Float, context: Context): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
    }

}