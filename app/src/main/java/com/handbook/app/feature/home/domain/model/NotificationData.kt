package com.handbook.app.feature.home.domain.model

import com.google.gson.annotations.SerializedName
import com.handbook.app.feature.home.data.source.remote.dto.NotificationDto

data class NotificationData(
    val notifications: List<HandbookNotification>,
    val page: Int,
    val pageSize: Int,
    val totalNotifications: Int,
    val totalPages: Int,
    val isLastPage: Boolean
)
