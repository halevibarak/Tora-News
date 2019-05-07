package com.barak.tabs.service

import com.google.android.exoplayer2.ui.PlayerControlView


interface Mp3Service {
    fun play(arquivo: String, title: String, playerView: PlayerControlView?)
    fun stop()
    fun _isPlayOrPause(): Boolean
    fun stop4Play()
    fun bindPlayerView(playerView: PlayerControlView)
    fun unBindPlayerView(playerView: PlayerControlView)


    fun _getTitle(): String
    fun _isPlayingNow(): Boolean
}
