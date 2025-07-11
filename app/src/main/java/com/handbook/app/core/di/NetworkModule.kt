package com.handbook.app.core.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.handbook.app.BuildConfig
import com.handbook.app.common.util.gson.StringConverter
import com.handbook.app.core.Env
import com.handbook.app.core.envForConfig
import com.handbook.app.core.net.AndroidHeaderInterceptor
import com.handbook.app.core.net.ForbiddenInterceptor
import com.handbook.app.core.net.GuestUserInterceptor
import com.handbook.app.core.net.JwtInterceptor
import com.handbook.app.core.net.PlatformInterceptor
import com.handbook.app.core.net.UserAgentInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun okHttpCallFactory(): Call.Factory = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor()
                .apply {
                    if (BuildConfig.DEBUG) {
                        setLevel(HttpLoggingInterceptor.Level.BODY)
                    }
                },
        )
        .build()

    @Singleton
    @Provides
    fun provideOkhttpClient(): OkHttpClient {
        val okHttpClientBuilder = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.MINUTES)

        okHttpClientBuilder.addInterceptor(UserAgentInterceptor())
        okHttpClientBuilder.addInterceptor(AndroidHeaderInterceptor())
        okHttpClientBuilder.addInterceptor(JwtInterceptor())
        okHttpClientBuilder.addInterceptor(PlatformInterceptor())
        okHttpClientBuilder.addInterceptor(GuestUserInterceptor())
        okHttpClientBuilder.addInterceptor(ForbiddenInterceptor())

        // Add delays to all api calls
        // ifDebug { okHttpClientBuilder.addInterceptor(DelayInterceptor(2_000, TimeUnit.MILLISECONDS)) }

        if (envForConfig(BuildConfig.ENV) == Env.DEV || BuildConfig.DEBUG) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            okHttpClientBuilder.addInterceptor(httpLoggingInterceptor)
        }
        return okHttpClientBuilder.build()
    }

    @Provides
    @Singleton
    @WebService
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(String::class.java, StringConverter())
        .create()
}