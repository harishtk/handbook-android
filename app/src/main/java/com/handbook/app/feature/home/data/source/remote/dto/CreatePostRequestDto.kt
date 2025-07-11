package com.handbook.app.feature.home.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import com.handbook.app.feature.home.domain.model.request.CreatePostRequest

data class CreatePostRequestDto(
    @SerializedName("content")
    val content: String
)

fun CreatePostRequest.asDto(): CreatePostRequestDto {
    return CreatePostRequestDto(
        content = this.content
    )
}
