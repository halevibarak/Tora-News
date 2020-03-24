package com.barak.tabs.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.v4.app.JobIntentService
import android.view.View
import android.widget.RemoteViews
import com.barak.tabs.R
import com.barak.tabs.app.App
import com.barak.tabs.service.Mp3Service
import com.barak.tabs.service.Mp3ServiceImpl
import com.barak.tabs.service.Mp3ServiceImpl.Companion.ACAO_PLAY
import com.barak.tabs.service.Mp3ServiceImpl.Companion.EXTRA_ACAO
import com.barak.tabs.ui.MainActivity

/**
 * Updates the state of the player widget
 */
class PlayerWidgetJobService : JobIntentService() {

    private var playbackService: Mp3Service? = null

    override fun onHandleWork(intent: Intent) {
        if (!PlayerWidget.isEnabled(applicationContext)) {
            return
        }
        if (App.getService() != null) {
            playbackService = App.getService()
        }
        updateViews()
    }

    fun updateViews() {

        val playerWidget = ComponentName(this, PlayerWidget::class.java)
        val manager = AppWidgetManager.getInstance(this)
        val views = RemoteViews(packageName, R.layout.widget_player)
        val itPlay = Intent(this, Mp3ServiceImpl::class.java)
        itPlay.putExtra(EXTRA_ACAO, ACAO_PLAY)
        val pitPlay = PendingIntent.getService(this, 1, itPlay, 0)
        val mainIntent = Intent(App.getInstance(), MainActivity::class.java)
        mainIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pitMain = PendingIntent.getActivity(applicationContext, 4, mainIntent, 0)

        if (playbackService != null) {
            views.setOnClickPendingIntent(R.id.layout_left, pitMain)

            views.setTextViewText(R.id.txtvTitle, playbackService!!.getTitle())
            views.setViewVisibility(R.id.txtvTitle, View.VISIBLE)
            if (playbackService!!.isPlayingNow()) {
                views.setImageViewResource(R.id.butPlay, R.drawable.pause_g)
                views.setContentDescription(R.id.butPlay, getString(R.string.pause_label))
            } else {
                views.setImageViewResource(R.id.butPlay, R.drawable.play_g)
                views.setContentDescription(R.id.butPlay, getString(R.string.play_label))
            }
            views.setOnClickPendingIntent(R.id.butPlay, pitPlay)
        } else {
            if (App.getLastArticle() == null) {
                views.setTextViewText(R.id.txtvTitle,
                        this.getString(R.string.app_name))
            } else {
                views.setTextViewText(R.id.txtvTitle, App.getLastArticle().title)
            }
            views.setOnClickPendingIntent(R.id.layout_left, pitMain)
            views.setOnClickPendingIntent(R.id.butPlay, pitPlay)
            views.setImageViewResource(R.id.butPlay, R.drawable.play_g)
        }
        manager.updateAppWidget(playerWidget, views)
    }

    companion object {
        fun updateWidget(context: Context) {
            JobIntentService.enqueueWork(context, PlayerWidgetJobService::class.java, 0, Intent(context, PlayerWidgetJobService::class.java))
        }
    }

}
