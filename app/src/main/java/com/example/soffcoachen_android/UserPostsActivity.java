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

        this.recyclerView = findViewById(R.id.userPostsRecyclerView);
        this.webView = findViewById(R.id.webView);
        this.userNameTextView = findViewById(R.id.userPosts_username);
        this.teamTextView = findViewById(R.id.userPosts_team);
        this.noOfFollowersTextView = findViewById(R.id.userPosts_noOfFollowers_val);
        this.noOfLikesTextView = findViewById(R.id.userPosts_noOfRecLikes_val);
        this.noOfWrittenComTextView = findViewById(R.id.userPosts_noOfWrittenCom_val);
        this.followButtonTextView = findViewById(R.id.userPosts_followButton);
        this.followButtonTextView.setEnabled(isAuth);

        // Displays the "follow" or "unfollow" on the action button depending on the current user has that user in its following list (gathered from a previous activity).
        if (followingList != null) {
            for (String username : followingList) {
                if (this.userNameTextView.getText().equals(username)) {
                    this.followButtonTextView.setText("Unfollow");
                    break;
                } else {
                    this.followButtonTextView.setText("Follow");
                }
            }
        }

        this.followButtonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFollowUserWebView();
            }
        });

        // Get the user name used in this activity from the intent
        Intent intent = getIntent();
        this.userName = intent.getStringExtra("user_name_to_user_posts");

        // Fetch posts made by the user
        fetchUserPosts();

        // Set the users name in the TextView
        this.userNameTextView.setText(userName);

        this.postAdapter = new PostAdapter(this, this.postList, this.webView, this);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.recyclerView.setAdapter(postAdapter);

        this.goBackButton = findViewById(R.id.goBackButton);
        this.goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Exit the userPostActivity and go back the the activity that led me here.
                finish();
            }
        });
    }

    /*
        All the "open*WebView() functions work in the same way:
            - opens a hidden webview which fetches data from the API with the specified route and its needed parameters.
            - the webview reads the response and looks for a success message.
            - on success it calls the appropriate "on-success-response" function (see later in this file)
    */
    private void openFollowUserWebView() {
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.addJavascriptInterface(new UserPostsActivity.WebAppInterface(this), "Android");
        this.webView.setWebViewClient(new WebViewClient() {
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
        this.webView.loadUrl(BASE_URL + "follow?username=" + userName);
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

    // Fetches data for a users page. (its posts and metadata)
    private void fetchUserPosts() {
        // Adding logging interceptor to log the network requests
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        // Setup to fetch and store the API response.
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
