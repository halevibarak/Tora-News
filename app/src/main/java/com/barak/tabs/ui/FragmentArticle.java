package com.barak.tabs.ui;


import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.barak.tabs.Parser.Article;
import com.barak.tabs.R;
import com.barak.tabs.adapter.RecyclerViewAdapter;
import com.barak.tabs.app.App;
import com.barak.tabs.app.AppUtility;
import com.barak.tabs.model.MyTab;
import com.barak.tabs.model.MyViewModelFactory;

import java.io.File;
import java.util.ArrayList;

import static com.barak.tabs.app.DownloadToExtStrService.DOWNLOAD_TAB_ACTION;


public class FragmentArticle extends Fragment implements ActionInterface {
    public static final String TAG = "FragmentArticle";
    private static final String LOGTAG = "FragmentArticle";
    private static final String FRAGTYPE = "FRAGTYPE";
    RecyclerView recyclerView;

    private ArrayList<Article> mArticles = new ArrayList<>();
    ;
    private RecyclerViewAdapter adapter;

    private OnCompleteListener mListener;
    private ArticleModel articleModel;
    private View errorTextView;
    private MyTab myTab;
    private SwipeRefreshLayout mySwipeRefreshLayout;
    private long mTimeStamp;

    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.mListener = (OnCompleteListener) context;
        } catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnCompleteListener");
        }
    }

    public static FragmentArticle newInstance(MyTab tab) {
        FragmentArticle fragment = new FragmentArticle();
        Bundle args = new Bundle();
        args.putSerializable(FRAGTYPE, tab);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment, container, false);
        setRetainInstance(true);

        return rootView;


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        errorTextView = view.findViewById(R.id.text_e);
        mySwipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        mySwipeRefreshLayout.setOnRefreshListener(() -> modelConfig()
        );
        myTab = ((MyTab) getArguments().getSerializable(FRAGTYPE));
        boolean showMore = (myTab.getUrl().equals(App.getInstance().getString(R.string.main_url)));
        adapter = new RecyclerViewAdapter(mArticles, showMore, this, myTab.getTabType());

        if (myTab.getTabType() == MyTab.TabType.LOCAL) {
            view.setBackgroundColor(getResources().getColor(R.color.black));
            String[] files = AppUtility.getMainExternalFolder().list();
            if (files != null && files.length > 0) {
                for (String str : files) {
                    mArticles.add(new Article(str));
                }
            }
        } else {
            modelConfig();
        }


        recyclerView = view.findViewById(R.id.recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);


        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        if (myTab.getTabType() == MyTab.TabType.LOCAL) {
            registerReceiver();
        }
    }

    private void modelConfig() {
        if (myTab.getTabType() == MyTab.TabType.LOCAL) {

            String[] files = AppUtility.getMainExternalFolder().list();
            if (files != null && files.length > 0) {
                mArticles.clear();
                for (String str : files) {
                        mArticles.add(new Article(str));
                }

            }
            mySwipeRefreshLayout.setRefreshing(false);
            adapter.notifyDataSetChanged();
        } else {
            mTimeStamp = System.currentTimeMillis();
            mySwipeRefreshLayout.setRefreshing(true);
            articleModel = ViewModelProviders.of(this, new MyViewModelFactory(App.getInstance(), myTab.getUrl())).get(ArticleModel.class);
            articleModel.getArticleList().observe(this, articles -> {
                mySwipeRefreshLayout.setRefreshing(false);
                if (articles == null || articles.size() == 0) {
                    errorTextView.setVisibility(View.VISIBLE);
                    return;
                }
                errorTextView.setVisibility(View.GONE);
                if (mArticles.size() == 0) {
                    mArticles.addAll(articles);
                } else {
                    for (Article ne : articles) {
                        if (!mArticles.contains(ne))
                            mArticles.add(ne);
                    }
                }
                adapter.notifyDataSetChanged();
            });
        }

    }

    @Override
    public void goListen(Article article) {
        if (article.getLink().endsWith("mp3")) {
            article.setTitle(App.convertToUTF8(article.getTitle()));
            mListener.playMp(article);
        }
    }

    @Override
    public void goMore(Article article) {
        mListener.mainMore(article);
    }

    @Override
    public void goDownload(Article article) {
        if (article.getLink().endsWith("mp3")) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle(getString(R.string.download_title)).setMessage(getString(R.string.download_text))
                    .setNeutralButton(getString(R.string.submit), (dialogInterface, d) -> mListener.download(article))
                    .setOnCancelListener(dialogInterface -> {
                    });
            alert.show();
        }
    }

    @Override
    public void goBrowser(Article article) {
        String url = article.getLink();
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            url = "http://" + url;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    @Override
    public void goListenLocal(Article article) {
        if (article.getTitle().endsWith("mp3")) {
            mListener.playLocalMp(article);
        }
    }

    @Override
    public void deleteLocalFile(Article article) {
        if (article.getTitle().endsWith("mp3")) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle(getString(R.string.delete))
                    .setPositiveButton(getString(R.string.submit_delete), (dialogInterface, d) -> {
                        File fileName = new File(AppUtility.getMainExternalFolder().getAbsolutePath() + "/" + article.getTitle());
                        if (fileName.exists()) {
                            fileName.delete();
                            String[] files = AppUtility.getMainExternalFolder().list();
                            mArticles.clear();
                            for (String str : files) {
                                mArticles.add(new Article(str));
                            }

                            Snackbar.make(errorTextView, "קובץ נמחק", Snackbar.LENGTH_LONG).show();
                            if (files.length < 2) {
                                mListener.removeAddDownloadTab();
                            } else {
                                adapter.notifyDataSetChanged();
                            }
                        }
                    })
                    .setNegativeButton(getString(R.string.submit_share), (dialogInterface, d) -> {
                        File fileName = new File(AppUtility.getMainExternalFolder().getAbsolutePath() + "/" + article.getTitle());
                        if (fileName.exists()) {
                            AppUtility.shareDownloadedSong(getContext(), fileName);
                        }
                    });

            alert.show();


        }
    }

    public interface OnCompleteListener {
        void onComplete();

        void onLoading();

        void download(Article article);

        void playMp(Article article);

        void mainMore(Article article);

        void playLocalMp(Article article);

        void removeAddDownloadTab();

    }

    public void updateView() {
        if (adapter != null) {
            if (myTab.getTabType() == MyTab.TabType.LOCAL) {
                String[] files = AppUtility.getMainExternalFolder().list();
                for (String str : files) {
                    mArticles.add(new Article(str));
                }
            } else {
                articleModel.refreshData(myTab.getUrl());
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        recyclerView.setAdapter(null);
        recyclerView = null;
        articleModel = null;
        errorTextView = null;
        if (myTab.getTabType() == MyTab.TabType.LOCAL) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
        }
        myTab = null;
        broadcastReceiver = null;
        super.onDestroyView();
    }

    private void registerReceiver() {

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(getContext());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DOWNLOAD_TAB_ACTION);
        bManager.registerReceiver(broadcastReceiver, intentFilter);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DOWNLOAD_TAB_ACTION)) {
                if (myTab.getTabType() == MyTab.TabType.LOCAL) {
                    String[] files = AppUtility.getMainExternalFolder().list();
                    mArticles.clear();
                    for (String str : files) {
                        mArticles.add(new Article(str));
                    }
                    if (files.length < 2) {
                        mListener.removeAddDownloadTab();
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        long timeStamp = System.currentTimeMillis();
        if (timeStamp > mTimeStamp + 200000) {
            if (myTab == null || articleModel == null) return;
            mTimeStamp = timeStamp;
            modelConfig();
        }

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}