package com.example.soffcoachen_android.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("home")
    Call<HomeApiResponse> getHomeApiResponse();

    @GET("post")
    Call<PostApiResponse> getPostApiResponse(@Query("post_id") int parameterValue);

    @GET("user")
    Call<UserPostsApiResponse>getUserPostsApiResponse(@Query("username") String parameterValue);
}
