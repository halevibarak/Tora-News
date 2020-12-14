package com.barak.tabs.app


import com.barak.tabs.models.Item
import com.barak.tabs.service.Mp3Service

/**
 * Created by Barak Halevi on 2020-03-24.
 */
class Singleton {
    companion object {
        @Volatile
        private var INSTANCE: Singleton? = null
        fun getInstance() =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: Singleton().also {
                        INSTANCE = it
                    }
                }
    }

    var playList: List<Item>?= null
    var service: Mp3Service? = null
     var lastArticle: Item? = null
}
