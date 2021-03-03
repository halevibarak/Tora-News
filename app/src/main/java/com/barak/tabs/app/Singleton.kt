package com.barak.tabs.app


import com.barak.tabs.models.Item
import com.barak.tabs.service.Mp3Service

/**
 * Created by Barak Halevi on 2020-03-24.
 */
object Singleton {

    var playList: List<Item>?= null
    var service: Mp3Service? = null
     var lastArticle: Item? = null
}
