package com.barak.tabs.service

import android.app.Notification
import android.app.Notification.GROUP_ALERT_SUMMARY
import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.widget.RemoteViews
import com.barak.tabs.Parser.Article
import com.barak.tabs.R
import com.barak.tabs.app.App
import com.barak.tabs.app.AppUtility
import com.barak.tabs.app.DownloadToExtStrService.DOWNLOAD_ERR
import com.barak.tabs.app.DownloadToExtStrService.DOWNLOAD_TAB_ACTION
import com.barak.tabs.notif.NotificationHelper
import com.barak.tabs.notif.NotificationHelper.PRIMARY_CHANNEL
import com.barak.tabs.ui.MainActivity
import com.barak.tabs.widget.PlayerWidget
import com.barak.tabs.widget_list.ListWidget
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.RepeatModeUtil
import java.io.IOException

class Mp3ServiceImpl : Service(), Mp3Service, Player.EventListener, ExtractorMediaSource.EventListener {
    private var mIsPause: Boolean = false
    private var mNumMessages: Int = 0
    private var mIsPlaying: Boolean = false
    private var mTitle: String? = ""
    private var mMainHandler: Handler? = null
    private var mPlayer: SimpleExoPlayer? = null
    private var mPlayerView: PlayerControlView? = null
    private var mUrl: String? = null


    override fun onBind(arg0: Intent): IBinder? {
        Log.e("barak",this.toString())
        return Mp3Binder(this)
    }

