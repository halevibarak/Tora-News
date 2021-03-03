package com.barak.tabs.ui

import com.barak.tabs.models.RootObject
import kotlinx.coroutines.*

object RepoGetFeeds {


    suspend fun getRss(rss_Url: String) = withContext(Dispatchers.IO) {
        try {
            RetrofitBuilder.apiService.getItems(rss_Url)
        } catch (error: Throwable) {
            RootObject("", null, emptyList())
        }
    }
}