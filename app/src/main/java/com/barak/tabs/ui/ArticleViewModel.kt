package com.barak.tabs.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.barak.tabs.Parser.Article
import com.barak.tabs.Parser.XMLParser
import com.barak.tabs.app.VolleySingleton
import com.barak.tabs.repository.RssRepository
import kotlinx.coroutines.launch
import java.util.*

/**
 * Created by Barak on 1/10/2018.
 */

class ArticleViewModel() :  ViewModel() {

    private var rssRepository: RssRepository = RssRepository()
    var articleList: JsonLiveData
    private var xmlParser: XMLParser? = null
    private var mTmeStamp: Long = 0

    private val refresh = MutableLiveData<Int>()


    init {
        articleList = JsonLiveData( "")
    }

    fun fetchDetails(url: String) {
        viewModelScope.launch {
            articleList.LoadData(url)
            rssRepository.fetchResponse(url)
        }

    }


    inner class JsonLiveData(mParam: String) : MutableLiveData<List<Article>>(), Observer {
        private val mArticles = ArrayList<Article>()


        init {
            LoadData(mParam)
        }

        override fun update(o: Observable, data: Any) {
            mArticles.clear()
            for (a in data as ArrayList<Article>) {
                mArticles.add(a)
            }
            value = mArticles
            refresh.postValue(1)

        }

        fun LoadData( mParam: String) {
            val stringRequest = StringRequest(Request.Method.POST, mParam,
                    { response ->
                        xmlParser = XMLParser(mParam.contains("meir"))
                        xmlParser!!.addObserver(this@JsonLiveData)
                        try {
                            xmlParser!!.parseXML(response)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, { error ->
                value = null
                refresh.postValue(1)
            })
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    3000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance().addToRequestQueue(stringRequest)


        }
    }

    companion object {
        @JvmField val NOTIF_ALLOW = "NOTIF_ALLOW"
        @JvmField val START_ALLOW = "START_ALLOW"
    }
}
