package com.example.myapplication.core.network

import com.example.myapplication.core.model.BaseResponse
import com.example.myapplication.core.model.QiniuUploadResult
import com.example.myapplication.core.model.QiniuUploadTokenData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Url

interface UploadService {
    @GET("api/uploads/qiniu/token")
    suspend fun getQiniuUploadToken(
        @Query("ext") ext: String,
        @Query("dir") dir: String
    ): Response<BaseResponse<QiniuUploadTokenData>>

    @Multipart
    @POST
    suspend fun uploadToQiniu(
        @Url uploadUrl: String,
        @Part("token") token: RequestBody,
        @Part("key") key: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<QiniuUploadResult>
}
