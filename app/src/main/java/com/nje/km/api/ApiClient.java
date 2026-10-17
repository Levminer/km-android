package com.nje.km.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    private static final String BASE_URL = "https://menetrend.kecskemet.hu/api/api/";

    private static KmApi api;

    private ApiClient() {
    }

    public static synchronized KmApi get() {
        if (api == null) {
            api = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(KmApi.class);
        }
        return api;
    }
}
