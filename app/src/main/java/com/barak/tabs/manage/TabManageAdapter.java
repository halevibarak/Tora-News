package com.barak.tabs.manage;

import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.barak.tabs.R;
import com.barak.tabs.app.App;
import com.barak.tabs.model.MyTab;

import java.util.List;

/**
 * Created by Barak on 24/08/2017.
 */

public class TabManageAdapter extends RecyclerView.Adapter<TabManageAdapter.ArticleViewHolder> {

    private List<MyTab> mPages;


    public TabManageAdapter(List<MyTab> ArticleList) {
        this.mPages = ArticleList;

    }

    @Override
    public int getItemCount() {
        return mPages.size();
    }

    public MyTab getItem(int position) {
        return mPages.get(position);
    }


    @Override
    public ArticleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View horizontalItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.art_item, parent, false);
        return new ArticleViewHolder(horizontalItem);
    }

    @Override
    public void onBindViewHolder(ArticleViewHolder holder, int position) {

        final MyTab myTab = getItem(position);
        if (myTab != null) {
            holder.urlView.setText("");
            holder.titleView.setText(myTab.getTitle());
            holder.checkBox.setChecked(myTab.ismVisibility());
            holder.checkBox.setOnClickListener(v -> {
                if (((CheckBox) v).isChecked()) {
                    myTab.setmVisibility(true);
                }
                else{
                    if (App.getVisTabs().size()==1){
                        holder.checkBox.setChecked(true);
                        return;
                    }
                    myTab.setmVisibility(false);
                }
                mPages.set(position, myTab);
                App.setStringArrayPref_(mPages);

            });
        }
        holder.itemView.setOnClickListener(v -> {

        });
    }

    public class ArticleViewHolder extends RecyclerView.ViewHolder {


        public ArticleViewHolder(View itemView) {
            super(itemView);
            urlView =  itemView.findViewById(R.id.url_text);
            titleView =  itemView.findViewById(R.id.title_text);
            checkBox =  itemView.findViewById(R.id.chk_view);

        }

        private  AppCompatCheckBox checkBox;
        TextView urlView;
        TextView titleView;
    }

}
