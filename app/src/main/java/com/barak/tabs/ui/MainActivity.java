package com.barak.tabs.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.barak.tabs.Parser.Article;
import com.barak.tabs.Parser.XMLParser;
import com.barak.tabs.R;
import com.barak.tabs.adapter.ViewPagerAdapter;
import com.barak.tabs.app.App;
import com.barak.tabs.app.AppUtility;
import com.barak.tabs.app.Singleton;
import com.barak.tabs.app.VolleySingleton;
import com.barak.tabs.manage.ManageActivity;
import com.barak.tabs.model.ChangeLogDialog;
import com.barak.tabs.model.MyTab;
import com.barak.tabs.models.Item;
import com.barak.tabs.network.ConnectivityHelper;
import com.barak.tabs.network.DownloadExecelDataSource;
import com.barak.tabs.notif.AlarmUtils;
import com.barak.tabs.notif.MyBroadcastReceiver;
import com.barak.tabs.service.Mp3Binder;
import com.barak.tabs.service.Mp3Service;
import com.barak.tabs.service.Mp3ServiceImpl;
import com.barak.tabs.widget.PlayerWidget;
import com.barak.tabs.widget_list.ListWidget;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.barak.tabs.app.DownloadToExtStrService.DOWNLOAD_ERR;
import static com.barak.tabs.app.DownloadToExtStrService.DOWNLOAD_TAB;
import static com.barak.tabs.app.DownloadToExtStrService.DOWNLOAD_TAB_ACTION;
import static com.barak.tabs.manage.ManageActivity.NOTIF_HOUR;
import static com.barak.tabs.manage.ManageActivity.NOTIF_MINUT;
import static com.barak.tabs.notif.BootComplete.BroadcastService.FROM_BLE;
import static com.barak.tabs.ui.ArticleViewModel.NOTIF_ALLOW;


public class MainActivity extends AppCompatActivity implements FragmentArticle.OnCompleteListener, Observer {
    private static final int PERMISSION_REQUEST_CODE = 45454;
    public static final String MESSAGE_PROGRESS = "message_progress";
    private static final String PREFERENCE = "PREFERENCE";
    public static final String BLE_ALLOW = "BLE_ALLOW";
    private Mp3Service mMP3Service;

    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    private Item mRabbiPost;
    private static final String URL_JSON = "http://www.meirtv.co.il/site/rss/archive.asp";

