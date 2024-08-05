package com.example.soffcoachen_android.models;

public class Comment {
    private final int id;
    private final String content;
    private final String dateCommented;
    private final String author;

    public Comment(int id, String content, String dateCommented, String author) {
        this.id = id;
        this.content = content;
        this.dateCommented = dateCommented;
        this.author = author;
    }

    public int getId() {
        return this.id;
    }
    public String getContent() {
        return this.content;
    }
    public String getAuthor() {
        return this.author;
    }
    public String getDateCommented() {
        return this.dateCommented;
    }
}
