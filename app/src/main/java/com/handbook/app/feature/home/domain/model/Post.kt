package com.handbook.app.feature.home.domain.model

data class Post(
    val id: String,
    val authorId: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
    val likesCount: Int,
    val likedByCurrentUser: Boolean
)
