package com.example.soffcoachen_android.adapters;

import static com.example.soffcoachen_android.MainActivity.BASE_URL;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soffcoachen_android.PostActivity;
import com.example.soffcoachen_android.R;
import com.example.soffcoachen_android.models.Post;
import com.example.soffcoachen_android.network.ApiService;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private final List<Post> postList;
    private OnItemClickListener listener;
    private ApiService apiService;
    private WebView webView;
    private PostAdapterCallback callback;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface PostAdapterCallback {
        void returnToRecyclerView();
    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public PostAdapter(Context context, List<Post> postList, WebView webView, PostAdapterCallback callback) {
        this.context = context;
        this.postList = postList;
        this.webView = webView;
        this.callback = callback;
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
        holder.noOfLikesTextView.setText(post.getNoOfLikes());
        holder.postTeamTextView.setText(post.getPostTeam());
        holder.postTagTextView.setText(post.getPostTag());
        holder.titleTextView.setText(post.getTitle());
        holder.contentTextView.setText(post.getContent());

        holder.likeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                callback.returnToRecyclerView();
                webView.getSettings().setJavaScriptEnabled(true);
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        String script = "(function() {" +
                                "function checkLikePostSuccess() {" +
                                "    var text = document.body.innerText || document.body.textContent;" +
                                "    if (text.indexOf('like_post_success') !== -1) {" +
                                "        Android.onLikePostSuccess();" +
                                "    } else {" +
                                "        setTimeout(checkLikePostSuccess, 1000);" +
                                "    }" +
                                "}" +
                                "checkLikePostSuccess();" +
                                "})();";
                        webView.evaluateJavascript(script, null);
                    }
                });
                webView.loadUrl(BASE_URL + "like/post?post_id=" + post.getPostId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        TextView authorTextView;
        TextView authorTeamTextView;
        TextView datePostedTextView;
        TextView noOfLikesTextView;
        TextView postTeamTextView;
        TextView postTagTextView;
        TextView titleTextView;
        TextView contentTextView;
        Button likeButton;
        CardView cardView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            authorTextView = itemView.findViewById(R.id.post_author);
            authorTeamTextView = itemView.findViewById(R.id.post_author_team);
            datePostedTextView = itemView.findViewById(R.id.post_date_posted);
            noOfLikesTextView = itemView.findViewById(R.id.post_no_of_likes);
            postTeamTextView = itemView.findViewById(R.id.post_team);
            postTagTextView = itemView.findViewById(R.id.post_tag);
            titleTextView = itemView.findViewById(R.id.post_title);
            contentTextView = itemView.findViewById(R.id.post_content);

            likeButton = itemView.findViewById(R.id.likeButton);

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
                    Post post = postList.get(getAdapterPosition());
                    Intent intent = new Intent(context, PostActivity.class);
                    intent.putExtra("post_id", post.getPostId());
                    intent.putExtra("post_author", post.getAuthor());
                    intent.putExtra("post_author_team", post.getUserTeam());
                    intent.putExtra("date_posted", post.getDatePosted());

                    intent.putExtra("post_team", post.getPostTeam());
                    intent.putExtra("post_tag", post.getPostTag());

                    intent.putExtra("post_title", post.getTitle());
                    intent.putExtra("post_content", post.getContent());
                    intent.putExtra("post_no_of_likes", post.getNoOfLikes());
                    context.startActivity(intent);
                }
            });
        }
    }
}
