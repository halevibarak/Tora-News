package com.barak.tabs.adapter;

import android.os.SystemClock;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.barak.tabs.R;
import com.barak.tabs.app.App;
import com.barak.tabs.model.MyTab;
import com.barak.tabs.models.Item;
import com.barak.tabs.ui.ActionInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Barak Halevi on 22/10/2018.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final MyTab.TabType tabType;
    List<Item> articles = new ArrayList<>();
    private boolean mShowMore;
    private ActionInterface mLisenner;
    private long mLastClickTime;

    public RecyclerViewAdapter(ArrayList<Item> items, boolean showMore, ActionInterface listnner, MyTab.TabType tabType_) {
        this.articles = items;
        this.mLisenner = listnner;
        this.tabType = tabType_;
        mShowMore = showMore;

    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup view, int viewType) {
        switch (MyTab.TabType.fromInt(viewType)) {
            case MEIR:
                View view_meir = LayoutInflater.from(view.getContext()).inflate(R.layout.recycler_view_list_item, view, false);
                return new LessonViewHolder(view_meir);
            case LOCAL:
                View view_local = LayoutInflater.from(view.getContext()).inflate(R.layout.local_list_item, view, false);
                return new LocalViewHolder(view_local);
            case REST:
                View view_rest = LayoutInflater.from(view.getContext()).inflate(R.layout.recycler_view_list_item, view, false);
                return new NewsViewHolder(view_rest);
        }
        return new LocalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder view_holder, int position) {

        final Item article = articles.get(position);
        switch (view_holder.getItemViewType()) {
            case 0:
                LessonViewHolder lessonViewHolder = (LessonViewHolder) view_holder;
                if (article != null) {
                    lessonViewHolder.titleView.setText(App.convertToUTF8(article.getTitle()));
                    lessonViewHolder.descView.setText(App.convertToUTF8(article.getDescription()));
                    lessonViewHolder.playView.setVisibility(View.VISIBLE);
                    if (!mShowMore) {
                        lessonViewHolder.moreButton.setVisibility(View.GONE);
                    } else {
                        lessonViewHolder.moreButton.setVisibility(View.VISIBLE);
                    }

                    lessonViewHolder.moreButton.setText(lessonViewHolder.moreButton.getContext().getString(R.string.more_text));
                    if (article.getLink().equals(lessonViewHolder.moreButton.getContext().getString(R.string.donate_url))) {
                        lessonViewHolder.moreButton.setText(lessonViewHolder.moreButton.getContext().getString(R.string.donate));
                        lessonViewHolder.moreButton.setVisibility(View.VISIBLE);
                        lessonViewHolder.playView.setVisibility(View.GONE);
                        lessonViewHolder.moreButton.setVisibility(View.GONE);
//                        lessonViewHolder.moreButton.setOnClickListener(v -> {
//                            String url = "http://www.meirtv.co.il/Donate/Home/Donate?source=mobile3";
//                            Intent i = new Intent(Intent.ACTION_VIEW);
//                            i.setData(Uri.parse(url));
//                            try {
//                                v.getContext().startActivity(i);
//                            } catch ( Exception e) { }
//
//
//                        });

                    } else {
                        lessonViewHolder.moreButton.setOnClickListener(v -> {
                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();
                            mLisenner.goMore(article);
                        });

                    }
                    lessonViewHolder.itemView.setOnClickListener(v -> mLisenner.goListen(article));
                    lessonViewHolder.itemView.setOnLongClickListener(view -> {
                        mLisenner.goDownload(article);
                        return true;
                    });
                }
                break;
            case 1:
                LocalViewHolder localViewHolder = (LocalViewHolder) view_holder;
                if (article != null) {
                    localViewHolder.titleView.setText((article.getTitle()));
                    localViewHolder.itemView.setOnClickListener(v -> mLisenner.goListenLocal(article));
                    localViewHolder.itemView.setOnLongClickListener(v -> {
                        mLisenner.deleteLocalFile(article);
                        return true;
                    });
                }
                break;
            case 2:
                NewsViewHolder newsViewHolder = (NewsViewHolder) view_holder;
                if (article != null) {
                    newsViewHolder.titleView.setText((article.getTitle()));
                    newsViewHolder.descView.setText(Html.fromHtml(article.getDescription().replace("FFFFCC", "FFFFFF")));
                    newsViewHolder.moreButton.setVisibility(View.GONE);
                    if (article.getLink().endsWith("mp3")){
                        newsViewHolder.playView.setVisibility(View.VISIBLE);
                        newsViewHolder.itemView.setOnLongClickListener(view -> {
                            mLisenner.goDownload(article);
                            return true;
                        });
                        newsViewHolder.itemView.setOnClickListener(v -> {
                            mLisenner.goListen(article);
                        });
                    }
                    else {
                        newsViewHolder.playView.setVisibility(View.GONE);
                        newsViewHolder.itemView.setOnClickListener(v -> {
                            mLisenner.goBrowser(article);
                    });
                    }
                }
                break;
        }
    }


    @Override
    public int getItemViewType(int position) {
        return tabType.ordinal();    //meir 0 / local - 1 / rest 2
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public class LessonViewHolder extends RecyclerView.ViewHolder {


        public LessonViewHolder(View itemView_) {
            super(itemView_);
            titleView = itemView_.findViewById(R.id.title_item);
            descView = itemView_.findViewById(R.id.desc_item);
            moreButton = itemView_.findViewById(R.id.button2);
            playView = itemView_.findViewById(R.id.imgplay);
            itemView = itemView_;
        }

        View itemView;
        TextView moreButton;
        TextView titleView;
        TextView descView;
        View playView;
    }

    public void clearData() {
        if (articles != null)
            articles.clear();
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder {


        public NewsViewHolder(View itemView_) {
            super(itemView_);
            titleView = itemView_.findViewById(R.id.title_item);
            descView = itemView_.findViewById(R.id.desc_item);
            moreButton = itemView_.findViewById(R.id.button2);
            playView = itemView_.findViewById(R.id.imgplay);
            itemView = itemView_;
        }

        View itemView;
        TextView moreButton;
        TextView titleView;
        TextView descView;
        View playView;
    }
    public class LocalViewHolder extends RecyclerView.ViewHolder {


        public LocalViewHolder(View itemView_) {
            super(itemView_);
            titleView = itemView_.findViewById(R.id.local_title_item);
            playView = itemView_.findViewById(R.id.local_imgplay);
        }
        TextView titleView;
        View playView;
    }

}
