package com.example.myapplication.core.network

import com.example.myapplication.core.model.BaseResponse
import com.example.myapplication.core.model.Comment
import com.example.myapplication.core.model.CreateCommentRequest
import com.example.myapplication.core.model.PagedResponse
import retrofit2.Response
import retrofit2.http.*

interface CommentService {
    @GET("api/stories/{storyId}/comments")
    suspend fun getComments(
        @Path("storyId") storyId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("parentId") parentId: String? = null
    ): Response<BaseResponse<PagedResponse<Comment>>>

    @POST("api/stories/{storyId}/comments")
    suspend fun createComment(
        @Path("storyId") storyId: String,
        @Body request: CreateCommentRequest
    ): Response<BaseResponse<Comment>>

    @DELETE("api/stories/{storyId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("storyId") storyId: String,
        @Path("commentId") commentId: String
    ): Response<BaseResponse<Void>>
}
