package com.example.myapplication.core.model

import com.google.gson.annotations.SerializedName

data class Comment(
    @SerializedName("id") val id: String,
    @SerializedName("content") val content: String,
    @SerializedName("author") val author: Author,
    @SerializedName(value = "likes_count", alternate = ["likesCount"]) val likesCount: Int,
    @SerializedName(value = "parent_id", alternate = ["parentId"]) val parentId: String?,
    @SerializedName(value = "replies_count", alternate = ["repliesCount"]) val repliesCount: Long,
    @SerializedName(value = "created_at", alternate = ["createdAt"]) val createdAt: String,
    @SerializedName(value = "is_owner", alternate = ["isOwner"]) val isOwner: Boolean = false
)

data class CreateCommentRequest(
    @SerializedName("content") val content: String,
    @SerializedName("parent_id") val parentId: String? = null
)
