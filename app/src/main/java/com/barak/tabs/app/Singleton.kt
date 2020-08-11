package com.barak.tabs.app


import com.barak.tabs.Parser.Article
import com.barak.tabs.service.Mp3Service
import java.util.ArrayList

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

    var playList: List<Article>?= null
    var service: Mp3Service? = null
     var lastArticle: Article? = null
}
