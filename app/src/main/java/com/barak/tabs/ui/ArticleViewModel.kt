package com.barak.tabs.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.barak.tabs.models.RootObject

/**
 * Created by Barak on 1/10/2018.
 */

class ArticleViewModel() :  ViewModel() {
    private var _rssUrl: MutableLiveData<String> = MutableLiveData()

    val rootObject: LiveData<RootObject> = Transformations
            .switchMap(_rssUrl) {
                url ->
                RepoGetFeeds.getFeeds(url)
            }

    fun setRssUrl(rssUrl: String) {
//        if (_rssUrl.value == rssUrl) {
//            return
//        }
        _rssUrl.value = rssUrl
    }

    fun cancelJob() {
//        RepoGetFeeds.cancelJob()
    }
    companion object {
        @JvmField val NOTIF_ALLOW = "NOTIF_ALLOW"
        @JvmField val START_ALLOW = "START_ALLOW"
    }
}
