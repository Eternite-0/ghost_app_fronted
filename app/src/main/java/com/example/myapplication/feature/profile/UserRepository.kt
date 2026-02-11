package com.example.myapplication.feature.profile

import com.example.myapplication.core.model.BaseResponse
import com.example.myapplication.core.model.PagedResponse
import com.example.myapplication.core.model.UpdateProfileRequest
import com.example.myapplication.core.model.Story
import com.example.myapplication.core.model.User
import com.example.myapplication.core.network.UserService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response

import com.example.myapplication.core.model.UserPublicResponse

class UserRepository(private val userService: UserService) {

    suspend fun getUserPublicProfile(userId: String): Result<UserPublicResponse> {
        return handleResponse(userService.getUserPublicProfile(userId))
    }

    suspend fun followUser(userId: String): Result<Boolean> {
        val response = userService.followUser(userId)
        if (response.isSuccessful) return Result.success(true)
        return Result.failure(Exception(response.errorBody()?.string() ?: "Unknown error"))
    }

    suspend fun unfollowUser(userId: String): Result<Boolean> {
        val response = userService.unfollowUser(userId)
        if (response.isSuccessful) return Result.success(true)
        return Result.failure(Exception(response.errorBody()?.string() ?: "Unknown error"))
    }

    suspend fun getCurrentUser(): Result<User> {
        return handleResponse(userService.getCurrentUser())
    }

    suspend fun updateProfile(username: String, bio: String): Result<User> {
        return handleResponse(userService.updateProfile(UpdateProfileRequest(username, bio)))
    }

    suspend fun uploadAvatar(file: java.io.File): Result<String> {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("avatar", file.name, requestFile)
        val response = userService.uploadAvatar(body)
        if (response.isSuccessful) {
             val bodyResponse = response.body()
             if (bodyResponse != null && bodyResponse.data != null) {
                 return Result.success(bodyResponse.data["avatar_url"] ?: "")
             }
        }
        return Result.failure(Exception(response.errorBody()?.string() ?: "Unknown error"))
    }

    suspend fun getMyStories(page: Int, limit: Int): Result<PagedResponse<Story>> {
        return handleResponse(userService.getMyStories(page, limit))
    }

    suspend fun getMyFavorites(page: Int, limit: Int): Result<PagedResponse<Story>> {
        return handleResponse(userService.getMyFavorites(page, limit))
    }

    suspend fun getUserComments(userId: String, page: Int, limit: Int): Result<PagedResponse<com.example.myapplication.core.model.Comment>> {
        return handleResponse(userService.getUserComments(userId, page, limit))
    }

    suspend fun getUserFollowers(userId: String, page: Int, limit: Int): Result<PagedResponse<UserPublicResponse>> {
        return handleResponse(userService.getUserFollowers(userId, page, limit))
    }

    suspend fun getUserFollowing(userId: String, page: Int, limit: Int): Result<PagedResponse<UserPublicResponse>> {
        return handleResponse(userService.getUserFollowing(userId, page, limit))
    }

    private fun <T> handleResponse(response: Response<BaseResponse<T>>): Result<T> {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.data != null) {
                return Result.success(body.data)
            }
        }
        return Result.failure(Exception(response.errorBody()?.string() ?: "Unknown error"))
    }
}
