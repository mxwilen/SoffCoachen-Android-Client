package com.example.soffcoachen_android;

import static android.content.ContentValues.TAG;

import static com.example.soffcoachen_android.MainActivity.BASE_URL;
import static com.example.soffcoachen_android.MainActivity.isAuth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soffcoachen_android.adapters.CommentsAdapter;
import com.example.soffcoachen_android.models.Comment;
import com.example.soffcoachen_android.models.Post;
import com.example.soffcoachen_android.network.ApiService;
import com.example.soffcoachen_android.network.PostApiResponse;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PostActivity extends AppCompatActivity implements CommentsAdapter.CommentAdapterCallback {

    private Post post;
    private TextView postAuthor;
    private TextView postAuthorTeam;
    private TextView datePosted;
    private TextView postTeam;
    private TextView postTag;
    private TextView postTitle;
    private TextView postContent;
    private TextView postNoOfLikesTextView;

    private RecyclerView commentsRecyclerView;
    private CommentsAdapter commentsAdapter;
    private List<Comment> commentsList = new ArrayList<>();

    private Button commentButton;
    private Button cancelButton;

    private ApiService apiService;
    private WebView webView;
    private int postId;
    private View postItem;
    private Button postLikeButton;
    private View commentLayout;
    private SharedPreferences sharedPreferences;
    private Button goBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        this.postAuthor = findViewById(R.id.post_author);
        this.postAuthorTeam = findViewById(R.id.post_author_team);
        this.datePosted = findViewById(R.id.post_date_posted);
        this.postTeam = findViewById(R.id.post_team);
        this.postTag = findViewById(R.id.post_tag);
        this.postTitle = findViewById(R.id.post_title);
        this.postContent = findViewById(R.id.post_content);
        this.postNoOfLikesTextView = findViewById(R.id.post_no_of_likes);

        // Retrieve the post data from the intent created when clicking on a post. (PostAdapter)
        this.postId = getIntent().getIntExtra("post_id", 0);
        String postAuthor = getIntent().getStringExtra("post_author");
        String postAuthorTeam = getIntent().getStringExtra("post_author_team");
        String datePosted = getIntent().getStringExtra("date_posted");
        String postTeam = getIntent().getStringExtra("post_team");
        String postTag = getIntent().getStringExtra("post_tag");
        String postTitle = getIntent().getStringExtra("post_title");
        String postContent = getIntent().getStringExtra("post_content");
        String postNoOfLikes = getIntent().getStringExtra("post_no_of_likes");

        this.postAuthor.setText(postAuthor);
        this.postAuthorTeam.setText(postAuthorTeam);
        this.datePosted.setText(datePosted);
        this.postTeam.setText(postTeam);
        this.postTag.setText(postTag);
        this.postTitle.setText(postTitle);
        this.postContent.setText(postContent);
        this.postNoOfLikesTextView.setText(postNoOfLikes);

        this.webView = findViewById(R.id.webView);
        this.commentsRecyclerView = findViewById(R.id.comments_recycler_view);

        this.commentsAdapter = new CommentsAdapter(this, this.commentsList, this.webView, this);
        this.commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.commentsRecyclerView.setAdapter(this.commentsAdapter);

        this.postItem = findViewById(R.id.post_item);
        this.postLikeButton = findViewById(R.id.postActivity_postLikeButton);
        this.commentLayout = findViewById(R.id.comment_button_layout);
        this.commentButton = findViewById(R.id.commentButton);
        goBackButton = findViewById(R.id.goBackButton);

        this.postAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostActivity.this, UserPostsActivity.class);
                intent.putExtra("user_name_to_user_posts", post.getAuthor());
                startActivity(intent);
            }
        });

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent intent = new Intent(PostActivity.this, MainActivity.class);
                // startActivity(intent);
                finish();
            }
        });

        configCommentAndLikeButton();
        this.cancelButton = findViewById(R.id.cancelButton);
        this.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentLayout.setVisibility(View.VISIBLE);
                postItem.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                commentsRecyclerView.setVisibility(View.VISIBLE);
            }
        });
        this.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCommentWebView();
            }
        });
        this.postLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { openLikePostWebView(); }
        });
        fetch_post(postId);
    }

    private void openLikePostWebView() {
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.addJavascriptInterface(new PostActivity.WebAppInterface(this), "Android");
        this.webView.setWebViewClient(new WebViewClient() {
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
        this.webView.loadUrl(BASE_URL + "like/post?post_id=" + this.postId);
    }

    private void openCommentWebView() {
        this.webView = findViewById(R.id.webView);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.addJavascriptInterface(new PostActivity.WebAppInterface(this), "Android");
        this.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                String script = "(function() {" +
                        "function checkCommentSuccess() {" +
                        "    var text = document.body.innerText || document.body.textContent;" +
                        "    if (text.indexOf('new_comment_success') !== -1) {" +
                        "        Android.onNewCommentSuccess();" +
                        "    } else if (text.indexOf('error. post is locked.') !== -1) {" +
                        "        Android.onPostLockedError();" +
                        "    } else {" +
                        "        setTimeout(checkCommentSuccess, 1000);" +
                        "    }" +
                        "}" +
                        "checkCommentSuccess();" +
                        "})();";

                webView.evaluateJavascript(script, null);
            }
        });
        this.commentLayout.setVisibility(View.GONE);
        this.postItem.setVisibility(View.GONE);
        this.commentsRecyclerView.setVisibility(View.GONE);
        this.webView.setVisibility(View.VISIBLE);
        this.cancelButton.setVisibility(View.VISIBLE);
        this.webView.loadUrl(BASE_URL + "new_comment?post_id=" + this.postId);
    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context context) {
            mContext = context;
        }

        @JavascriptInterface
        public void onNewCommentSuccess() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "Comment created successfully!", Toast.LENGTH_SHORT).show();
                    cancelButton.setVisibility(View.GONE);
                    returnToRecyclerView();
                }
            });
        }

        @JavascriptInterface
        public void onPostLockedError() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "Comment is locked for supporters only!", Toast.LENGTH_SHORT).show();
                    cancelButton.setVisibility(View.GONE);
                    returnToRecyclerView();
                }
            });
        }

        @JavascriptInterface
        public void onLikePostSuccess() {
            // Ensure that the context is an instance of Activity before calling runOnUiThread
            if (mContext instanceof Activity) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        postNoOfLikesTextView.setText(post.getNoOfLikes());

                        Toast.makeText(mContext, "Liked post!", Toast.LENGTH_SHORT).show();
                        returnToRecyclerView();
                    }
                });
            }
        }
    }

    private void fetch_post(int postId) {
        // Adding logging interceptor to log the network requests
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.NONE);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.apiService = retrofit.create(ApiService.class);
        Call<PostApiResponse> call = this.apiService.getPostApiResponse(postId);

        call.enqueue(new Callback<PostApiResponse>() {
            @Override
            public void onResponse(Call<PostApiResponse> call, Response<PostApiResponse> response) {
                if (response.isSuccessful()) {
                    PostApiResponse postApiResponse = response.body();
                    if (postApiResponse != null) {
                        clearLists();
                        setPost(postApiResponse.getPost());
                        for (Comment comment : postApiResponse.getComments()) {
                            addToCommentList(comment);
                        }
                        // Notify adapter of data change. Run on main thread.
                        runOnUiThread(() -> commentsAdapter.notifyDataSetChanged());
                    } else {
                        Log.e(TAG, "fetch_post: Response body is null");
                    }
                } else {
                    Log.e(TAG, "fetch_post: Response not successful: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PostApiResponse> call, Throwable t) {
                Log.e(TAG, "fetch_post: Network call failed: " + t.getMessage());
            }
        });
    }

    public void addToCommentList(Comment comment) {
        this.commentsList.add(comment);
    }

    private void setPost(Post post) {
        this.post = post;
    }

    private void clearLists() {
        this.commentsList.clear();
    }

    public void returnToRecyclerView() {
        configCommentAndLikeButton();
        fetch_post(this.postId);

        this.postItem.setVisibility(View.VISIBLE);
        this.commentLayout.setVisibility(View.VISIBLE);
        this.webView.setVisibility(View.GONE);
        this.commentsRecyclerView.setVisibility(View.VISIBLE);
        this.webView.getSettings().setJavaScriptEnabled(false);
    }

    private void configCommentAndLikeButton() {
        this.commentButton.setEnabled(isAuth);
        this.postLikeButton.setEnabled(isAuth);
    }
}

