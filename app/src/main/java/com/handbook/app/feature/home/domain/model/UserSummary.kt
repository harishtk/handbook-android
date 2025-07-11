package com.handbook.app.feature.home.domain.model

data class UserSummary(
    val id: String,
    val username: String,
    val displayName: String,
    val profilePictureId: String,
    val isFollowing: Boolean,
)