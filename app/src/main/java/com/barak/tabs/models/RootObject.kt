package com.atdev.feedsrssreader.pojo.models

import com.barak.tabs.models.Item

data class RootObject(
    val status: String,
    val feed: Feed,
    val items: List<Item>
)