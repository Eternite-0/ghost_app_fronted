package com.example.myapplication.core.network

import com.example.myapplication.core.storage.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager,
    private val backendHost: String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenManager.accessToken.first() }
        val originalRequest = chain.request()
        val isBackendRequest = originalRequest.url.host.equals(backendHost, ignoreCase = true)

        val requestBuilder = originalRequest.newBuilder()

        if (isBackendRequest && !token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())

        if (isBackendRequest && response.code == 401) {
            runBlocking { tokenManager.clearToken() }
        }

        return response
    }
}
