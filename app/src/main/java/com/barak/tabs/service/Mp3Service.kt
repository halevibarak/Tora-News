package com.barak.tabs.service

import com.barak.tabs.Parser.Article
import com.google.android.exoplayer2.ui.PlayerControlView


interface Mp3Service {
    fun play(arquivo: String, title: String, playerView: PlayerControlView?)
    fun stop()
    fun pause()
    fun isPlayOrPause(): Boolean
    fun stop4Play()
    fun bindPlayerView(playerView: PlayerControlView)
    fun unBindPlayerView(playerView: PlayerControlView)


    fun getTitle(): String
    fun isPlayingNow(): Boolean
    fun play(index:Int, playerView: PlayerControlView)
    fun playIfYouCan()
}
