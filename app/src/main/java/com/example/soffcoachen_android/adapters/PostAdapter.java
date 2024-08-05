package com.example.soffcoachen_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soffcoachen_android.R;
import com.example.soffcoachen_android.models.Post;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final List<Post> postList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

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
        holder.postTeamTextView.setText(post.getPostTeam());
        holder.postTagTextView.setText(post.getPostTag());
        holder.titleTextView.setText(post.getTitle());
        holder.contentTextView.setText(post.getContent());
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        TextView authorTextView;
        TextView authorTeamTextView;
        TextView datePostedTextView;
        TextView postTeamTextView;
        TextView postTagTextView;
        TextView titleTextView;
        TextView contentTextView;
        CardView cardView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            authorTextView = itemView.findViewById(R.id.post_author);
            authorTeamTextView = itemView.findViewById(R.id.post_author_team);
            datePostedTextView = itemView.findViewById(R.id.post_date_posted);
            postTeamTextView = itemView.findViewById(R.id.post_team);
            postTagTextView = itemView.findViewById(R.id.post_tag);
            titleTextView = itemView.findViewById(R.id.post_title);
            contentTextView = itemView.findViewById(R.id.post_content);

            cardView = (CardView) itemView;
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
