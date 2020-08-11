package com.barak.tabs.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.barak.tabs.Parser.Article
import com.barak.tabs.Parser.XMLParser
import com.barak.tabs.app.VolleySingleton
import java.util.*

/**
 * Created by Barak on 1/10/2018.
 */

class ArticleModel(application: Application, mParam: String) : AndroidViewModel(application) {

    var articleList: JsonLiveData
    private var xmlParser: XMLParser? = null
    private var mTmeStamp: Long = 0

    private val refresh = MutableLiveData<Int>()


    init {
        articleList = JsonLiveData(this.getApplication(), mParam)
    }

    fun refreshData(mParam: String) {
        if (System.currentTimeMillis() > mTmeStamp + 5000) {
            articleList.LoadData(this.getApplication(), mParam)
        }
    }
    fun refreshData_(mParam: String) {
        refresh.value = 0
        articleList = JsonLiveData(this.getApplication(), mParam)
    }

    inner class JsonLiveData(context: Context, mParam: String) : MutableLiveData<List<Article>>(), Observer {
        private val mArticles = ArrayList<Article>()


        init {
            LoadData(context, mParam)
        }

        override fun update(o: Observable, data: Any) {
            mArticles.clear()
            for (a in data as ArrayList<Article>) {
                mArticles.add(a)
            }
            value = mArticles
            refresh.postValue(1)

        }

        fun LoadData(context: Context, mParam: String) {
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
            VolleySingleton.getInstance(context).addToRequestQueue(stringRequest)


        }
    }

    companion object {
        @JvmField val NOTIF_ALLOW = "NOTIF_ALLOW"
        @JvmField val START_ALLOW = "START_ALLOW"
    }
}
