package com.example.task.model;

import java.io.Serializable;

import io.realm.RealmObject;

public class News extends RealmObject implements Serializable {
    private String title;
    private String link;

    public News() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

}
