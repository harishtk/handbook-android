package com.handbook.app.core.data.source.remote.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UploaderResponse(
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: UploadDataDto?
) : Serializable

data class UploadDataDto(
    @SerializedName("fileName")
    val fileName: String,
)