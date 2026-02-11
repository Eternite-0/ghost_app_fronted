package com.example.myapplication.core.network

import com.example.myapplication.core.model.BaseResponse
import com.example.myapplication.core.model.NotificationItem
import com.example.myapplication.core.model.PagedResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface NotificationService {
    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<BaseResponse<PagedResponse<NotificationItem>>>

    @GET("api/notifications/unread-count")
    suspend fun getUnreadCount(): Response<BaseResponse<Map<String, Long>>>

    @POST("api/notifications/read-all")
    suspend fun markAllAsRead(): Response<BaseResponse<Void>>
}
