package com.barak.tabs.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.barak.tabs.Parser.Article
import com.barak.tabs.Parser.XMLParser
import com.barak.tabs.app.App
import java.util.*

/**
 * Created by Barak on 1/10/2018.
 */

class ArticleModel(application: Application, mParam: String) : AndroidViewModel(application) {

    val NOTIF_ALLOW = "NOTIF_ALLOW"
    private var articleList: JsonLiveData? = null
    private var xmlParser: XMLParser? = null


    private val refresh = MutableLiveData<Int>()


    init {
        if (articleList == null)
            articleList = JsonLiveData(this.getApplication(), mParam)
    }

    fun getArticleList(): MutableLiveData<List<Article>>? {
        return articleList
    }

    fun refreshData(mParam: String) {
        refresh.value = 0
        articleList = JsonLiveData(this.getApplication(), mParam)
    }

    inner class JsonLiveData(context: Context, mParam: String) : MutableLiveData<List<Article>>(), Observer {
        private val mArticles = ArrayList<Article>()


        init {
            LoadData(context, mParam)
        }

        override fun update(o: Observable, data: Any) {
            for (a in data as ArrayList<Article>) {
                mArticles.add(a)
            }
            value = mArticles
            refresh.postValue(1)

        }

        private fun LoadData(context: Context, mParam: String) {
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
            App.getInstance().addToRequestQueue(stringRequest)


        }
    }

    companion object {
        @JvmField val NOTIF_ALLOW = "NOTIF_ALLOW"
    }
}
