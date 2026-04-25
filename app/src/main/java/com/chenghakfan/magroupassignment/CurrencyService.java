package com.chenghakfan.magroupassignment;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CurrencyService {
    @GET("v6/latest/{base}")
    Call<CurrencyResponse> getLatestRates(@Path("base") String base);
}
