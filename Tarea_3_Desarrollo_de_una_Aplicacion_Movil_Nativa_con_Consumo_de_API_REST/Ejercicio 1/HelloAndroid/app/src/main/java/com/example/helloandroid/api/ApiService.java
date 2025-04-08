package com.example.helloandroid.api;

import com.example.helloandroid.model.HelloResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("api/hello")
    Call<HelloResponse> getHello();
}
