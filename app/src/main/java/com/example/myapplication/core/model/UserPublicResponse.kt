package com.example.myapplication.core.model

import com.google.gson.annotations.SerializedName

data class UserPublicResponse(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("followersCount") val followersCount: Long,
    @SerializedName("followingCount") val followingCount: Long,
    @SerializedName("isFollowed") val isFollowed: Boolean
)
