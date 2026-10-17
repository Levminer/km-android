package com.nje.km.api;

import com.nje.km.model.Line;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface KmApi {

    @GET("tracks/{date}")
    Call<List<Line>> getLines(@Path("date") String date);
}
