package com.example.myapplication.core.model

import com.google.gson.annotations.SerializedName

data class StoryMapMarker(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("category") val category: String,
    @SerializedName("location") val location: Location,
    @SerializedName("likes_count") val likesCount: Int
)

data class Story(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("author") val author: Author,
    @SerializedName("category") val category: String,
    @SerializedName("location") val location: Location,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("audio_url") val audioUrl: String?,
    @SerializedName("likes_count") val likesCount: Int,
    @SerializedName("favorites_count") val favoritesCount: Int,
    @SerializedName("comments_count") val commentsCount: Int,
    @SerializedName("views_count") val viewsCount: Int,
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("is_liked") val isLiked: Boolean?,
    @SerializedName("is_favorited") val isFavorited: Boolean?
)

data class Location(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("address") val address: String? = null
)

data class Author(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("avatar_url") val avatarUrl: String?
)

data class CreateStoryRequest(
    val title: String,
    val content: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val placeName: String?,
    val mapStory: Boolean = false
)
