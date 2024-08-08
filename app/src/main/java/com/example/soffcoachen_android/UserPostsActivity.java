package com.example.soffcoachen_android;

import static com.example.soffcoachen_android.MainActivity.BASE_URL;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soffcoachen_android.adapters.PostAdapter;
import com.example.soffcoachen_android.models.Post;
import com.example.soffcoachen_android.network.ApiService;
import com.example.soffcoachen_android.network.HomeApiResponse;
import com.example.soffcoachen_android.network.RetrofitClient;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_posts);

        // Initialize views
        recyclerView = findViewById(R.id.userPostsRecyclerView);
        userNameTextView = findViewById(R.id.user_info);

        // Get the user name from the intent
        Intent intent = getIntent();
        userName = intent.getStringExtra("user_name");

        // Fetch posts made by the user
        fetchUserPosts();

        // Set the user's name in the TextView
        userNameTextView.setText(userName);

        // Initialize the RecyclerView
        webView = findViewById(R.id.webView);

        postAdapter = new PostAdapter(this, this.postList, this.webView, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(postAdapter);
    }

    private void fetchUserPosts() {
        /*
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
        Call<UserPostsApiResponse> call = this.apiService.getUserPostsApiResponse();



        RetrofitClient retrofitClient = new RetrofitClient();
        apiService = retrofitClient.getApiService();

        Call<List<Post>> call = apiService.getPostsByUser(userName);
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (response.isSuccessful()) {
                    List<Post> posts = response.body();
                    if (posts != null) {
                        postList.clear();
                        postList.addAll(posts);
                        postAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(UserPostsActivity.this, "Failed to load posts", Toast.LENGTH_SHORT).show();
                    Log.e("UserPostsActivity", "Response not successful: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Toast.makeText(UserPostsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                Log.e("UserPostsActivity", "Network error: " + t.getMessage());
            }
        });
         */
    }

    @Override
    public void returnToRecyclerView() {
        fetchUserPosts();
    }
}
