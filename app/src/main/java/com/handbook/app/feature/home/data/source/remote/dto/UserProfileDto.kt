package com.handbook.app.feature.home.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import com.handbook.app.feature.home.domain.model.UserProfile


data class UserProfileDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("displayName")
    val displayName: String,
    @SerializedName("bio")
    val bio: String,
    @SerializedName("profilePictureId")
    val profilePictureId: String?,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("followersCount")
    val followersCount: Int,
    @SerializedName("followingCount")
    val followingCount: Int,
    @SerializedName("postsCount")
    val postsCount: Int?,
    @SerializedName("isFollowing")
    val isFollowing: Boolean,
    @SerializedName("isSelf")
    val isSelf: Boolean
)

fun UserProfileDto.toUserProfile(): UserProfile {
    return UserProfile(
        id = id,
        username = username,
        displayName = displayName,
        bio = bio,
        profilePictureId = profilePictureId ?: "",
        createdAt = createdAt,
        followersCount = followersCount,
        followingCount = followingCount,
        postsCount = postsCount ?: 0,
        isFollowing = isFollowing,
        isSelf = isSelf
    )
}