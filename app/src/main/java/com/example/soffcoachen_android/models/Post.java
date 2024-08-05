package com.example.soffcoachen_android.models;

import static android.content.ContentValues.TAG;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

public class Post {
    private final String title;
    private final String content;
    private final String author;
    private final int id;
    private final int user_id;
    private final String date_posted;
    private final String team;
    private final String tag;
    private final String user_team;

    public Post(String title, String content, int id, String date_posted, String team, String tag, String author, int user_id, String user_team) {
        this.title = title;
        this.content = content;
        this.id = id;
        this.date_posted = date_posted;
        this.team = team;
        this.tag = tag;
        this.author = author;
        this.user_id = user_id;
        this.user_team = user_team;
    }

    public String getTitle() {
        return this.title;
    }
    public String getContent() {
        return this.content;
    }
    public int getPostId() {
        return this.id;
    }

    public String getAuthor() {
        return this.author;
    }
    public int getUserId() {
        return this.user_id;
    }
    public String getDatePosted() {
        return this.date_posted;
    }
    public String getUserTeam() { return this.user_team; }
    public String getPostTeam() { return this.team; }
    public String getPostTag() { return this.tag; }
}
