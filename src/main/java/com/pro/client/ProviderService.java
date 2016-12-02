package com.pro.client;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ProviderService {
    @GET("providers/{providerId}")
    Call<ResponseBody> get(@Path("providerId") String providerId);
}
