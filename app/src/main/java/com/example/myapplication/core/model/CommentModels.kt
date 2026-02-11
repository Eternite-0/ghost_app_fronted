package com.example.myapplication.core.model

import com.google.gson.annotations.SerializedName

data class Comment(
    @SerializedName("id") val id: String,
    @SerializedName("content") val content: String,
    @SerializedName("author") val author: Author,
    @SerializedName("likes_count") val likesCount: Int,
    @SerializedName("parent_id") val parentId: String?,
    @SerializedName("replies_count") val repliesCount: Long,
    @SerializedName("created_at") val createdAt: String
)

data class CreateCommentRequest(
    @SerializedName("content") val content: String,
    @SerializedName("parentId") val parentId: String? = null
)
