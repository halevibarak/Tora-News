package com.barak.tabs.adapter;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.barak.tabs.model.MyTab;
import com.barak.tabs.ui.FragmentArticle;

import java.util.ArrayList;


public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<MyTab> mPages;
    public ViewPagerAdapter(FragmentManager fm,ArrayList<MyTab> pages) {
        super(fm);
        mPages = pages;
    }


    @Override
    public Fragment getItem(int position) {
        return FragmentArticle.newInstance(mPages.get(position));


    }
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }


    @Override
    public int getCount() {
        return mPages.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mPages.get(position).getTitle();
    }

    public void updatePages(ArrayList<MyTab> pages) {
        mPages.clear();
        mPages.addAll(pages);
        notifyDataSetChanged();
    }
}
