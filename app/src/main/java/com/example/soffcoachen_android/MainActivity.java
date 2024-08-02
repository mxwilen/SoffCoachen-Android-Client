package com.example.soffcoachen_android;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soffcoachen_android.adapters.PostAdapter;
import com.example.soffcoachen_android.network.ApiResponse;
import com.example.soffcoachen_android.models.Post;
import com.example.soffcoachen_android.models.Team;
import com.example.soffcoachen_android.network.ApiService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {
    private static final String BASE_URL = "http://192.168.0.13:8000/api/";
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private ApiService apiService;
    private List<Post> postList = new ArrayList<>();
    private List<Team> teamList = new ArrayList<>();
    private WebView webView;

    private Button loginButton;
    private Button logoutButton;
    private Button newPostButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initScreenConfig();

        this.recyclerView = findViewById(R.id.recyclerView_posts);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.postAdapter = new PostAdapter(this.postList);
        this.recyclerView.setAdapter(this.postAdapter);

        // Fetch home page posts from API.
        fetch_home();

        this.loginButton = findViewById(R.id.loginButton);
        this.logoutButton = findViewById(R.id.logoutButton);
        this.newPostButton = findViewById(R.id.newPostButton);
        this.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginWebView();
            }
        });
        this.newPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewPostWebView();
            }
        });
    }

    private void openLoginWebView() {
        this.webView = findViewById(R.id.login_webView);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        this.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                String script = "(function() {" +
                        "function checkLoginSuccess() {" +
                        "    var text = document.body.innerText || document.body.textContent;" +
                        "    if (text.indexOf('login_success') !== -1) {" +
                        "        Android.onLoginSuccess();" +
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
        this.webView.setVisibility(View.VISIBLE);
        this.webView.loadUrl("http://192.168.0.13:8000/api/login");
    }

    private void openNewPostWebView() {
        this.webView = findViewById(R.id.login_webView);
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
        this.webView.setVisibility(View.VISIBLE);
        this.webView.loadUrl("http://192.168.0.13:8000/api/new_post");
    }

    private void returnToRecyclerView() {
        this.webView.setVisibility(View.GONE);
        this.recyclerView.setVisibility(View.VISIBLE);
        this.webView.getSettings().setJavaScriptEnabled(false);

        fetch_home();
    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context context) {
            mContext = context;
        }

        @JavascriptInterface
        public void onLoginSuccess() {
            // Close the WebView or handle the event
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loginButton.setVisibility(View.GONE);
                    logoutButton.setVisibility(View.GONE);
                    newPostButton.setVisibility(View.VISIBLE);
                    returnToRecyclerView();
                }
            });
        }

        @JavascriptInterface
        public void onNewPostSuccess() {
            // Close the WebView or handle the event
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    returnToRecyclerView();
                }
            });
        }
    }




    private void fetch_home() {
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
        Call<ApiResponse> call = this.apiService.getApiResponse();

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse != null) {
                        for (Team team : apiResponse.getTeams()) {
                            addToTeamList(team);
                        }
                        for (Post post : apiResponse.getPosts()) {
                            addToPostList(post);
                        }
                        postAdapter.notifyDataSetChanged();
                    } else {
                        Log.e(TAG, "Response body is null");
                    }
                } else {
                    Log.e(TAG, "Response not successful: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e(TAG, "Network call failed: " + t.getMessage());
            }
        });
    }

    private void initScreenConfig() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void addToPostList(Post post) {
        this.postList.add(post);
    }

    public void addToTeamList(Team team) {
        this.teamList.add(team);
    }
}