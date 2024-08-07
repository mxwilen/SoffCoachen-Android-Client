package com.example.soffcoachen_android.models;

public class Comment {
    private final int id;
    private final String content;
    private final String date_commented;
    private final String author;
    private final String author_team;
    private final String no_of_likes;

    public Comment(int id, String content, String date_commented, String author, String author_team, String no_of_likes) {
        this.id = id;
        this.content = content;
        this.date_commented = date_commented;
        this.author = author;
        this.author_team = author_team;
        this.no_of_likes = no_of_likes;
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
    public String getAuthorTeam() {
        return this.author_team;
    }
    public String getDateCommented() {
        return this.date_commented;
    }
    public String getNoOfLikes() {
        return this.no_of_likes;
    }
}
