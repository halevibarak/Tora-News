package com.barak.tabs.ui

import com.barak.tabs.Parser.Article

/**
 * Created by Barak on 24/08/2017.
 */

interface ActionInterface {
    fun goListen(article: Article)

    fun goMore(article: Article)

    fun goDownload(article: Article)

    fun goBrowser(article: Article)

    fun goListenLocal(article: Article)

    fun deleteLocalFile(article: Article)
}
