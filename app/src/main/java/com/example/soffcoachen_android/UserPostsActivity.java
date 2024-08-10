package com.example.soffcoachen_android;

import static android.content.ContentValues.TAG;
import static com.example.soffcoachen_android.MainActivity.BASE_URL;
import static com.example.soffcoachen_android.MainActivity.followingList;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soffcoachen_android.adapters.PostAdapter;
import com.example.soffcoachen_android.models.Post;
import com.example.soffcoachen_android.network.ApiService;
import com.example.soffcoachen_android.network.UserPostsApiResponse;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserPostsActivity extends AppCompatActivity implements PostAdapter.PostAdapterCallback {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList = new ArrayList<>();
    private ApiService apiService;
    private TextView userNameTextView;
    private String userName;
    private SharedPreferences sharedPreferences;
    private WebView webView;
    private TextView teamTextView;
    private TextView noOfFollowersTextView;
    private TextView noOfLikesTextView;
    private TextView noOfWrittenComTextView;
    private TextView followButtonTextView;
    private Button goBackButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_posts);

        recyclerView = findViewById(R.id.userPostsRecyclerView);
        webView = findViewById(R.id.webView);

        userNameTextView = findViewById(R.id.userPosts_username);
        teamTextView = findViewById(R.id.userPosts_team);
        noOfFollowersTextView = findViewById(R.id.userPosts_noOfFollowers_val);
        noOfLikesTextView = findViewById(R.id.userPosts_noOfRecLikes_val);
        noOfWrittenComTextView = findViewById(R.id.userPosts_noOfWrittenCom_val);
        followButtonTextView = findViewById(R.id.userPosts_followButton);
        followButtonTextView.setEnabled(isAuth);

        if (followingList != null) {
            for (String username : followingList) {
                if (userNameTextView.getText().equals(username)) {
                    followButtonTextView.setText("Unfollow");
                    break;
                } else {
                    followButtonTextView.setText("Follow");
                }
            }
        }

        followButtonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFollowUserWebView();
            }
        });

        // Get the user name used in this activity from the intent
        Intent intent = getIntent();
        userName = intent.getStringExtra("user_name_to_user_posts");

        // Fetch posts made by the user
        fetchUserPosts();

        // Set the user's name in the TextView
        userNameTextView.setText(userName);

        postAdapter = new PostAdapter(this, this.postList, this.webView, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(postAdapter);

        goBackButton = findViewById(R.id.goBackButton);
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent intent = new Intent(UserPostsActivity.this, MainActivity.class);
                // startActivity(intent);
                finish();
            }
        });
    }

    private void openFollowUserWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new UserPostsActivity.WebAppInterface(this), "Android");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                String script = "(function() {" +
                        "function checkFollowUserSuccess() {" +
                        "    var text = document.body.innerText || document.body.textContent;" +
                        "    try {" +
                        "        var response = JSON.parse(text);" +
                        "        if (response.status === 'follow_user_success') {" +
                        "            Android.onFollowUserSuccess(response.is_following);" +
                        "        } else {" +
                        "            setTimeout(checkFollowUserSuccess, 1000);" +
                        "        }" +
                        "    } catch (e) {" +
                        "        setTimeout(checkFollowUserSuccess, 1000);" +
                        "    }" +
                        "}" +
                        "checkFollowUserSuccess();" +
                        "})();";
                webView.evaluateJavascript(script, null);
            }
        });
        // Load the URL to like the post
        webView.loadUrl(BASE_URL + "follow?username=" + userName);
    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context context) {
            mContext = context;
        }

        @JavascriptInterface
        public void onFollowUserSuccess(boolean isFollowing) {
            // Ensure that the context is an instance of Activity before calling runOnUiThread
            if (mContext instanceof Activity) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (isFollowing) {
                            followingList.add(userName);
                            Toast.makeText(mContext, "Now following!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            followingList.remove(userName);
                            Toast.makeText(mContext, "Unfollowed user", Toast.LENGTH_SHORT).show();
                        }

                        returnToRecyclerView();
                    }
                });
            }
        }
    }

    private void fetchUserPosts() {
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
        Call<UserPostsApiResponse> call = this.apiService.getUserPostsApiResponse(userName);

        call.enqueue(new Callback<UserPostsApiResponse>() {
            @Override
            public void onResponse(Call<UserPostsApiResponse> call, Response<UserPostsApiResponse> response) {
                if (response.isSuccessful()) {
                    UserPostsApiResponse userPostsApiResponse = response.body();
                    if (userPostsApiResponse != null) {
                        resetLists();

                        if (userPostsApiResponse.getPosts() != null) {
                            postList.addAll(userPostsApiResponse.getPosts());
                        }

                        teamTextView.setText(userPostsApiResponse.getTeam());
                        noOfFollowersTextView.setText(userPostsApiResponse.getNoOfFollowers());
                        noOfLikesTextView.setText(userPostsApiResponse.getNoOfRecievedLikes());
                        noOfWrittenComTextView.setText(userPostsApiResponse.getNoOfWrittenComments());

                        // Changing to follow or unfollow depending on the current following-state
                        boolean isFollowing = false;
                        if (followingList != null && !followingList.isEmpty()) {
                            isFollowing = false;
                            String currentUsername = userNameTextView.getText().toString();

                            for (String username : followingList) {
                                if (currentUsername.equals(username)) {
                                    isFollowing = true;
                                    break;
                                }
                            }
                        }
                        followButtonTextView.setText(isFollowing ? "Unfollow" : "Follow");

                        // Notify adapter of data change. Run on main thread.
                        runOnUiThread(() -> postAdapter.notifyDataSetChanged());
                    } else {
                        Log.e(TAG, "fetchUserPosts: Response body is null");
                    }
                } else {
                    Log.e(TAG, "fetchUserPosts: Response not successful: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserPostsApiResponse> call, Throwable t) {
                Log.e(TAG, "fetchUserPosts: Network call failed: " + t.getMessage());
            }
        });
    }

    private void resetLists() {
        postList.clear();
    }

    @Override
    public void returnToRecyclerView() {
        fetchUserPosts();
    }
}
