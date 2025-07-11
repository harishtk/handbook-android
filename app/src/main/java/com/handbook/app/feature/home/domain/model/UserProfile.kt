package com.handbook.app.feature.home.domain.model

data class UserProfile(
    val id: String,
    val username: String,
    val displayName: String,
    val bio: String,
    val profilePictureId: String,
    val createdAt: String,
    val followersCount: Int,
    val followingCount: Int,
    val postsCount: Int,
    val isFollowing: Boolean,
    val isSelf: Boolean,
)