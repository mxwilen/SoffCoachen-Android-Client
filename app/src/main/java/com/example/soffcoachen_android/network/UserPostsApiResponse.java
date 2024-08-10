package com.example.soffcoachen_android.network;

import com.example.soffcoachen_android.models.Post;

import java.util.List;

public class UserPostsApiResponse {
    private List<Post> posts;
    private String user_team;
    private String no_of_user_comments;
    private String no_of_recieved_likes;
    private String no_of_followers;

    public UserPostsApiResponse(List<Post> posts, String user_team, String no_of_user_comments, String no_of_recieved_likes, String no_of_followers) {
        this.posts = posts;
        this.user_team = user_team;
        this.no_of_user_comments = no_of_user_comments;
        this.no_of_recieved_likes = no_of_recieved_likes;
        this.no_of_followers = no_of_followers;
    }

    public List<Post> getPosts() {
        return this.posts;
    }

    public String getTeam() { return this.user_team; }

    public String getNoOfWrittenComments() { return this.no_of_user_comments; }
    public String getNoOfRecievedLikes() { return this.no_of_recieved_likes; }
    public String getNoOfFollowers() { return this.no_of_followers; }
}
