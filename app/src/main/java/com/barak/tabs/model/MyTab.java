package com.barak.tabs.model;

import java.io.Serializable;

/**
 * Created by Barak Halevi on 11/11/2018.
 */



public class MyTab implements Serializable {
    String title;
    String url;
    TabType tabType;
    boolean mVisibility;

    public MyTab(String title, String url, TabType tabType_, boolean visibility) {
        this.title = title;
        this.url = url;
        tabType = tabType_;
        this.mVisibility = visibility;

    }


    public TabType getTabType() {
        return tabType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean ismVisibility() {
        return mVisibility;
    }

    public void setmVisibility(boolean mVisibility) {
        this.mVisibility = mVisibility;
    }

    public enum TabType
    {
        MEIR, LOCAL, REST;
        public static TabType fromInt(int i) {
            for (TabType b : TabType.values()) {
                if (b.ordinal() == i) { return b; }
            }
            return null;
        }
    }
}
