package com.example.task.db;

import android.content.Context;

import com.example.task.model.News;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class RealmManager {
    private Realm realm;

    public RealmManager(Context context) {
        realm = realm.getInstance(context);
    }

    public void saveNews(ArrayList<News> newsList) {
        realm.beginTransaction();
        for (News news : newsList) {
            News data = realm.createObject(News.class);
            data.setTitle(news.getTitle());
            data.setLink(news.getLink());
        }
        realm.commitTransaction();
    }

    public void clearAllNews() {
        realm.beginTransaction();
        realm.allObjects(News.class).clear();
        realm.commitTransaction();
    }

    public ArrayList<News> getNews() {
        RealmResults<News> results = realm.allObjects(News.class);
        ArrayList<News> news = new ArrayList<>(results);
        return news;
    }

    public void close() {
        realm.close();
    }
}
