package com.example.soffcoachen_android;

import static android.content.ContentValues.TAG;

import static com.example.soffcoachen_android.MainActivity.BASE_URL;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soffcoachen_android.adapters.CommentsAdapter;
import com.example.soffcoachen_android.adapters.PostAdapter;
import com.example.soffcoachen_android.models.Comment;
import com.example.soffcoachen_android.models.Post;
import com.example.soffcoachen_android.models.Team;
import com.example.soffcoachen_android.network.ApiService;
import com.example.soffcoachen_android.network.HomeApiResponse;
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

public class PostActivity extends AppCompatActivity {

    private TextView postAuthor;
    private TextView postAuthorTeam;
    private TextView datePosted;
    private TextView postTeam;
    private TextView postTag;
    private TextView postTitle;
    private TextView postContent;
    private TextView postNoOfLikes;

    private RecyclerView commentsRecyclerView;
    private CommentsAdapter commentsAdapter;
    private List<Comment> commentsList = new ArrayList<>();

    private ApiService apiService;
    private WebView webView;

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
        this.postNoOfLikes = findViewById(R.id.post_no_of_likes);

        // Retrieve the post data from the intent created when clicking on a post. (PostAdapter)
        int postId = getIntent().getIntExtra("post_id", 0);
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
        this.postNoOfLikes.setText(postNoOfLikes);


        fetch_post(postId);
        this.webView = findViewById(R.id.webView);
        this.commentsRecyclerView = findViewById(R.id.comments_recycler_view);

        this.commentsAdapter = new CommentsAdapter(this, this.commentsList, this.webView);
        this.commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.commentsRecyclerView.setAdapter(this.commentsAdapter);
    }

    private void fetch_post(int postId) {
        // Adding logging interceptor to log the network requests
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

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
                        for (Comment comment : postApiResponse.getComments()) {
                            addToCommentList(comment);
                        }
                        commentsAdapter.notifyDataSetChanged();
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
}

