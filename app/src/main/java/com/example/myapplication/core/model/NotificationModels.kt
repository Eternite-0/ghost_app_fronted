package com.example.myapplication.core.model

import com.google.gson.annotations.SerializedName

data class NotificationItem(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("content") val content: String,
    @SerializedName("sender") val sender: NotificationSender?,
    @SerializedName("related_entity_id") val relatedEntityId: String?,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("created_at") val createdAt: String
)

data class NotificationSender(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String
)
