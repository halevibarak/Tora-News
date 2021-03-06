package com.barak.tabs.Parser

import java.util.*

/**
 * Created by Barak Halevi on 27/11/2018.
 */
data class Article(
        var title: String,
        var link: String,
        var description: String,
        val pubDate: Date?) {
    constructor(title: String) : this(title, "",
            "", null
    )

    constructor() : this("", "",
            "", null
    )

    override fun equals(other: Any?): Boolean {
        if(other == null || other !is Article)
            return false
        return link == other.link
        return false;
    }
}
