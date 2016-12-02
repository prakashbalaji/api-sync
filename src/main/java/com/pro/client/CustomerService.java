package com.pro.client;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CustomerService {
    @GET("customers/{customerId}")
    Call<ResponseBody> get(@Path("customerId") String customerId);
}
