package com.pro;

import com.pro.client.CustomerService;
import com.pro.client.ProviderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;

@Configuration
public class RetrofitConfiguration {

    private String baseUrl = "http://localhost:8089/";

    @Bean
    public CustomerService customerService(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .build();

        return retrofit.create(CustomerService.class);
    }

    @Bean
    public ProviderService providerService(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .build();

        return retrofit.create(ProviderService.class);
    }

}