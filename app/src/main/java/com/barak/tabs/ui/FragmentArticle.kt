package com.barak.tabs.ui

import android.app.AlertDialog
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.barak.tabs.Parser.Article
import com.barak.tabs.R
import com.barak.tabs.adapter.RecyclerViewAdapter
import com.barak.tabs.app.App
import com.barak.tabs.app.AppUtility
import com.barak.tabs.app.DownloadToExtStrService
import com.barak.tabs.app.Singleton.Companion.getInstance
import com.barak.tabs.model.MyTab
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.io.File
import java.util.*

@FlowPreview
@ExperimentalCoroutinesApi
class FragmentArticle : Fragment(), ActionInterface {
    private val mArticles = ArrayList<Article>()
    private var adapter: RecyclerViewAdapter? = null
    private var mListener: OnCompleteListener? = null
    private val articleViewModel: ArticleViewModel by viewModels()
    private var myTab: MyTab? = null
    private var mTimeStamp: Long = 0
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener = context as OnCompleteListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnCompleteListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(
                R.layout.fragment, container, false)
        retainInstance = true
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swiperefresh.setOnRefreshListener(OnRefreshListener { modelConfig() }
        )
        myTab = requireArguments().getSerializable(FRAGTYPE) as MyTab
        val showMore = myTab!!.url == requireContext().getString(R.string.main_url)
        adapter = RecyclerViewAdapter(mArticles, showMore, this, myTab!!.tabType)
        if (myTab!!.tabType == MyTab.TabType.LOCAL) {
            view.setBackgroundColor(resources.getColor(R.color.black))
            val files = AppUtility.getMainExternalFolder().list()
            if (files != null && files.size > 0) {
                for (str in files) {
                    mArticles.add(Article(str!!))
                }
            }
        } else {
            modelConfig()
        }
        val layoutManager = LinearLayoutManager(context)
        val dividerItemDecoration = DividerItemDecoration(recyclerView.getContext(),
                layoutManager.orientation)
        dividerItemDecoration.setDrawable(requireContext().resources.getDrawable(R.drawable.sk_line_divider))
        recyclerView.addItemDecoration(dividerItemDecoration)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        if (myTab!!.tabType == MyTab.TabType.LOCAL) {
            registerReceiver()
        }
    }

    private fun modelConfig() {
        if (myTab!!.tabType == MyTab.TabType.LOCAL) {
            val files = AppUtility.getMainExternalFolder().list()
            if (files != null && files.size > 0) {
                mArticles.clear()
                for (str in files) {
                    mArticles.add(Article(str))
                }
            }
            swiperefresh.isRefreshing = false
            adapter!!.notifyDataSetChanged()
        } else {
            mTimeStamp = System.currentTimeMillis()
            swiperefresh.isRefreshing = true
            if (mArticles.size == 0) {
                articleViewModel.articleList.observe(viewLifecycleOwner, Observer observe@{ articles: List<Article>? ->
                    updateView(articles)
                })
                articleViewModel.fetchDetails(myTab!!.url)
            }
            swiperefresh.setOnRefreshListener {
                articleViewModel.fetchDetails(myTab!!.url)
            }
        }
    }

    private fun updateView(articles: List<Article>?) {
        swiperefresh.isRefreshing = false
        if (articles == null || articles.size == 0) {
            errorTextView!!.visibility = View.VISIBLE
            return
        }
        if (getInstance().playList == null && articles[0].link.endsWith("mp3")) {
            val prefs = requireContext().getSharedPreferences(ArticleViewModel.NOTIF_ALLOW, Context.MODE_PRIVATE)
            val articlesList = articles
            getInstance().playList = articlesList
            if (prefs.getBoolean(ArticleViewModel.START_ALLOW, false)) {
                goListen(articlesList)
            }
        }
        errorTextView.visibility = View.GONE
        if (mArticles.size == 0) {
            mArticles.addAll(articles)
        } else {
            for (ne in articles) {
                if (!mArticles.contains(ne)) mArticles.add(0, ne)
            }
        }
        adapter!!.notifyDataSetChanged()
    }

    private fun goListen(articles: List<Article>) {
        if (articles[0].link.endsWith("mp3")) {
            articles[0].title = App.convertToUTF8(articles[0].title)
            mListener!!.playMp(articles)
        }
    }

    override fun goListen(article: Article) {
        if (article.link.endsWith("mp3")) {
            article.title = App.convertToUTF8(article.title)
            mListener!!.playMp(article)
        }
    }

    override fun goMore(article: Article) {
        mListener!!.mainMore(article)
    }

    override fun goDownload(article: Article) {
        if (article.link.endsWith("mp3")) {
            val alert = AlertDialog.Builder(context)
            alert.setTitle(getString(R.string.download_title)).setMessage(getString(R.string.download_text))
                    .setNeutralButton(getString(R.string.submit)) { dialogInterface: DialogInterface?, d: Int -> mListener!!.download(article) }
                    .setOnCancelListener { dialogInterface: DialogInterface? -> }
            alert.show()
        }
    }

    override fun goBrowser(article: Article) {
        var url = article.link
        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://$url"
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    override fun goListenLocal(article: Article) {
        if (article.title.endsWith("mp3")) {
            mListener!!.playLocalMp(article)
        }
    }

    override fun deleteLocalFile(article: Article) {
        if (article.title.endsWith("mp3")) {
            val alert = AlertDialog.Builder(context)
            alert.setTitle(getString(R.string.delete))
                    .setPositiveButton(getString(R.string.submit_delete)) { dialogInterface: DialogInterface?, d: Int ->
                        val fileName = File(AppUtility.getMainExternalFolder().absolutePath + "/" + article.title)
                        if (fileName.exists()) {
                            fileName.delete()
                            val files = AppUtility.getMainExternalFolder().list()
                            mArticles.clear()
                            for (str in files) {
                                mArticles.add(Article(str!!))
                            }
                            Snackbar.make(errorTextView, "קובץ נמחק", Snackbar.LENGTH_LONG).show()
                            if (files.size < 2) {
                                mListener!!.removeAddDownloadTab()
                            } else {
                                adapter!!.notifyDataSetChanged()
                            }
                        }
                    }
                    .setNegativeButton(getString(R.string.submit_share)) { dialogInterface: DialogInterface?, d: Int ->
                        val fileName = File(AppUtility.getMainExternalFolder().absolutePath + "/" + article.title)
                        if (fileName.exists()) {
                            AppUtility.shareDownloadedSong(context, fileName)
                        }
                    }
            alert.show()
        }
    }

    interface OnCompleteListener {
        fun onComplete()
        fun onLoading()
        fun download(article: Article?)
        fun playMp(article: Article?)
        fun playMp(articles: List<Article>?)
        fun mainMore(article: Article?)
        fun playLocalMp(article: Article?)
        fun removeAddDownloadTab()
    }

    override fun onDestroyView() {
        recyclerView.adapter = null
        if (myTab!!.tabType == MyTab.TabType.LOCAL) {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver!!)
        }
        myTab = null
        broadcastReceiver = null
        super.onDestroyView()
    }

    private fun registerReceiver() {
        val bManager = LocalBroadcastManager.getInstance(requireContext())
        val intentFilter = IntentFilter()
        intentFilter.addAction(DownloadToExtStrService.DOWNLOAD_TAB_ACTION)
        bManager.registerReceiver(broadcastReceiver!!, intentFilter)
    }

    private var broadcastReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == DownloadToExtStrService.DOWNLOAD_TAB_ACTION) {
                if (myTab!!.tabType == MyTab.TabType.LOCAL) {
                    val files = AppUtility.getMainExternalFolder().list()
                    mArticles.clear()
                    for (str in files) {
                        mArticles.add(Article(str!!))
                    }
                    if (files.size < 2) {
                        mListener!!.removeAddDownloadTab()
                    }
                    adapter!!.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val timeStamp = System.currentTimeMillis()
        if (timeStamp > mTimeStamp + 200000) {
            if (myTab == null || articleViewModel == null) return
            mTimeStamp = timeStamp
            modelConfig()
        }
    }


    companion object {
        const val TAG = "FragmentArticle"
        private const val LOGTAG = "FragmentArticle"
        private const val FRAGTYPE = "FRAGTYPE"
        @JvmStatic
        fun newInstance(tab: MyTab?): FragmentArticle {
            val fragment = FragmentArticle()
            val args = Bundle()
            args.putSerializable(FRAGTYPE, tab)
            fragment.arguments = args
            return fragment
        }
    }
}