package com.barak.tabs.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.barak.tabs.models.RootObject
import com.barak.tabs.models.Test
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

/**
 * Created by Barak on 1/10/2018.
 */
//private open class Test_(value: Int): Test(value) {
//    override fun foo(): Pair<Int, String> {
//        return Pair(200,"k")
//    }
//}
class ArticleViewModel() :  ViewModel() {
    val namesFlow = flow {
        val names = listOf("Jody", "Steve", "Lance", "Joe")
        for (name in names) {
            delay(2000)
            emit(name)
        }
    }
    val namesFlow1 = flowOf("Jody", "Steve", "Lance", "Joe")


    val queryChannel = BroadcastChannel<String>(Channel.CONFLATED)

    private val flow = queryChannel
            .asFlow()
            .mapLatest {
                getRss(it)
            }
            .catch {
                Log.e("barakk","catch")
            }
    val rss = flow.map {
        for (item in it.items) {
            if (item.enclosure.link.endsWith("mp3")) {
                item.link = item.enclosure.link
            }
        }
        it
    }.asLiveData()
    public inline fun TODO(): Nothing = throw NotImplementedError()

    private suspend fun getRss(url: String): RootObject {


            namesFlow
                    .map { name -> name.length }
                    .filter { length -> length < 5 }
                    .collect { Log.e("barakk","cb $it") }

        val deferred = viewModelScope.async { RepoGetFeeds.getRss(url) }
        return deferred.await()
    }

    companion object {
        @JvmField val NOTIF_ALLOW = "NOTIF_ALLOW"
        @JvmField val START_ALLOW = "START_ALLOW"
    }
}
