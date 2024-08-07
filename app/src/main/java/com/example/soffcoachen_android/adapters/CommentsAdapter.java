package com.example.soffcoachen_android.adapters;

import static com.example.soffcoachen_android.MainActivity.BASE_URL;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.example.soffcoachen_android.R;
import com.example.soffcoachen_android.models.Comment;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

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

        holder.commentLikeButton.setOnClickListener(new View.OnClickListener() {

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
                webView.loadUrl(BASE_URL + "like/comment?comment_id=" + comment.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
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
        }
    }
}

