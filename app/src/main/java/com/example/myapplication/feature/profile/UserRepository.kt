package com.example.myapplication.feature.profile

import com.example.myapplication.core.model.BaseResponse
import com.example.myapplication.core.model.PagedResponse
import com.example.myapplication.core.model.UpdateProfileRequest
import com.example.myapplication.core.model.Story
import com.example.myapplication.core.model.User
import com.example.myapplication.core.network.UserService
import com.example.myapplication.core.network.UploadService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

import com.example.myapplication.core.model.UserPublicResponse

class UserRepository(
    private val userService: UserService,
    private val uploadService: UploadService
) {

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
        val ext = file.extension.ifBlank { "jpg" }
        val tokenResp = uploadService.getQiniuUploadToken(ext, "avatar")
        if (!tokenResp.isSuccessful || tokenResp.body()?.data == null) {
            return Result.failure(Exception("获取七牛上传凭证失败"))
        }
        val tokenData = tokenResp.body()!!.data!!

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val qiniuFile = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val tokenBody = tokenData.uploadToken.toRequestBody("text/plain".toMediaTypeOrNull())
        val keyBody = tokenData.objectKey.toRequestBody("text/plain".toMediaTypeOrNull())
        val uploadResp = uploadService.uploadToQiniu(tokenData.uploadUrl, tokenBody, keyBody, qiniuFile)
        if (!uploadResp.isSuccessful) {
            return Result.failure(Exception("上传头像到七牛失败"))
        }
        val uploadedKey = uploadResp.body()?.key ?: tokenData.objectKey
        val finalUrl = "${tokenData.fileBaseUrl}/$uploadedKey"

        val response = userService.uploadAvatarByUrl(finalUrl)
        if (response.isSuccessful) {
             val bodyResponse = response.body()
             if (bodyResponse != null && bodyResponse.data != null) {
                 return Result.success(bodyResponse.data["avatar_url"] ?: finalUrl)
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
        if (response.code() == 401) {
            return Result.failure(Exception("登录已过期，请重新登录"))
        }
        return Result.failure(Exception(response.errorBody()?.string() ?: "Unknown error"))
    }
}
