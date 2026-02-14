package com.example.myapplication.feature.story

import com.example.myapplication.core.model.BaseResponse
import com.example.myapplication.core.model.CreateStoryRequest
import com.example.myapplication.core.model.MapStoriesPayload
import com.example.myapplication.core.model.PagedResponse
import com.example.myapplication.core.model.Story
import com.example.myapplication.core.network.StoryService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

class StoryRepository(private val storyService: StoryService) {

    suspend fun getPublishedStories(
        page: Int = 1,
        limit: Int = 20,
        keyword: String? = null,
        category: String? = null
    ): Result<PagedResponse<Story>> {
        return handleResponse(storyService.getPublishedStories(page, limit, keyword, category))
    }

    suspend fun getStoriesOnMap(latitude: Double, longitude: Double, radius: Int = 5000): Result<MapStoriesPayload> {
        return handleResponse(storyService.getStoriesOnMap(latitude, longitude, radius))
    }

    suspend fun getStoryById(storyId: String): Result<Story> {
        return handleResponse(storyService.getStoryById(storyId))
    }

    suspend fun createStory(request: CreateStoryRequest, imageFile: File?): Result<Story> {
        val title = request.title.toRequestBody("text/plain".toMediaTypeOrNull())
        val content = request.content.toRequestBody("text/plain".toMediaTypeOrNull())
        val category = request.category.toRequestBody("text/plain".toMediaTypeOrNull())
        val latitude = request.latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val longitude = request.longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val address = request.address?.toRequestBody("text/plain".toMediaTypeOrNull())
        val placeName = request.placeName?.toRequestBody("text/plain".toMediaTypeOrNull())
        val mapStory = request.mapStory.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = imageFile?.let {
            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", it.name, requestFile)
        }

        return handleResponse(
            storyService.createStory(
                title, content, category, latitude, longitude, address, placeName, mapStory, imagePart
            )
        )
    }

    suspend fun likeStory(storyId: String): Result<Map<String, Int>> {
        return handleResponse(storyService.likeStory(storyId))
    }

    suspend fun unlikeStory(storyId: String): Result<Map<String, Int>> {
        return handleResponse(storyService.unlikeStory(storyId))
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
