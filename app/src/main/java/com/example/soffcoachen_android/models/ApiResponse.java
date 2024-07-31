package com.example.soffcoachen_android.models;

import java.util.List;

public class ApiResponse {
    private String status;
    private List<Team> teams;
    private List<Post> posts;

    public ApiResponse(String status, List<Team> teams, List<Post> posts) {
        this.status = status;
        this.teams = teams;
        this.posts = posts;
    }

    public String getStatus() {
        return status;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public List<Post> getPosts() {
        return posts;
    }
}
