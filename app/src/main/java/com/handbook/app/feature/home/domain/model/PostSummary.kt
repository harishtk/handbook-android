package com.handbook.app.feature.home.domain.model

data class PostSummary(
    val id: String,
    val authorId: String,
    val content: String,
    val createdAt: String,
    val likesCount: Int,
    val likedByCurrentUser: Boolean,
    val author: UserSummary,
)