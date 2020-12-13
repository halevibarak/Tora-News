package com.barak.tabs.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.DisplayMetrics
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
        val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
        builder.setMessage(mContext.resources.getString(R.string.please_rate))
                .setCancelable(false)
                .setPositiveButton(mContext.resources.getString(R.string.please_rate_b1)) { dialog, _ -> rate1(dialog,mContext, editor) }
                .setNegativeButton(mContext.resources.getString(R.string.please_rate_b2)) { dialog, _ -> dialog.dismiss() }
                .setNeutralButton(mContext.resources.getString(R.string.please_rate_b3)) { dialog, _ -> dontRate(dialog,mContext, editor) }
        builder.create().show()
    }

    private fun dontRate(dialog: DialogInterface?, mContext: Context, editor: SharedPreferences.Editor?) {
        if (editor != null) {
            editor.putBoolean(DONT_SHOW_AGAIN, true)
            editor.apply()
        }
        dialog?.dismiss()
    }

    private fun rate1(dialog: DialogInterface?, mContext: Context, editor: SharedPreferences.Editor?) {
        if (editor != null) {
            editor.putBoolean(DONT_SHOW_AGAIN, true)
            editor.apply()
        }
        try {
            mContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$APP_PACKAGE")))
        } catch (e: Exception) {
        }
        dialog?.dismiss()

    }



    fun convertDpToPixel(dp: Float, context: Context): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
    }

}