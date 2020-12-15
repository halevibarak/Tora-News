package com.barak.tabs.app

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

/**
 * Created by Barak Halevi on 2020-03-24.
 */
class VolleySingleton {
    companion object {
        @Volatile
        private var INSTANCE: VolleySingleton? = null
        fun getInstance() =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: VolleySingleton().also {
                        INSTANCE = it
                    }
                }
    }


    val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(App.getInstance())
    }

    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }
}
