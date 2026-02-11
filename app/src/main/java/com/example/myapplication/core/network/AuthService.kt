package com.example.myapplication.core.network

import com.example.myapplication.core.model.AuthResponse
import com.example.myapplication.core.model.BaseResponse
import com.example.myapplication.core.model.LoginRequest
import com.example.myapplication.core.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<BaseResponse<AuthResponse>>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<BaseResponse<AuthResponse>>
}
