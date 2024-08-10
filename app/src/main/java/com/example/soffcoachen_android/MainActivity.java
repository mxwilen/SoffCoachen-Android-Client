package com.example.soffcoachen_android;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soffcoachen_android.adapters.PostAdapter;
import com.example.soffcoachen_android.models.Comment;
import com.example.soffcoachen_android.network.HomeApiResponse;
import com.example.soffcoachen_android.models.Post;
import com.example.soffcoachen_android.models.Team;
import com.example.soffcoachen_android.network.ApiService;

import org.json.JSONArray;
import org.json.JSONException;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity implements PostAdapter.PostAdapterCallback {
    public static final String BASE_URL = "http://10.0.2.2:8000/api/";
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private ApiService apiService;
    private List<Post> postList = new ArrayList<>();
    private List<Team> teamList = new ArrayList<>();
    private List<Comment> commentList = new ArrayList<>();
    private WebView webView;
    private Toolbar toolbar;
    private Button cancelButton;
    private TextView likeCount;
    private Button loginButton;
    private Button logoutButton;
    private Button registerButton;
    private Button newPostButton;
    private Button followingFeedButton;
    private Button homeFeedButton;
    private Button currentUserButton;
    private View loginRegisterLayout;
    private View loggedInLayout;
    private View welcomeText_layout;
    private SharedPreferences sharedPreferences;
    public static List<String> followingList = new ArrayList<>();
    private List<Post> followingPosts = new ArrayList<>();

    public static boolean isAuth = false;
    public static String curUsr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.recyclerView = findViewById(R.id.recyclerView_posts);
        this.webView = findViewById(R.id.webView);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.postAdapter = new PostAdapter(this, this.postList, this.webView, this);
        this.recyclerView.setAdapter(this.postAdapter);

        // Fetch home page posts from API.
        fetch_home();

        welcomeText_layout = findViewById(R.id.welcomeText_layout);
        this.toolbar = findViewById(R.id.toolbar);
        this.cancelButton = findViewById(R.id.cancelButton);
        this.loginButton = findViewById(R.id.loginButton);
        this.logoutButton = findViewById(R.id.logoutButton);
        this.registerButton = findViewById(R.id.registerButton);
        this.newPostButton = findViewById(R.id.newPostButton);
        followingFeedButton = findViewById(R.id.followingFeedButton);
        homeFeedButton = findViewById(R.id.homeFeedButton);
        followingFeedButton.setEnabled(isAuth);
        homeFeedButton.setEnabled(isAuth);

        loginRegisterLayout = findViewById(R.id.loginRegisterLayout);
        loggedInLayout = findViewById(R.id.loggedInLayout);

        if (isAuth) {
            loginRegisterLayout.setVisibility(View.GONE);
            loggedInLayout.setVisibility(View.VISIBLE);
            setCurrentUser(curUsr);
        } else {
            loginRegisterLayout.setVisibility(View.VISIBLE);
            loggedInLayout.setVisibility(View.GONE);
        }

        this.likeCount = findViewById(R.id.post_no_of_likes);
        this.currentUserButton = findViewById(R.id.currentUserButton);
        this.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.VISIBLE);
            }
        });
        this.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginWebView();
            }
        });
        this.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLogoutWebView();
            }
        });
        this.registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRegisterWebView();
            }
        });
        this.newPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewPostWebView();
            }
        });
        this.currentUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAccountWebView();
            }
        });
        this.followingFeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followingPosts.clear();
                for (Post post : postList) {
                    if (followingList.contains(post.getAuthor())) {
                        followingPosts.add(post);
                        // Log.d(TAG, "loppis: " + post.getContent());
                    }
                }
                postAdapter.setPostList(followingPosts);
                postAdapter.notifyDataSetChanged();

                homeFeedButton.setVisibility(View.VISIBLE);
                followingFeedButton.setVisibility(View.GONE);
            }
        });
        this.homeFeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postAdapter.setPostList(postList);
                postAdapter.notifyDataSetChanged();

                homeFeedButton.setVisibility(View.GONE);
                followingFeedButton.setVisibility(View.VISIBLE);
            }
        });

        this.postAdapter.setOnItemClickListener(new PostAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Post post = postList.get(position);
            }
        });
    }

    private void openLoginWebView() {
        this.webView = findViewById(R.id.webView);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        this.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                String script = "(function() {" +
                        "function checkLoginSuccess() {" +
                        "    var text = document.body.innerText || document.body.textContent;" +
                        "    try {" +
                        "        var response = JSON.parse(text);" +
                        "        if (response.status === 'login_success') {" +
                        "            Android.onLoginSuccess(response.current_user, JSON.stringify(response.following_list));" +
                        "        } else {" +
                        "            setTimeout(checkLoginSuccess, 1000);" +
                        "        }" +
                        "    } catch (e) {" +
                        "        setTimeout(checkLoginSuccess, 1000);" +
                        "    }" +
                        "}" +
                        "checkLoginSuccess();" +
                        "})();";
                webView.evaluateJavascript(script, null);
            }
        });

        this.recyclerView.setVisibility(View.GONE);
        this.toolbar.setVisibility(View.GONE);
        this.webView.setVisibility(View.VISIBLE);
        this.cancelButton.setVisibility(View.VISIBLE);
        this.webView.loadUrl(BASE_URL + "login");
    }

    private void openLogoutWebView() {
        this.webView = findViewById(R.id.webView);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        this.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                String script = "(function() {" +
                        "function checkLogoutSuccess() {" +
                        "    var text = document.body.innerText || document.body.textContent;" +
                        "    if (text.indexOf('logout_success') !== -1) {" +
                        "        Android.onLogoutSuccess();" +
                        "    } else {" +
                        "        setTimeout(checkLogoutSuccess, 1000);" +
                        "    }" +
                        "}" +
                        "checkLogoutSuccess();" +
                        "})();";
                webView.evaluateJavascript(script, null);
            }
        });
        this.webView.loadUrl(BASE_URL + "logout");
    }

    private void openRegisterWebView() {
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        this.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                String script = "(function() {" +
                        "function checkRegisterSuccess() {" +
                        "    var text = document.body.innerText || document.body.textContent;" +
                        "    try {" +
                        "        var response = JSON.parse(text);" +
                        "        if (response.status === 'register_success') {" +
                        "            Android.onRegisterSuccess(response.current_user, JSON.stringify(response.following_list));" +
                        "        } else {" +
                        "            setTimeout(checkRegisterSuccess, 1000);" +
                        "        }" +
                        "    } catch (e) {" +
                        "        setTimeout(checkRegisterSuccess, 1000);" +
                        "    }" +
                        "}" +
                        "checkRegisterSuccess();" +
                        "})();";
                webView.evaluateJavascript(script, null);
            }
        });
        this.recyclerView.setVisibility(View.GONE);
        this.toolbar.setVisibility(View.GONE);
        this.webView.setVisibility(View.VISIBLE);
        this.cancelButton.setVisibility(View.VISIBLE);
        this.webView.loadUrl(BASE_URL + "register");
    }

    private void openNewPostWebView() {
        this.webView = findViewById(R.id.webView);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        this.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                String script = "(function() {" +
                        "function checkLoginSuccess() {" +
                        "    var text = document.body.innerText || document.body.textContent;" +
                        "    if (text.indexOf('new_post_success') !== -1) {" +
                        "        Android.onNewPostSuccess();" +
                        "    } else {" +
                        "        setTimeout(checkLoginSuccess, 1000);" +
                        "    }" +
                        "}" +
                        "checkLoginSuccess();" +
                        "})();";
                webView.evaluateJavascript(script, null);
            }
        });

        this.recyclerView.setVisibility(View.GONE);
        this.toolbar.setVisibility(View.GONE);
        this.webView.setVisibility(View.VISIBLE);
        this.cancelButton.setVisibility(View.VISIBLE);
        this.webView.loadUrl(BASE_URL + "new_post");
    }

    private void openAccountWebView() {
        this.webView = findViewById(R.id.webView);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        this.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                String script = "(function() {" +
                        "function checkAccountSuccess() {" +
                        "    var text = document.body.innerText || document.body.textContent;" +
                        "    try {" +
                        "        var response = JSON.parse(text);" +
                        "        if (response.status === 'account_update_success') {" +
                        "            Android.onAccountSuccess(response.current_user);" +
                        "        } else {" +
                        "            setTimeout(checkAccountSuccess, 1000);" +
                        "        }" +
                        "    } catch (e) {" +
                        "        setTimeout(checkAccountSuccess, 1000);" +
                        "    }" +
                        "}" +
                        "checkAccountSuccess();" +
                        "})();";
                webView.evaluateJavascript(script, null);
            }
        });

        this.recyclerView.setVisibility(View.GONE);
        this.toolbar.setVisibility(View.GONE);
        this.webView.setVisibility(View.VISIBLE);
        this.cancelButton.setVisibility(View.VISIBLE);
        this.webView.loadUrl(BASE_URL + "account");
    }

    public void returnToRecyclerView() {
        fetch_home();

        this.webView.setVisibility(View.GONE);
        this.cancelButton.setVisibility(View.GONE);
        this.recyclerView.setVisibility(View.VISIBLE);
        this.toolbar.setVisibility(View.VISIBLE);
        this.webView.getSettings().setJavaScriptEnabled(false);
    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context context) {
            mContext = context;
        }

        @JavascriptInterface
        public void onLoginSuccess(String currentUser, String following_list_json) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setCurrentUser(currentUser);

                    // Parse the JSON string into a List<String>
                    try {
                        JSONArray jsonArray = new JSONArray(following_list_json);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            followingList.add(jsonArray.getString(i));
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    loginRegisterLayout.setVisibility(View.GONE);
                    loggedInLayout.setVisibility(View.VISIBLE);

                    followingFeedButton.setEnabled(isAuth);
                    homeFeedButton.setEnabled(isAuth);

                    Toast.makeText(MainActivity.this, "Successfully logged in. Welcome back!", Toast.LENGTH_SHORT).show();
                    returnToRecyclerView();
                }
            });
        }

        @JavascriptInterface
        public void onLogoutSuccess() {
            if (mContext instanceof Activity) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setCurrentUser("");

                        followingList.clear();

                        loginRegisterLayout.setVisibility(View.VISIBLE);
                        loggedInLayout.setVisibility(View.GONE);

                        followingFeedButton.setEnabled(isAuth);
                        homeFeedButton.setEnabled(isAuth);

                        Toast.makeText(MainActivity.this, "Successfully logged out!", Toast.LENGTH_SHORT).show();
                        returnToRecyclerView();
                    }
                });
            }
        }

        @JavascriptInterface
        public void onRegisterSuccess(String currentUser, String following_list_json) {
            if (mContext instanceof Activity) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setCurrentUser(currentUser);

                        // Parse the JSON string into a List<String>
                        try {
                            JSONArray jsonArray = new JSONArray(following_list_json);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                followingList.add(jsonArray.getString(i));
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        loginRegisterLayout.setVisibility(View.GONE);
                        loggedInLayout.setVisibility(View.VISIBLE);

                        followingFeedButton.setEnabled(isAuth);
                        homeFeedButton.setEnabled(isAuth);

                        Toast.makeText(MainActivity.this, "Successfully created account. You are logged in!", Toast.LENGTH_LONG).show();
                        returnToRecyclerView();
                    }
                });
            }
        }

        @JavascriptInterface
        public void onNewPostSuccess() {
            // Close the WebView or handle the event
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Post created successfully!", Toast.LENGTH_SHORT).show();
                    returnToRecyclerView();
                }
            });
        }

        @JavascriptInterface
        public void onAccountSuccess(String currentUser) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setCurrentUser(currentUser);
                    Toast.makeText(MainActivity.this, "Account update successfully!", Toast.LENGTH_SHORT).show();
                    returnToRecyclerView();
                }
            });
        }
    }


    private void fetch_home() {
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
        Call<HomeApiResponse> call = this.apiService.getHomeApiResponse();

        call.enqueue(new Callback<HomeApiResponse>() {
            @Override
            public void onResponse(Call<HomeApiResponse> call, Response<HomeApiResponse> response) {
                if (response.isSuccessful()) {
                    HomeApiResponse homeApiResponse = response.body();
                    if (homeApiResponse != null) {
                        resetLists();

                        // Safely update the lists
                        if (homeApiResponse.getTeams() != null) {
                            teamList.addAll(homeApiResponse.getTeams());
                        }
                        if (homeApiResponse.getPosts() != null) {
                            postList.addAll(homeApiResponse.getPosts());
                        }

                        // Notify adapter of data change. Run on main thread.
                        runOnUiThread(() -> postAdapter.notifyDataSetChanged());
                    } else {
                        Log.e(TAG, "fetch_home: Response body is null");
                    }
                } else {
                    Log.e(TAG, "fetch_home: Response not successful: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<HomeApiResponse> call, Throwable t) {
                Log.e(TAG, "fetch_home: Network call failed: " + t.getMessage());
            }
        });
    }

    private void resetLists() {
        this.postList.clear();
        this.teamList.clear();
        this.commentList.clear();
    }

    public void addToPostList(Post post) {
        this.postList.add(post);
    }

    public void addToTeamList(Team team) {
        this.teamList.add(team);
    }
    public void addToCommentList(Comment comment) {
        this.commentList.add(comment);
    }

    private void setCurrentUser(String currentUser) {
        this.sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putString("current_user", currentUser);
        editor.apply();

        if (currentUser.equals("")) {
            isAuth = false;
            welcomeText_layout.setVisibility(View.VISIBLE);
        } else {
            welcomeText_layout.setVisibility(View.GONE);
            isAuth = true;
        }
        curUsr = currentUser;

        this.currentUserButton.setText(currentUser);
    }
}