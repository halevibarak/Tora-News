package com.barak.tabs.manage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.barak.tabs.R;
import com.barak.tabs.app.App;
import com.barak.tabs.model.MyTab;
import com.barak.tabs.notif.AlarmUtils;
import com.barak.tabs.notif.BootComplete;
import com.barak.tabs.notif.MyBroadcastReceiver;

import java.util.ArrayList;
import java.util.Calendar;

import static com.barak.tabs.ui.ArticleModel.NOTIF_ALLOW;
import static com.barak.tabs.ui.ArticleModel.START_ALLOW;

public class ManageActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ArrayList<MyTab> mPages;
    private TabManageAdapter articleAdapter;
    private CheckBox chkIos,chkStart;
    public static final int NOTIF_HOUR = 17;
    public static final int NOTIF_MINUT = 0;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);
        mPages = App.getTabs();
        mRecyclerView = findViewById(R.id.contact_list);
        if (checkFirstRun()){
            findViewById(R.id.first).setVisibility(View.VISIBLE);
        }
        chkIos = findViewById(R.id.button);
        chkStart = findViewById(R.id.button_2);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
        articleAdapter = new TabManageAdapter(mPages);
        mRecyclerView.setAdapter(articleAdapter);


        prefs = getSharedPreferences(NOTIF_ALLOW, MODE_PRIVATE);
        boolean allow = prefs.getBoolean(NOTIF_ALLOW, false);
        boolean allowStart = prefs.getBoolean(START_ALLOW, false);
        if (allow) {
            startAlert();
            chkIos.setChecked(true);
        } else {
            chkIos.setChecked(false);
        }
        chkIos.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            if (((CheckBox) v).isChecked()) {
                editor.putBoolean(NOTIF_ALLOW, true);
                startAlert();
            } else {
                editor.putBoolean(NOTIF_ALLOW, false);
                Intent intent = new Intent(ManageActivity.this, MyBroadcastReceiver.class);
                AlarmUtils.cancelAllAlarms(this, intent);
            }
            editor.apply();
        });

        if (allowStart) {
            chkStart.setChecked(true);
        } else {
            chkStart.setChecked(false);
        }
        chkStart.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            if (((CheckBox) v).isChecked()) {
                editor.putBoolean(START_ALLOW, true);
                BootComplete.checkStartVpnOnBoot(this,new BootComplete.BroadcastService());
            } else {
                editor.putBoolean(START_ALLOW, false);
            }
            editor.apply();
        });



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
    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            int position = viewHolder.getAdapterPosition();
            if (App.getVisTabs().size()==1 && articleAdapter.getItem(position).ismVisibility()){
                articleAdapter.notifyDataSetChanged();
                return;

            }
            mPages.remove(articleAdapter.getItem(position));
            App.setStringArrayPref_(mPages);
            articleAdapter.notifyDataSetChanged();
        }
    };

    public boolean checkFirstRun() {
        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isManageFirstRun", true);
        if (isFirstRun) {
            getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    .edit()
                    .putBoolean("isManageFirstRun", false)
                    .apply();

        }
        return isFirstRun;
    }
}
