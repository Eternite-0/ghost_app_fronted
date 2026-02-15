package com.example.myapplication.core.network

import com.example.myapplication.core.model.BaseResponse
import com.example.myapplication.core.model.PagedResponse
import com.example.myapplication.core.model.Story
import com.example.myapplication.core.model.UpdateProfileRequest
import com.example.myapplication.core.model.User
import com.example.myapplication.core.model.UserPublicResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface UserService {
    @GET("api/users/me")
    suspend fun getCurrentUser(): Response<BaseResponse<User>>

    @PUT("api/users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<BaseResponse<User>>

    @Multipart
    @POST("api/users/me/avatar")
    suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): Response<BaseResponse<Map<String, String>>>

    @FormUrlEncoded
    @POST("api/users/me/avatar")
    suspend fun uploadAvatarByUrl(@Field("avatarUrl") avatarUrl: String): Response<BaseResponse<Map<String, String>>>

    @GET("api/users/me/stories")
    suspend fun getMyStories(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<BaseResponse<PagedResponse<Story>>>

    @GET("api/users/me/favorites")
    suspend fun getMyFavorites(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<BaseResponse<PagedResponse<Story>>>

    @GET("api/users/{userId}/comments")
    suspend fun getUserComments(
        @Path("userId") userId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<BaseResponse<PagedResponse<com.example.myapplication.core.model.Comment>>>

    @GET("api/users/{userId}/followers")
    suspend fun getUserFollowers(
        @Path("userId") userId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<BaseResponse<PagedResponse<UserPublicResponse>>>

    @GET("api/users/{userId}/following")
    suspend fun getUserFollowing(
        @Path("userId") userId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<BaseResponse<PagedResponse<UserPublicResponse>>>

    @GET("api/users/{userId}")
    suspend fun getUserPublicProfile(@Path("userId") userId: String): Response<BaseResponse<UserPublicResponse>>

    @POST("api/users/{userId}/follow")
    suspend fun followUser(@Path("userId") userId: String): Response<BaseResponse<Void>>

    @DELETE("api/users/{userId}/follow")
    suspend fun unfollowUser(@Path("userId") userId: String): Response<BaseResponse<Void>>
}
