package com.handbook.app.feature.home.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import com.handbook.app.feature.home.domain.model.UserSummary

data class UserSummaryDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("displayName")
    val displayName: String,
    @SerializedName("profilePictureId")
    val profilePictureId: String?,
    @SerializedName("isFollowing")
    val isFollowing: Boolean?,
)

fun UserSummaryDto.toUserSummary(): UserSummary {
    return UserSummary(
        id = id,
        username = username,
        displayName = displayName,
        profilePictureId = profilePictureId ?: "",
        isFollowing = isFollowing ?: false,
    )
}