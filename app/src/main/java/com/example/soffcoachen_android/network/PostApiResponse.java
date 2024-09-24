package com.example.soffcoachen_android.network;

import com.example.soffcoachen_android.models.Comment;
import com.example.soffcoachen_android.models.Post;

import java.util.List;

public class PostApiResponse {
    private Post post;
    private List<Comment> comments;
    private boolean hasComments;

    public PostApiResponse(Post post, List<Comment> comments, boolean hasComments) {
        this.post = post;
        this.comments = comments;
        this.hasComments = hasComments;
    }

    public Post getPost() {
        return post;
    }
    public List<Comment> getComments() {
        return comments;
    }
    public int getNoOfComments() {
        return comments.size();
    }
    public boolean getHasComments() {
        return hasComments;
    }
}
