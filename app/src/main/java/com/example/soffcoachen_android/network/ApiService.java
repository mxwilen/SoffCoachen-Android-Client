package com.example.soffcoachen_android.network;

import com.example.soffcoachen_android.models.ApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("home")
    Call<ApiResponse> getApiResponse();
}
