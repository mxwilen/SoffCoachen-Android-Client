package com.example.soffcoachen_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soffcoachen_android.R;
import com.example.soffcoachen_android.models.Post;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final List<Post> postList;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.authorTextView.setText(post.getAuthor());
        holder.authorTeamTextView.setText(post.getUserTeam());
        holder.datePostedTextView.setText(post.getDatePosted());
        holder.titleTextView.setText(post.getTitle());
        holder.contentTextView.setText(post.getContent());
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView authorTextView;
        TextView authorTeamTextView;
        TextView datePostedTextView;
        TextView titleTextView;
        TextView contentTextView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            authorTextView = itemView.findViewById(R.id.post_author);
            authorTeamTextView = itemView.findViewById(R.id.post_author_team);
            datePostedTextView = itemView.findViewById(R.id.post_date_posted);
            titleTextView = itemView.findViewById(R.id.post_title);
            contentTextView = itemView.findViewById(R.id.post_content);
        }
    }
}
