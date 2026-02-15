package com.example.myapplication.core.network

import com.example.myapplication.core.model.BaseResponse
import com.example.myapplication.core.model.MapStoriesPayload
import com.example.myapplication.core.model.PagedResponse
import com.example.myapplication.core.model.Story
import com.example.myapplication.core.model.StoryMapMarker
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface StoryService {
    @GET("api/stories")
    suspend fun getPublishedStories(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("q") keyword: String? = null,
        @Query("category") category: String? = null
    ): Response<BaseResponse<PagedResponse<Story>>>

    @GET("api/stories/map")
    suspend fun getStoriesOnMap(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Int = 5000,
        @Query("category") category: String? = null
    ): Response<BaseResponse<MapStoriesPayload>>

    @GET("api/stories/{storyId}")
    suspend fun getStoryById(@Path("storyId") storyId: String): Response<BaseResponse<Story>>

    @Multipart
    @POST("api/stories")
    suspend fun createStory(
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody,
        @Part("category") category: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("address") address: RequestBody?,
        @Part("placeName") placeName: RequestBody?,
        @Part("imageUrl") imageUrl: RequestBody?,
        @Part("mapStory") mapStory: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<BaseResponse<Story>>

    @POST("api/stories/{storyId}/like")
    suspend fun likeStory(@Path("storyId") storyId: String): Response<BaseResponse<Map<String, Int>>>

    @DELETE("api/stories/{storyId}/like")
    suspend fun unlikeStory(@Path("storyId") storyId: String): Response<BaseResponse<Map<String, Int>>>

    @POST("api/stories/{storyId}/favorite")
    suspend fun favoriteStory(@Path("storyId") storyId: String): Response<BaseResponse<Map<String, Int>>>

    @DELETE("api/stories/{storyId}/favorite")
    suspend fun unfavoriteStory(@Path("storyId") storyId: String): Response<BaseResponse<Map<String, Int>>>

    @DELETE("api/stories/{storyId}")
    suspend fun deleteStory(@Path("storyId") storyId: String): Response<BaseResponse<Void>>
}
