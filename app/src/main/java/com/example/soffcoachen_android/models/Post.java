package com.example.soffcoachen_android.models;

import android.util.Log;

import com.google.android.material.tabs.TabLayout;

public class Post {
    private final String title;
    private final String content;
    private final String author;

    public Post(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }

    public String getTitle() {
        return this.title;
    }

    public String getContent() {
        return this.content;
    }

    public String getAuthor() {
        return this.author;
    }
}
