package com.handbook.app.feature.home.data.source.remote.model

import com.handbook.app.feature.home.data.source.remote.dto.UserProfileDto

data class UserProfileResponse(
    val statusCode: Int,
    val message: String,
    val data: UserProfileDto?
)

