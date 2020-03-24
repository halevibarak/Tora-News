package com.barak.tabs.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import com.barak.tabs.app.App

/**
 * Created by Barak Halevi on 03/12/2018.
 */
class PlayerWidget : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        PlayerWidgetJobService.updateWidget(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        setEnabled(context, true)

    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        PlayerWidgetJobService.updateWidget(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        setEnabled(context, false)
    }

    private fun setEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    companion object {

        private val PREFS_NAME = "PlayerWidgetPrefs"
        private val KEY_ENABLED = "WidgetEnabled"

        fun update(instance: Context) {
            PlayerWidgetJobService.updateWidget(instance)
        }

        fun isEnabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_ENABLED, false)
        }
    }
}