    override fun onUnbind(intent: Intent): Boolean {
        return true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent != null) {
            if (intent.extras != null && intent.extras!!.getString("widget_local", "") != "") {
                mTitle = intent.extras!!.getString("widget_local", "")
                if (mTitle == getString(R.string.no_downloads)) {
                    mTitle = ""
                    return Service.START_NOT_STICKY
                }
                mUrl = AppUtility.getMainExternalFolder().absolutePath + "/" + mTitle
                val art = Article(mTitle!! + " ", mUrl!!, "", null)
                App.setLastArticle(art)
                App.setService(this)
                stop4Play()
                play(mUrl!!, mTitle!!, null)
                return Service.START_NOT_STICKY
            }
            if (ACAO_PLAY == intent.getStringExtra(EXTRA_ACAO)) {
                playPause()
            } else if (ACAO_STOP == intent.getStringExtra(EXTRA_ACAO)) {
                stop()
            }

        }
        return START_NOT_STICKY
    }

    private fun playPause() {
        if (mPlayer == null) {
            if (App.getLastArticle() != null) {
                App.setService(this)
                play(App.getLastArticle().link, App.getLastArticle().title, null)
            }

            return
        }
        if (mIsPause) {
            mPlayer!!.playWhenReady = true
            mIsPause = false
        } else {
            mPlayer!!.playWhenReady = false
            mIsPause = true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            showNotificationO(mIsPause)
        } else {
            displayNotification(mIsPause)
        }
        PlayerWidget.update(applicationContext)
        ListWidget.update(applicationContext)
    }

    override fun play(url: String, title: String, playerView_: PlayerControlView?) {

        PlayerWidget.update(applicationContext)
        ListWidget.update(applicationContext)
        if (title != null) mTitle = title
        if (url != null && !mIsPlaying && !mIsPause) {
            try {
                mUrl = url.replace(" ", "%20")
                val dataSourceFactory = DefaultDataSourceFactory(applicationContext, "ExoplayerDemo")
                val extractorsFactory = DefaultExtractorsFactory()
                mMainHandler = Handler()
                val mediaSource = ExtractorMediaSource(Uri.parse(mUrl),
                        dataSourceFactory,
                        extractorsFactory,
                        mMainHandler,
                        this)
                stop4Play()
                if (mPlayer == null) {
                    mPlayer = ExoPlayerFactory.newSimpleInstance(applicationContext)
                }

                mPlayer?.addListener(this)
                mPlayer?.prepare(mediaSource)
                mPlayer?.playWhenReady = true
                mIsPause = false
                mIsPlaying = true
                if (mPlayerView == null && playerView_ != null) {

                    mPlayerView = playerView_
                    mPlayerView?.player = mPlayer
                    mPlayerView?.show()


                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    showNotificationO(false)
                } else {
                    displayNotification(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }

        }
        PlayerWidget.update(applicationContext)

    }

    override fun stop() {
        removeNotification()
        if (mIsPlaying || mIsPause) {
            mIsPause = false
            mPlayer?.release()
            mIsPlaying = false
            mPlayerView = null
            stopForeground(true)
            stopSelf()
        }
        PlayerWidget.update(applicationContext)
    }

    override fun isPlayOrPause(): Boolean {
        return mIsPlaying || mIsPause
    }

    override fun bindPlayerView(playerView: PlayerControlView) {
        if (mPlayer != null) {
            playerView.player = mPlayer
        }
    }

    override fun unBindPlayerView(playerView: PlayerControlView) {
        if (mPlayer != null) {
            playerView.player = null
        }
    }


    override fun stop4Play() {
        if (mIsPlaying || mIsPause) {
            mIsPause = false
            mIsPlaying = false
            mPlayer?.stop()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun showNotificationO(showPlay: Boolean) {
        val itPlayPause = Intent(this, Mp3ServiceImpl::class.java)
        itPlayPause.putExtra(EXTRA_ACAO, ACAO_PLAY)

        val itStop = Intent(this, Mp3ServiceImpl::class.java)
        itStop.putExtra(EXTRA_ACAO, ACAO_STOP)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(Intent(this, MainActivity::class.java))
        val pitPlayPause = PendingIntent.getService(this, 1, itPlayPause, 0)
        val pitStop = PendingIntent.getService(this, 3, itStop, 0)

        val views = RemoteViews(packageName, R.layout.layout_notif)
        views.setOnClickPendingIntent(R.id.play_v, pitPlayPause)
        views.setImageViewResource(R.id.play_v, if (showPlay) R.drawable.play_g else R.drawable.pause_g)
        views.setOnClickPendingIntent(R.id.stop_v, pitStop)
        views.setTextViewText(R.id.text_v, mTitle)
        val noti = NotificationHelper(applicationContext)

        val nb = noti.getNotification1(this.getString(R.string.app_name), "")
        nb!!.setGroupAlertBehavior(GROUP_ALERT_SUMMARY)
                .setGroup(getString(R.string.app_name))
                .setGroupSummary(false)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
        nb.setDefaults(NotificationCompat.FLAG_FOREGROUND_SERVICE)
        nb.setCustomContentView(views)
        val resultIntent = Intent(this, MainActivity::class.java)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        nb.setContentIntent(resultPendingIntent)
        val notification = nb.build()
        startForeground(1100, notification)


    }

    private fun removeNotification() {
        val nm = NotificationManagerCompat.from(this)
        nm.cancel(1100)
    }


    private fun displayNotification( showPlay: Boolean) {
        val mBuilder = NotificationCompat.Builder(applicationContext, PRIMARY_CHANNEL)
        mBuilder.setContentTitle(applicationContext.getString(R.string.app_name))
        mBuilder.setContentText(applicationContext.getString(R.string.notif_text))
        mBuilder.setSmallIcon(R.drawable.ic_small)
        mBuilder.setNumber(++mNumMessages)
        val resultIntent = Intent(applicationContext, MainActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(applicationContext)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(resultIntent)
        val itPlayPause = Intent(this, Mp3ServiceImpl::class.java)
        itPlayPause.putExtra(EXTRA_ACAO, ACAO_PLAY)
        val itStop = Intent(this, Mp3ServiceImpl::class.java)
        itStop.putExtra(EXTRA_ACAO, ACAO_STOP)
        val pitPlayPausa = PendingIntent.getService(this, 1, itPlayPause, 0)
        val pitStop = PendingIntent.getService(this, 3, itStop, 0)
        val views = RemoteViews(packageName, R.layout.layout_notif)
        views.setOnClickPendingIntent(R.id.play_v, pitPlayPausa)
        views.setImageViewResource(R.id.play_v, if (showPlay) R.drawable.play_g else R.drawable.pause_g)

        views.setOnClickPendingIntent(R.id.stop_v, pitStop)
        views.setTextViewText(R.id.text_v, mTitle)
        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder.setContentIntent(resultPendingIntent)
        mBuilder.setCustomContentView(views)
        mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC)
        val n = mBuilder.build()
        n.flags = n.flags or (Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT)
        startForeground(1100, n)

    }

    override fun onDestroy() {
        super.onDestroy()
        mPlayer?.release()
        mMainHandler = null
        mPlayer = null
        mPlayerView = null
App.setService(null)

    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {

    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {

    }

    override fun onLoadingChanged(isLoading: Boolean) {

    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        mIsPause = !playWhenReady
        if (playbackState == 4) {
            stop()
        } else if (playbackState == 3 && playWhenReady && mPlayerView != null) {
            mPlayerView?.setShowMultiWindowTimeBar(true)
            mPlayerView?.showShuffleButton = false
            mPlayerView?.repeatToggleModes = RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE
            Handler().postDelayed({
                mPlayerView?.alpha = 1.0f
                mPlayerView = null

            }, 400)

        }

    }

    override fun onRepeatModeChanged(repeatMode: Int) {

    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {

    }

    override fun onPlayerError(error: ExoPlaybackException?) {

    }

    override fun onPositionDiscontinuity(reason: Int) {

    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {

    }

    override fun onSeekProcessed() {

    }

    override fun onLoadError(error: IOException) {
        stop()
        val intentLocal = Intent(DOWNLOAD_TAB_ACTION)
        intentLocal.putExtra(DOWNLOAD_ERR, true)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentLocal)
    }

    override fun getTitle(): String {
        if (mTitle == null) mTitle = ""
        return mTitle as String
    }

    override fun isPlayingNow(): Boolean {
        return mIsPlaying && !mIsPause
    }


    companion object {
        @JvmField
        val EXTRA_ACAO = "acao"
        @JvmField
        val ACAO_PLAY = "play_pause"
        @JvmField
        val ACAO_STOP = "stop"
    }
}
