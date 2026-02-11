package com.example.myapplication.feature.notification

import com.example.myapplication.core.model.BaseResponse
import com.example.myapplication.core.model.NotificationItem
import com.example.myapplication.core.model.PagedResponse
import com.example.myapplication.core.network.NotificationService
import retrofit2.Response

class NotificationRepository(
    private val notificationService: NotificationService
) {
    suspend fun getNotifications(page: Int = 1, limit: Int = 20): Result<PagedResponse<NotificationItem>> {
        return handleResponse(notificationService.getNotifications(page, limit))
    }

    suspend fun getUnreadCount(): Result<Long> {
        val response = notificationService.getUnreadCount()
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.data != null) {
                return Result.success(body.data["unread_count"] ?: 0L)
            }
        }
        return Result.failure(Exception(response.errorBody()?.string() ?: "Unknown error"))
    }

    suspend fun markAllAsRead(): Result<Unit> {
        val response = notificationService.markAllAsRead()
        return if (response.isSuccessful) {
            Result.success(Unit)
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
