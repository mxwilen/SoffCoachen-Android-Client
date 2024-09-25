package com.example.soffcoachen_android.adapters;

import static android.content.ContentValues.TAG;
import static com.example.soffcoachen_android.MainActivity.BASE_URL;
import static com.example.soffcoachen_android.MainActivity.isAuth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soffcoachen_android.MainActivity;
import com.example.soffcoachen_android.PostActivity;
import com.example.soffcoachen_android.R;
import com.example.soffcoachen_android.UserPostsActivity;
import com.example.soffcoachen_android.models.Post;
import com.example.soffcoachen_android.network.ApiService;

import java.util.List;


/*
    Controls each comment card in the comment recyclerview.
    Data for a specific is changed after retrieving the position of that card in the recyclerview.
*/
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<Post> postList;
    private OnItemClickListener listener;
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

    public void setPostList(List<Post> newPostList) {
        this.postList = newPostList;
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

        holder.authorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserPostsActivity.class);
                intent.putExtra("user_name_to_user_posts", post.getAuthor());
                context.startActivity(intent);
            }
        });

        holder.likeButton.setEnabled(isAuth);
        holder.likeButton.setOnClickListener(new View.OnClickListener() {

            /*
                Same as the "open*WebView()" functions found in the other files:
                    - opens a hidden webview which fetches data from the API with the specified route and its needed parameters.
                    - the webview reads the response and looks for a success message.
                    - on success it calls the appropriate "on-success-response" function (see later in this file)
            */
            @Override
            public void onClick(View v) {
                webView.getSettings().setJavaScriptEnabled(true);
                webView.addJavascriptInterface(new WebAppInterface(context), "Android");
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
                // Load the URL to like the post
                webView.loadUrl(BASE_URL + "like/post?post_id=" + post.getPostId());
            }
        });
    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context context) {
            mContext = context;
        }

        /*
           All "on*Success" work more or less in the same way.
            - gets called when a success response is fetched from the API.
            - parses the data gathered and stores it locally.
            - then updates the activity accordingly.
            - lastly calls returnToRecyclerView which closes the webview and restores the activity.
        */
        @JavascriptInterface
        public void onLikePostSuccess() {
            // Ensure that the context is an instance of Activity before calling runOnUiThread
            if (mContext instanceof Activity) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "Liked post!", Toast.LENGTH_SHORT).show();
                        callback.returnToRecyclerView();
                    }
                });
            }
        }
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

            likeButton = itemView.findViewById(R.id.postActivity_postLikeButton);
            likeButton.setEnabled(isAuth);

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
