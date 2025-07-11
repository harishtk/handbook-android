package com.handbook.app.feature.home.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import com.handbook.app.feature.home.domain.model.request.NotificationRequest

data class NotificationRequestDto(
    @SerializedName("page")
    val page: Int,
    @SerializedName("pageSize")
    val pageSize: Int
)

fun NotificationRequest.asDto(): NotificationRequestDto {
    return NotificationRequestDto(
        page = pagedRequest.key ?: 0,
        pageSize = pagedRequest.loadSize,
    )
}

