package com.example.myapplication.core.model

import com.google.gson.annotations.SerializedName

data class QiniuUploadTokenData(
    @SerializedName("upload_token") val uploadToken: String,
    @SerializedName("upload_url") val uploadUrl: String,
    @SerializedName("file_base_url") val fileBaseUrl: String,
    @SerializedName("object_key") val objectKey: String,
    @SerializedName("expires_in") val expiresIn: Long
)

data class QiniuUploadResult(
    @SerializedName("key") val key: String?,
    @SerializedName("hash") val hash: String?
)
