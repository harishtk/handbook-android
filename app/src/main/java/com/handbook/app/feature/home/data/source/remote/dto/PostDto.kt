package com.handbook.app.feature.home.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import com.handbook.app.feature.home.domain.model.Post

data class PostDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("authorId")
    val authorId: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("likesCount")
    val likesCount: Int,
    @SerializedName("likedByCurrentUser")
    val likedByCurrentUser: Boolean
)

fun PostDto.toPost(): Post {
    return Post(
        id = id,
        authorId = authorId,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt,
        likesCount = likesCount,
        likedByCurrentUser = likedByCurrentUser
    )
}