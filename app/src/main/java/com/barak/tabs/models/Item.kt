package com.barak.tabs.models

data class Item(var title: String,
                val pubDate: String = "",
                var link: String = "",
                val guid: String = "",
                val author: String = "",
                val thumbnail: String = "",
                val description: String = "",
                val content: String = "",
                val enclosure: Enclosure = Enclosure(""),
                val categories: List<String> = emptyList())

{

    constructor(title: String, link: String) : this( title,"",link,"","","","","",
            Enclosure(""),emptyList())
    constructor() : this( "title","","link","","","","","",
            Enclosure(""),emptyList())

}