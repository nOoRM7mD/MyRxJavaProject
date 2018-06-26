package com.example.lenovo.myrxjavaproject.app;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.example.lenovo.myrxjavaproject.Network.ApiService;
import com.example.lenovo.myrxjavaproject.utils.PrefUtil;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.IOException;

import javax.inject.Singleton;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by lenovo on 6/22/2018.
 */

public class MyApplication extends Application {
    ApiService apiService;

    @Singleton
    public ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofit(getApplicationContext(), Const.BASE_URL).create(ApiService.class);
        }
        return apiService;
    }

    public Retrofit getRetrofit(Context context, String url) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(provideOkHttpClient(context))
                .build();
        return retrofit;
    }

    /**
     * HttpLoggingInterceptor are added to print
     * the Request / Response in LogCat for debugging purpose.
     * The Authorization header field is added if the API Key is present in Shared Preferences.
     *
     * @param context
     * @return okHttpClient
     */
    private OkHttpClient provideOkHttpClient(final Context context) {
        OkHttpClient okHttpClient = new
                OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        Request.Builder requestBuilder = original.newBuilder()
                                .addHeader("Accept", "application/json")
                                .addHeader("Content-Type", "application/json");
                        if (!TextUtils.isEmpty(PrefUtil.getApiKey(context))) {
                            requestBuilder.header("Authorization", PrefUtil.getApiKey(context));
                        }
                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    }
                }).build();
        return okHttpClient;
    }
}
