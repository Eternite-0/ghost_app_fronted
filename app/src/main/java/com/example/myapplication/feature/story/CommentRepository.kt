package com.example.myapplication.feature.story

import com.example.myapplication.core.model.BaseResponse
import com.example.myapplication.core.model.Comment
import com.example.myapplication.core.model.CreateCommentRequest
import com.example.myapplication.core.model.PagedResponse
import com.example.myapplication.core.network.CommentService
import retrofit2.Response

class CommentRepository(private val commentService: CommentService) {

    suspend fun getComments(storyId: String, page: Int, limit: Int): Result<PagedResponse<Comment>> {
        return handleResponse(commentService.getComments(storyId, page, limit))
    }

    suspend fun createComment(storyId: String, content: String): Result<Comment> {
        return handleResponse(commentService.createComment(storyId, CreateCommentRequest(content)))
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
