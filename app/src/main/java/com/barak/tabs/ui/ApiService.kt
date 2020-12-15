package com.barak.tabs.ui

import com.barak.tabs.models.RootObject
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("api.json")
    suspend fun getItems(@Query("rss_url") rssUrl: String): RootObject
}