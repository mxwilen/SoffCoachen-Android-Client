package com.example.soffcoachen_android.network;

import com.example.soffcoachen_android.models.Post;
import com.example.soffcoachen_android.models.Team;

import java.util.List;

public class HomeApiResponse {
    private String current_user;
    private String status;
    private List<Team> teams;
    private List<Post> posts;

    public HomeApiResponse(String current_user, String status, List<Team> teams, List<Post> posts) {
        this.current_user = current_user;
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

    public String getCurrentUser() { return current_user; }
}
