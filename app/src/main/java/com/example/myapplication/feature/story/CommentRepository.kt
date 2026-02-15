package com.example.myapplication.feature.story

import com.example.myapplication.core.model.BaseResponse
import com.example.myapplication.core.model.Comment
import com.example.myapplication.core.model.CreateCommentRequest
import com.example.myapplication.core.model.PagedResponse
import com.example.myapplication.core.network.CommentService
import retrofit2.Response

class CommentRepository(private val commentService: CommentService) {

    suspend fun getComments(storyId: String, page: Int, limit: Int, parentId: String? = null): Result<PagedResponse<Comment>> {
        return handleResponse(commentService.getComments(storyId, page, limit, parentId))
    }

    suspend fun createComment(storyId: String, content: String, parentId: String? = null): Result<Comment> {
        return handleResponse(commentService.createComment(storyId, CreateCommentRequest(content, parentId)))
    }

    suspend fun deleteComment(storyId: String, commentId: String): Result<Boolean> {
        val response = commentService.deleteComment(storyId, commentId)
        return if (response.isSuccessful) {
            Result.success(true)
        } else {
            Result.failure(Exception(response.errorBody()?.string() ?: "Unknown error"))
        }
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
