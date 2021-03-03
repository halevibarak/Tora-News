package com.barak.tabs.app;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.barak.tabs.R;
import com.barak.tabs.model.MyTab;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Barak on 24/08/2017.
 */

public class App extends Application {

    public static final String TAG = App.class
            .getSimpleName();
    private static final String TABS = "TABS_NEW";
    private static App mInstance;


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
//        startKoin {
//            androidContext(this@App)
//            modules(listOf(appModule, repoModule, viewModelModule))
//        }
    }

    public static synchronized App getInstance() {
        return mInstance;
    }



    public static void setStringArrayPref_(List<MyTab> arrayList) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getInstance());
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString(TABS, json);
        editor.commit();
    }

    public static ArrayList<MyTab> getVisTabs() {
        ArrayList<MyTab> tabs = new ArrayList<>();
        for (MyTab a : getTabs()) {
            if (a.ismVisibility()) {
                tabs.add(a);
            }
        }
        if (AppUtility.getMainExternalFolder().list() !=null && AppUtility.getMainExternalFolder().list().length>0){
            tabs.add(new MyTab("הורדות", "", MyTab.TabType.LOCAL, true));
        }
        return tabs;
    }

    public static ArrayList<MyTab> getTabs() {
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getInstance());
        Gson gson = new Gson();
        Type type = new TypeToken<List<MyTab>>() {
        }.getType();
        String json = appSharedPrefs.getString(TABS, "");
        if (json.equals("")) {
            ArrayList<MyTab> start_ = new ArrayList<>();
            start_.add(new MyTab("ישיבה", getInstance().getString(R.string.main_y), MyTab.TabType.REST, true));
            start_.add(new MyTab("מכון מאיר", getInstance().getString(R.string.main_url), MyTab.TabType.MEIR, false));
            start_.add(new MyTab("מאיר מומלצים", getInstance().getString(R.string.main_like), MyTab.TabType.MEIR, false));
            start_.add(new MyTab("ברכה פיד", getInstance().getString(R.string.main_b), MyTab.TabType.REST, false));
            start_.add(new MyTab("שאלות ישיבה", getInstance().getString(R.string.main_ask), MyTab.TabType.REST, false));
            start_.add(new MyTab("ערוץ 7", getInstance().getString(R.string.main_7), MyTab.TabType.REST, true));
            start_.add(new MyTab("מבזקים 7", getInstance().getString(R.string.main_7_m), MyTab.TabType.REST, false));
            start_.add(new MyTab("הרב שרקי", getInstance().getString(R.string.harav_sharki), MyTab.TabType.MEIR, true));
            start_.add(new MyTab("הרב ביגון", getInstance().getString(R.string.harav_bigon), MyTab.TabType.MEIR, false));
            start_.add(new MyTab("תיקון המידות",                                      getInstance().getString(R.string.harav_tikun), MyTab.TabType.MEIR, false));
            start_.add(new MyTab("הרמבם מ.אבות", getInstance().getString(R.string.harav_avot), MyTab.TabType.MEIR, false));
            start_.add(new MyTab("פרשת השבוע", getInstance().getString(R.string.harav_parasha), MyTab.TabType.MEIR, true));
            start_.add(new MyTab("אמונה", getInstance().getString(R.string.harav_emuna), MyTab.TabType.MEIR, false));
            start_.add(new MyTab("אורות", getInstance().getString(R.string.harav_orot), MyTab.TabType.MEIR, false));
            start_.add(new MyTab("הרב קוק", getInstance().getString(R.string.harav_kuk), MyTab.TabType.MEIR, false));
            start_.add(new MyTab("הרמבם", getInstance().getString(R.string.harav_ram), MyTab.TabType.MEIR, false));
            start_.add(new MyTab("הרמחל", getInstance().getString(R.string.harav_ramchal), MyTab.TabType.MEIR, false));
            start_.add(new MyTab("הרב אייל", getInstance().getString(R.string.harav_eyal), MyTab.TabType.MEIR, true));
            start_.add(new MyTab("הרב לונדין", getInstance().getString(R.string.harav_lon), MyTab.TabType.MEIR, false));
            start_.add(new MyTab("הרב אנגלמן", getInstance().getString(R.string.harav_eng), MyTab.TabType.MEIR, false));
            start_.add(new MyTab("הרב אבינר", getInstance().getString(R.string.harav_aviner), MyTab.TabType.MEIR, false));
            start_.add(new MyTab("הרב י.זאגא", getInstance().getString(R.string.harav_zaga), MyTab.TabType.MEIR, false));
            start_.add(new MyTab("הרב קשתיאל", getInstance().getString(R.string.harav_kashtiel), MyTab.TabType.MEIR, false));
            start_.add(new MyTab("הדף היומי",  getInstance().getString(R.string.harav_yomi), MyTab.TabType.MEIR, false));
            start_.add(new MyTab("מדיטציה יהודית", getInstance().getString(R.string.harav_med), MyTab.TabType.MEIR, false));
            start_.add(new MyTab("סתם חיובי", getInstance().getString(R.string.harav_pos), MyTab.TabType.REST, true));
            setStringArrayPref_(start_);
            return start_;
        }
        return gson.fromJson(json, type);

    }


}