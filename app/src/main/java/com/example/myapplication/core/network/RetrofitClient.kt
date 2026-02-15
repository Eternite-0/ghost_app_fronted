package com.example.myapplication.core.network

import android.content.Context
import com.example.myapplication.core.storage.TokenManager
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.HttpUrl.Companion.toHttpUrl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    fun getClient(context: Context): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            // Avoid printing binary multipart bodies (image bytes) into logcat.
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val tokenManager = TokenManager(context)
        val backendHost = BASE_URL.toHttpUrl().host
        val authInterceptor = AuthInterceptor(tokenManager, backendHost)

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .build()

        val gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
    }
}