    private CompositeDisposable _disposables = new CompositeDisposable();
    private ProgressBar progressBar;
    private Item mArticle;
    public PlayerControlView playerView;
    private boolean mBound = false;
    private ChangeLogDialog myNewDialog;
    private boolean isFirstRun;
    private boolean secondRun;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressbar);
        playerView = findViewById(R.id.player);
        AppRater.INSTANCE.app_launched(this);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabs);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), App.getVisTabs());
        updateTabs(this);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(1);
        tabLayout.setupWithViewPager(viewPager);
        checkFirstRun();

        SharedPreferences prefs = getSharedPreferences(NOTIF_ALLOW, MODE_PRIVATE);
        if (prefs.getBoolean(NOTIF_ALLOW, true)) {
            startAlert();
        }
        Intent intent = getIntent();
        if (intent.getBooleanExtra(DOWNLOAD_TAB, false)) {
            viewPager.setCurrentItem(App.getVisTabs().size() - 1);
        }
        if (!ConnectivityHelper.isConnectedToNetwork(getApplicationContext())) {
            viewPager.setCurrentItem(App.getVisTabs().size() - 1);
            Snackbar.make(progressBar, getString(R.string.no_record), Snackbar.LENGTH_LONG).show();
        }


    }


    private void startAlert() {
        Intent intent = new Intent(this, MyBroadcastReceiver.class);
        AlarmUtils.cancelAllAlarms(this, intent);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (calendar.get(Calendar.HOUR_OF_DAY) >= NOTIF_HOUR) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        int day;
        calendar.set(Calendar.MINUTE, NOTIF_MINUT);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.HOUR_OF_DAY, NOTIF_HOUR);
        int id = 0;
        for (int i = 0; i < 15; i++) {
            day = calendar.get(Calendar.DAY_OF_WEEK);
            if (day >= 0 && day <= 5) {
                AlarmUtils.addAlarm(this, intent, id, calendar.getTimeInMillis());
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            id++;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void onComplete() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    //build new rss feed url from other rss feed
    public void mainMore(Item article) {
        progressBar.setVisibility(View.VISIBLE);
        mRabbiPost = article;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_JSON,
                response -> {
                    XMLParser xmlParser = new XMLParser(true);
                    xmlParser.addObserver(MainActivity.this);
                    try {
                        xmlParser.parseXML((response));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> {
        });
        VolleySingleton.Companion.getInstance().addToRequestQueue(stringRequest);
    }


    @Override
    public void update(Observable o, Object data) {
        for (Article a : (ArrayList<Article>) data) {
            if (a.getTitle().equals(mRabbiPost.getTitle())) {
                int pos = a.getLink().indexOf("cat_id=");
                String addToLink = "?cat_id=" + a.getLink().substring(pos + 7, pos + 11);
                ArrayList<MyTab> arrayList = App.getTabs();
                for (MyTab mt : arrayList) {
                    if (mt.getUrl().equals(getString(R.string.main_url) + addToLink)) {
                        if (mt.ismVisibility() == false) {
                            mt.setmVisibility(true);
                            App.setStringArrayPref_(arrayList);
                            viewPagerAdapter.updatePages(App.getVisTabs());
                        } else {
                            Toast.makeText(this, getString(R.string.already_exist), Toast.LENGTH_LONG).show();
                        }
                        progressBar.setVisibility(View.GONE);
                        return;
                    }
                }
                String title = App.convertToUTF8(mRabbiPost.getTitle());
                String newtitle = "";
                for (int i = 1; i < title.length(); i = i + 2) {
                    newtitle = newtitle + title.charAt(i);
                }
                newtitle = mRabbiPost.getDescription();
                int pos2 = newtitle.indexOf("(");
                if (pos2 > 0)
                    newtitle = newtitle.substring(0, pos2 - 1);
                arrayList.add(new MyTab(App.convertToUTF8(newtitle), getString(R.string.main_url) + addToLink, MyTab.TabType.MEIR, true));
                App.setStringArrayPref_(arrayList);
                viewPagerAdapter.updatePages(App.getVisTabs());
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void playLocalMp(Item article) {
        Item art = new Item(article.getTitle() + " ", AppUtility.getMainExternalFolder().getAbsolutePath() + "/" + article.getTitle());
        playMp(art);
    }

    @Override
    public void removeAddDownloadTab() {
        viewPagerAdapter.updatePages(App.getVisTabs());
        PlayerWidget.Companion.update(this);
        ListWidget.update(this);
    }

    public void playMp(Item article) {
        Singleton.Companion.getInstance().setPlayList(null);
        Singleton.Companion.getInstance().setLastArticle(article);
        registerReceiver();
        if (Singleton.Companion.getInstance().getService() != null) {
            mMP3Service = Singleton.Companion.getInstance().getService();
        }
        if (mMP3Service != null && mMP3Service.isPlayOrPause()) {
            mMP3Service.stop4Play();
            mMP3Service.play(article.getLink(), article.getTitle(), playerView);
            return;
        }
        Intent it = new Intent(this, Mp3ServiceImpl.class);
        startService(it);
        bindService(it, mConnection, 0);
        if (mMP3Service == null) {
            mArticle = article;
        } else {
            mArticle = null;
            if (article.getLink() != null) {
                mMP3Service.play(article.getLink(), article.getTitle(), playerView);
            }
        }
    }

    public void playMp(List<Item> articles) {
        if (!getIntent().getBooleanExtra(
                FROM_BLE,false)){
            return;
        }
        Singleton.Companion.getInstance().setLastArticle(articles.get(0));
        registerReceiver();
        if (Singleton.Companion.getInstance().getService() != null) {
            mMP3Service = Singleton.Companion.getInstance().getService();
        }
        if (mMP3Service != null && mMP3Service.isPlayOrPause()) {
            mMP3Service.stop4Play();
            mMP3Service.play(articles, articles.get(0).getTitle(), playerView);
            return;
        }
        Intent it = new Intent(this, Mp3ServiceImpl.class);
        startService(it);
        bindService(it, mConnection, 0);
        if (mMP3Service == null) {
            mArticle = articles.get(0);
        } else {
            mArticle = null;
            if (articles.get(0).getLink() != null) {
                mMP3Service.play(articles.get(0).getLink(), articles.get(0).getTitle(), playerView);
            }
        }
    }

    public void checkFirstRun() {
        isFirstRun = getSharedPreferences(PREFERENCE, MODE_PRIVATE).getBoolean("isFirstRun", true);
        secondRun = getSharedPreferences(PREFERENCE, MODE_PRIVATE).getBoolean("isSecondRun", true);

        if (isFirstRun) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.start_title)).setMessage(getString(R.string.start_text))
                    .setNeutralButton(getString(R.string.submit_), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int d) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                Intent i = new Intent(MainActivity.this, ManageActivity.class);
                                MainActivity.this.startActivityForResult(i, 1);
                            }
                        }
                    });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                alert.setOnDismissListener(dialogInterface -> {
                    Intent i = new Intent(MainActivity.this, ManageActivity.class);
                    MainActivity.this.startActivityForResult(i, 1);
                });
            }
            alert.show();
            getSharedPreferences(PREFERENCE, MODE_PRIVATE)
                    .edit()
                    .putBoolean("isFirstRun", false)
                    .apply();
        } else if (secondRun) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.start_title)).setMessage(getString(R.string.start_text2));
            alert.show();
            getSharedPreferences(PREFERENCE, MODE_PRIVATE)
                    .edit()
                    .putBoolean("isSecondRun", false)
                    .apply();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, ManageActivity.class);
            this.startActivityForResult(i, 1);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        viewPagerAdapter.updatePages(App.getVisTabs());
    }


    public void download(Item article) {
        if (checkPermission()) {
            registerReceiver();
            Snackbar.make(progressBar, "הורדה מתחילה", Snackbar.LENGTH_LONG).show();
            String newtitle = "";
            if (article.getLink().contains("meir")) {
                String title = App.convertToUTF8(article.getTitle());
                for (int i = 1; i < title.length(); i = i + 2) {
                    newtitle = newtitle + title.charAt(i);
                }
            }
            int minut = article.getDescription().indexOf("(");
            if (minut == -1) {
                AppUtility.downLoadSongToexternalStorage(this, article.getLink(), article.getTitle());
            } else {
                String timeStr = " " + article.getDescription().substring(minut);
                AppUtility.downLoadSongToexternalStorage(this, article.getLink(), newtitle + timeStr);
            }

        } else {
            mArticle = article;
            requestPermission();
        }

    }

    public void playMp(List<Item> articles, int index) {
        if (index<0) return;
        if (!getIntent().getBooleanExtra(FROM_BLE,true)){
            return;
        }
        Singleton.Companion.getInstance().setPlayList(articles);
        Singleton.Companion.getInstance().setLastArticle(articles.get(index));
        registerReceiver();
        if (Singleton.Companion.getInstance().getService() != null) {
            mMP3Service = Singleton.Companion.getInstance().getService();
        }
        if (mMP3Service != null && mMP3Service.isPlayOrPause()) {
            mMP3Service.stop4Play();
            mMP3Service.play(articles, articles.get(index).getTitle(), playerView);
            return;
        }
        Intent it = new Intent(this, Mp3ServiceImpl.class);
        startService(it);
        bindService(it, mConnection, 0);
        if (mMP3Service == null) {
            mArticle = articles.get(index);
        } else {
            mArticle = null;
            if (articles.get(index).getLink() != null) {
                mMP3Service.play(articles.get(index).getLink(), articles.get(index).getTitle(), playerView);
            }
        }
    }


    private void registerReceiver() {

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DOWNLOAD_TAB_ACTION);
        bManager.registerReceiver(broadcastReceiver, intentFilter);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DOWNLOAD_TAB_ACTION)) {
                if (intent.getBooleanExtra(DOWNLOAD_ERR, false)) {
                    Snackbar.make(progressBar, "תקלה", Snackbar.LENGTH_LONG).show();
                    LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(broadcastReceiver);
                    return;
                }
                if (AppUtility.getMainExternalFolder().list().length < 2) {
                    removeAddDownloadTab();
                    viewPagerAdapter.notifyDataSetChanged();
                }
                Snackbar.make(progressBar, "הורדה הסתיימה", Snackbar.LENGTH_LONG).show();
                LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(broadcastReceiver);
            }
        }
    };

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(progressBar, "הורדה מתחילה", Snackbar.LENGTH_LONG).show();
                    registerReceiver();
                    String newtitle = "";
                    if (mArticle.getLink().contains("meir")) {
                        String title = App.convertToUTF8(mArticle.getTitle());
                        for (int i = 1; i < title.length(); i = i + 2) {
                            newtitle = newtitle + title.charAt(i);
                        }
                    }
                    AppUtility.downLoadSongToexternalStorage(this, mArticle.getLink(), newtitle);
                    mArticle = null;
                    removeAddDownloadTab();
                } else {
                    Snackbar.make(progressBar, "Permission Denied, Please allow to proceed !", Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }

    public void updateTabs(MainActivity mainActivity) {
        WeakReference<MainActivity> activityWeakReference = new WeakReference<>(mainActivity);
        DisposableObserver<Boolean> d = _getDisposableObserver(activityWeakReference);

        _getObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(d);

        _disposables.add(d);

    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            mMP3Service = ((Mp3Binder) service).getService();
            Singleton.Companion.getInstance().setService(mMP3Service);
            mMP3Service.bindPlayerView(playerView);
            if (mArticle != null && !mMP3Service.isPlayingNow()) {
                mMP3Service.stop();
                if (Singleton.Companion.getInstance().getPlayList() != null) {
                    mMP3Service.play(Singleton.Companion.getInstance().getPlayList(), mArticle.getTitle(), playerView);
                } else {
                    mMP3Service.play(mArticle.getLink(), mArticle.getTitle(), playerView);
                }
                mMP3Service.play(mArticle.getLink(), mArticle.getTitle(), playerView);
            }
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mMP3Service = null;
            Singleton.Companion.getInstance().setService(null);
            if (playerView != null) {
                if (!playNext()) {
                    playerView.hide();
                    playerView.setPlayer(null);
                }

            }
            mBound = false;
        }
    };

    private boolean playNext() {
//        if (Singleton.Companion.getInstance().getPlayList() != null &&
//                Singleton.Companion.getInstance().getPlayList().size() > 0) {
//
//            playMp(Singleton.Companion.getInstance().getPlayList());
//            Singleton.Companion.getInstance().setPlayList(Singleton.Companion.getInstance().getPlayList().subList(1,
//                    Singleton.Companion.getInstance().getPlayList().size() - 1));
//            return true;
//        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mMP3Service == null) {
            mMP3Service = Singleton.Companion.getInstance().getService();
        }
        if (playerView != null && mMP3Service != null && mMP3Service.isPlayOrPause()) {
            Intent it = new Intent(this, Mp3ServiceImpl.class);
            startService(it);
            bindService(it, mConnection, 0);

            playerView.setVisibility(View.VISIBLE);
        } else {
            playerView.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mMP3Service.unBindPlayerView(playerView);
            Singleton.Companion.getInstance().setService(mMP3Service);
        }
        mBound = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        mMP3Service = null;
        tabLayout = null;
        viewPager = null;
        viewPagerAdapter = null;
        mRabbiPost = null;
        progressBar = null;
        mArticle = null;
        releasePlayer();
        _disposables.clear();
    }

    private void releasePlayer() {
        if (playerView != null && playerView.getPlayer() != null) {
            playerView.removeAllViews();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    private DisposableObserver<Boolean> _getDisposableObserver(WeakReference<MainActivity> activityWeakReference) {
        return new DisposableObserver<Boolean>() {

            @Override
            public void onComplete() {
                if (myNewDialog == null || secondRun || isFirstRun) return;
                SharedPreferences prefs = getSharedPreferences(PREFERENCE, MODE_PRIVATE);
                if (prefs.getBoolean(myNewDialog.getSharedKey(), false)) return;
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle(getString(R.string.start_title))
                        .setMessage(myNewDialog.getTitle()).show();
                prefs.edit()
                        .putBoolean(myNewDialog.getSharedKey(), true)
                        .apply();
                if (myNewDialog.getAction() == 11 && activityWeakReference != null) {
                    activityWeakReference.get().viewPagerAdapter.updatePages(App.getVisTabs());
                }
                myNewDialog = null;
            }

            @Override
            public void onError(Throwable e) {
                Snackbar.make(progressBar, e.getMessage(), Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onNext(Boolean bool) {
            }
        };
    }


    private io.reactivex.Observable<Boolean> _getObservable() {
        return io.reactivex.Observable.just(true)
                .map(
                        aBoolean -> {
                            updateTabsTask();
                            return aBoolean;
                        });
    }

    private void updateTabsTask() {
        try {
            myNewDialog = DownloadExecelDataSource.downloadUrl("https://spreadsheets.google.com/tq?key=1JJ0-iJ8fPhvF4IZo7Hug0PgGYVbtLDMM9PxJq4hesiw");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (playerView != null && playerView.dispatchKeyEvent(event)) {
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }


}