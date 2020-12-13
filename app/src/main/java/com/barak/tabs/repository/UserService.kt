package com.barak.tabs.repository

import android.database.Observable
import android.telecom.Call
import me.toptas.rssconverter.RssFeed
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Created by Barak Halevi on 13/12/2020.
 */
open interface UserService {
    @GET
    suspend fun fatchRss(@Url url: String): RssFeed
}