package com.example.myapplication.feature.story

import com.example.myapplication.core.model.BaseResponse
import com.example.myapplication.core.model.CreateStoryRequest
import com.example.myapplication.core.model.MapStoriesPayload
import com.example.myapplication.core.model.PagedResponse
import com.example.myapplication.core.model.Story
import com.example.myapplication.core.network.StoryService
import com.example.myapplication.core.network.UploadService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

class StoryRepository(
    private val storyService: StoryService,
    private val uploadService: UploadService
) {

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
        val imageUrl = if (imageFile != null) {
            val cloudUrlResult = uploadImageToQiniu(imageFile)
            if (cloudUrlResult.isFailure) {
                return Result.failure(cloudUrlResult.exceptionOrNull() ?: Exception("七牛上传失败"))
            }
            cloudUrlResult.getOrThrow().toRequestBody("text/plain".toMediaTypeOrNull())
        } else {
            null
        }
        val mapStory = request.mapStory.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        return handleResponse(
            storyService.createStory(
                title, content, category, latitude, longitude, address, placeName, imageUrl, mapStory, null
            )
        )
    }

    private suspend fun uploadImageToQiniu(imageFile: File): Result<String> {
        val ext = imageFile.extension.ifBlank { "jpg" }
        val tokenResp = uploadService.getQiniuUploadToken(ext, "stories")
        if (!tokenResp.isSuccessful || tokenResp.body()?.data == null) {
            return Result.failure(Exception("获取七牛上传凭证失败"))
        }

        val tokenData = tokenResp.body()!!.data!!
        val imagePart = MultipartBody.Part.createFormData(
            "file",
            imageFile.name,
            imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        )
        val token = tokenData.uploadToken.toRequestBody("text/plain".toMediaTypeOrNull())
        val key = tokenData.objectKey.toRequestBody("text/plain".toMediaTypeOrNull())
        val uploadResp = uploadService.uploadToQiniu(tokenData.uploadUrl, token, key, imagePart)

        if (!uploadResp.isSuccessful) {
            return Result.failure(Exception("上传图片到七牛失败"))
        }

        val returnedKey = uploadResp.body()?.key ?: tokenData.objectKey
        return Result.success("${tokenData.fileBaseUrl}/$returnedKey")
    }

    suspend fun likeStory(storyId: String): Result<Map<String, Int>> {
        return handleResponse(storyService.likeStory(storyId))
    }

    suspend fun unlikeStory(storyId: String): Result<Map<String, Int>> {
        return handleResponse(storyService.unlikeStory(storyId))
    }

    suspend fun deleteStory(storyId: String): Result<Boolean> {
        val response = storyService.deleteStory(storyId)
        return if (response.isSuccessful) {
            Result.success(true)
        } else if (response.code() == 401) {
            Result.failure(Exception("登录已过期，请重新登录后再操作"))
        } else if (response.code() == 403) {
            Result.failure(Exception("无权删除该故事"))
        } else {
            Result.failure(Exception(response.errorBody()?.string() ?: "删除失败"))
        }
    }

    private fun <T> handleResponse(response: Response<BaseResponse<T>>): Result<T> {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.data != null) {
                return Result.success(body.data)
            }
        }
        if (response.code() == 401) {
            return Result.failure(Exception("登录已过期，请重新登录后再发布"))
        }
        return Result.failure(Exception(response.errorBody()?.string() ?: "Unknown error"))
    }
}
