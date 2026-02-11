package com.example.myapplication.core.model

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @SerializedName("username") val username: String?,
    @SerializedName("bio") val bio: String?
)

data class PagedResponse<T>(
    @SerializedName("items") val items: List<T>,
    @SerializedName("total") val total: Long,
    @SerializedName("has_more") val hasMore: Boolean
)
