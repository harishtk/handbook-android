package com.handbook.app.feature.home.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import com.handbook.app.feature.home.domain.model.PostSummary

data class PostSummaryDto (
    @SerializedName("id")
    val id: String,
    @SerializedName("authorId")
    val authorId: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("likesCount")
    val likesCount: Int,
    @SerializedName("likedByCurrentUser")
    val likedByCurrentUser: Boolean,
    @SerializedName("author")
    val author: UserSummaryDto
)

fun PostSummaryDto.toPostSummary(): PostSummary {
    return PostSummary(
        id = id,
        authorId = authorId,
        content = content,
        createdAt = createdAt,
        likesCount = likesCount,
        likedByCurrentUser = likedByCurrentUser,
        author = author.toUserSummary()
    )
}