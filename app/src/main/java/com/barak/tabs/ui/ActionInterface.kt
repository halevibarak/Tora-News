package com.barak.tabs.ui

import com.barak.tabs.models.Item

/**
 * Created by Barak on 24/08/2017.
 */

interface ActionInterface {
    fun goListen(article: Item)

    fun goMore(article: Item)

    fun goDownload(article: Item)

    fun goBrowser(article: Item)

    fun goListenLocal(article: Item)

    fun deleteLocalFile(article: Item)
}
