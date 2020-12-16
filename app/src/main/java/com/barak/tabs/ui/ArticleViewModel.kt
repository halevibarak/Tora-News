package com.barak.tabs.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.barak.tabs.models.RootObject
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Created by Barak on 1/10/2018.
 */

class ArticleViewModel() :  ViewModel() {
    val queryChannel = BroadcastChannel<String>(Channel.CONFLATED)

    private val flow = queryChannel
            .asFlow()
            .map {
                getRss(it)
            }
            .catch {

            }
    val rss = flow.map {
        for (item in it.items) {
            if (item.enclosure.link.endsWith("mp3")) {
                item.link = item.enclosure.link
            }
        }
        it
    }.asLiveData()

    private suspend fun getRss(url: String): RootObject {
        val deferred = viewModelScope.async { RepoGetFeeds.getRss(url) }
        return deferred.await()
    }


    companion object {
        @JvmField val NOTIF_ALLOW = "NOTIF_ALLOW"
        @JvmField val START_ALLOW = "START_ALLOW"
    }
}
