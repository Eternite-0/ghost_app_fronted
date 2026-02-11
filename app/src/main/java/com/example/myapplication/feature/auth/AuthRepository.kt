package com.example.myapplication.feature.auth

import com.example.myapplication.core.model.AuthResponse
import com.example.myapplication.core.model.BaseResponse
import com.example.myapplication.core.model.LoginRequest
import com.example.myapplication.core.model.RegisterRequest
import com.example.myapplication.core.network.AuthService
import com.example.myapplication.core.storage.TokenManager
import retrofit2.Response

class AuthRepository(
    private val authService: AuthService,
    private val tokenManager: TokenManager
) {
    suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return try {
            val response = authService.login(request)
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = authService.register(request)
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun handleResponse(response: Response<BaseResponse<AuthResponse>>): Result<AuthResponse> {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.data != null) {
                tokenManager.saveToken(body.data.token.accessToken)
                return Result.success(body.data)
            }
        }
        return Result.failure(Exception(response.errorBody()?.string() ?: "Unknown error"))
    }
}
