package com.barak.tabs.models

import com.atdev.feedsrssreader.pojo.models.Feed

data class RootObject(
        val status: String,
        val feed: Feed,
        val items: List<Item>
)