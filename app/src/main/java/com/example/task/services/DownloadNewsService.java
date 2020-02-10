package com.example.task.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.task.R;
import com.example.task.model.News;
import com.example.task.utils.NetworkStatusChecker;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class DownloadNewsService extends IntentService {
    public static final String ACTION_SERVICE = "com.example.task.RESPONSE";

    public DownloadNewsService() {
        super("");
    }

    public void loadNews(String type) {
        if (NetworkStatusChecker.isNetworkAvailable(this)) {
            Document doc;
            Elements elements;
            String url;
            ArrayList<News> newsList = new ArrayList<>();

            try {
                switch (type) {
                    case "Все новости:Лента новостей":
                        url = getResources().getString(R.string.base_url);
                        doc = Jsoup.connect(url).get();
                        elements = doc.getElementsByClass("news-feed__item js-news-feed-item js-yandex-counter")
                                .select("a");

                        for (int i = 0; i < elements.size(); i++) {
                            News data = new News();
                            data.setLink(elements.get(i).attr("href"));
                            data.setTitle(elements.get(i).select("span.news-feed__item__title").text());
                            newsList.add(data);
                        }
                        break;

                    case "Автомобили:Тест-драйвы":
                        url = getResources().getString(R.string.test_drive_url);
                        doc = Jsoup.connect(url).get();
                        elements = doc.select("div.tag-news").first()
                                .getElementsByClass("tag-news__container item-medium" +
                                        "__wrap js-load-container")
                                .first().getElementsByClass("item-medium  js" +
                                        "-item-medium js-exclude-block");

                        for (int i = 0; i < elements.size(); i++) {
                            News data = new News();
                            data.setLink(elements.get(i).select("a").get(2).attr("href"));
                            data.setTitle(elements.get(i).select("a").get(2)
                                    .select("span.item-medium__title").text());
                            newsList.add(data);
                        }
                        break;

                    case "Автомобили:Лента новостей":
                        url = getResources().getString(R.string.auto_news_url);
                        doc = Jsoup.connect(url).get();
                        elements = doc.select("div.news-feed__scroll__inner").first()
                                .getElementsByClass("news-feed__item js-sly-item");

                        for (int i = 0; i < elements.size(); i++) {
                            News data = new News();
                            data.setLink(elements.get(i).select("a").attr("href"));
                            data.setTitle(elements.get(i).select("a").select("div.news-feed__" +
                                    "item__title").text());
                            newsList.add(data);
                        }
                        break;

                    case "Спорт:Главные":
                        url = getResources().getString(R.string.sport_news_url);
                        doc = Jsoup.connect(url).get();
                        elements = doc.select("div.l-col-center__inner").first()
                                .getElementsByClass("item-sport_medium js-item-sport ");


                        for (int i = 0; i < elements.size(); i++) {
                            News data = new News();
                            data.setLink(elements.get(i).select("a").first().attr("href"));
                            data.setTitle(elements.get(i).select("a").first()
                                    .select("span.item-sport_medium__title").text());
                            newsList.add(data);
                        }
                        break;
                }

                Intent responseIntent = new Intent();
                responseIntent.setAction(ACTION_SERVICE);
                responseIntent.addCategory(Intent.CATEGORY_DEFAULT);
                responseIntent.putExtra("news", newsList);
                sendBroadcast(responseIntent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setServiceAlarm(Context context, boolean isOn, int interval, String type) {
        Intent i = new Intent(context, DownloadNewsService.class);
        i.putExtra("type", type);
        PendingIntent pi = PendingIntent.getService(
                context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(), interval, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        loadNews(intent.getStringExtra("type"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
