package com.barak.tabs.ui

import androidx.lifecycle.LiveData
import com.barak.tabs.models.RootObject
import kotlinx.coroutines.*

object RepoGetFeeds {

    var job: CompletableJob? = null

    fun getFeeds(rss_Url: String): LiveData<RootObject> {
        job = Job()
        return object : LiveData<RootObject>() {
            override fun onActive() {
                super.onActive()
                job?.let { thejob ->
                    CoroutineScope(Dispatchers.IO + thejob).launch {
                        val rssObject = RetrofitBuilder.apiService.getItems(rss_Url)
                        withContext(Dispatchers.Main) {
                            value = rssObject
                            thejob.complete()
                        }
                    }
                }
            }
        }
    }

    fun cancelJob() {
        job!!.cancel()
    }
}