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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.soffcoachen_android.R;
import com.example.soffcoachen_android.UserPostsActivity;
import com.example.soffcoachen_android.models.Comment;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


/*
    Controls each comment card in the comment recyclerview.
    Data for a specific is changed after retrieving the position of that card in the recyclerview.
*/
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private Context context;
    private List<Comment> commentsList;
    private WebView webView;
    private CommentAdapterCallback callback;

    public interface CommentAdapterCallback {
        void returnToRecyclerView();
    }

    public CommentsAdapter(Context context, List<Comment> commentsList, WebView webView, CommentAdapterCallback callback) {
        this.context = context;
        this.commentsList = commentsList;
        this.webView = webView;
        this.callback = callback;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentsList.get(position);
        holder.commentAuthor.setText(comment.getAuthor());
        holder.commentAuthorTeam.setText(comment.getAuthorTeam());
        holder.dateCommented.setText(comment.getDateCommented());
        holder.commentContent.setText(comment.getContent());
        holder.commentNoOfLikes.setText(comment.getNoOfLikes());

        // Check current user status and update likeButton state
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String currentUser = sharedPreferences.getString("current_user", null);
        holder.commentLikeButton.setEnabled(isAuth);

        holder.commentAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserPostsActivity.class);
                intent.putExtra("user_name", comment.getAuthor());
                context.startActivity(intent);
            }
        });

        holder.commentLikeButton.setOnClickListener(new View.OnClickListener() {

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
                                "function checkLikeCommentSuccess() {" +
                                "    var text = document.body.innerText || document.body.textContent;" +
                                "    if (text.indexOf('like_comment_success') !== -1) {" +
                                "        Android.onLikeCommentSuccess();" +
                                "    } else {" +
                                "        setTimeout(checkLikeCommentSuccess, 1000);" +
                                "    }" +
                                "}" +
                                "checkLikeCommentSuccess();" +
                                "})();";
                        webView.evaluateJavascript(script, null);
                    }
                });
                webView.loadUrl(BASE_URL + "like/comment?comment_id=" + comment.getId());
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
        public void onLikeCommentSuccess() {
            // Ensure that the context is an instance of Activity before calling runOnUiThread
            if (mContext instanceof Activity) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "Liked comment!", Toast.LENGTH_SHORT).show();
                        callback.returnToRecyclerView();
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        public TextView commentAuthor;
        public TextView commentAuthorTeam;
        public TextView dateCommented;
        public TextView commentContent;
        public TextView commentNoOfLikes;
        Button commentLikeButton;

        public CommentViewHolder(View itemView) {
            super(itemView);
            commentAuthor = itemView.findViewById(R.id.comment_author);
            commentAuthorTeam = itemView.findViewById(R.id.comment_author_team);
            dateCommented = itemView.findViewById(R.id.date_commented);
            commentContent = itemView.findViewById(R.id.comment_content);
            commentNoOfLikes = itemView.findViewById(R.id.comment_no_of_likes);

            commentLikeButton = itemView.findViewById(R.id.commentLikeButton);
            SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            String currentUser = sharedPreferences.getString("current_user", null);

            commentLikeButton.setEnabled(isAuth);
        }
    }
}

