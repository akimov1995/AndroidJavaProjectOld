package com.example.task.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.task.model.News;
import com.example.task.services.DownloadNewsService;
import com.example.task.R;
import com.example.task.utils.NetworkStatusChecker;
import com.example.task.db.RealmManager;

import java.util.ArrayList;

public class NewsActivity extends AppCompatActivity {
    private NewsReceiver newsReceiver;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ArrayList<News> newsList;
    private RealmManager realmManager;
    private ListView listView;
    private SharedPreferences sharedPreferences;
    private int period;
    private final int minutePeriod = 60000;
    private String newsType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        getSupportActionBar().setTitle(getResources().getString(R.string.news_activity_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newsList = new ArrayList<>();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        period = Integer.parseInt(sharedPreferences.getString("time", "10"));
        newsType = sharedPreferences.getString("news", getResources().getString(R.string.def));
        realmManager = new RealmManager(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshNews();
                listView.invalidateViews();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                Intent intent = new Intent(NewsActivity.this, WebViewActivity.class);
                intent.putExtra("url", newsList.get(pos).getLink());
                startActivity(intent);
            }
        });

        sharedPreferences.registerOnSharedPreferenceChangeListener(
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        if (key.equals("time")) {
                            int newValue = Integer.parseInt(sharedPreferences.getString(key, "10"));
                            if (period != newValue) {
                                period = newValue;
                                updateSettings();
                            }
                        } else if (key.equals("news")) {
                            String newValue = sharedPreferences.getString("news", "Все новости:Лента новостей");

                            if (!newsType.equals(newValue)) {
                                newsType = newValue;
                                updateSettings();
                            }
                        }
                    }
                });

        newsReceiver = new NewsReceiver();
        IntentFilter intentFilter = new IntentFilter(
                DownloadNewsService.ACTION_SERVICE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(newsReceiver, intentFilter);

        loadNews();
        DownloadNewsService.setServiceAlarm(NewsActivity.this, true, minutePeriod * period, newsType);
    }

    private void loadNews() {
        Intent intentService = new Intent(NewsActivity.this, DownloadNewsService.class);
        intentService.putExtra("type", newsType);
        startService(intentService);
    }

    public class NewsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                newsList.addAll((ArrayList<News>) intent.getSerializableExtra("news"));
                ArrayList<String> titleList = new ArrayList<>();
                for (News news : newsList) {
                    titleList.add(news.getTitle());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(NewsActivity.this,
                        android.R.layout.simple_list_item_1, titleList);
                listView.setAdapter(adapter);

                realmManager.clearAllNews();
                realmManager.saveNews(newsList);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void updateSettings() {
        loadNews();
        DownloadNewsService.setServiceAlarm(NewsActivity.this, false, minutePeriod * period, newsType);
        DownloadNewsService.setServiceAlarm(NewsActivity.this, true, minutePeriod * period, newsType);
    }

    private void refreshNews() {
        if (NetworkStatusChecker.isNetworkAvailable(this)) {
            loadNews();
        } else {
            newsList = realmManager.getNews();
            ArrayList<String> titleList = new ArrayList<>();
            for (News news : newsList) {
                titleList.add(news.getTitle());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(NewsActivity.this,
                    android.R.layout.simple_list_item_1, titleList);
            listView.setAdapter(adapter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(newsReceiver);
        DownloadNewsService.setServiceAlarm(NewsActivity.this, false, minutePeriod * period, newsType);
        realmManager.close();
    }
}

