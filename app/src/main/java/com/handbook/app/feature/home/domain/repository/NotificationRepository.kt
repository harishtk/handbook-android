package com.handbook.app.feature.home.domain.repository

import kotlinx.coroutines.flow.Flow
import com.handbook.app.core.util.Result
import com.handbook.app.feature.home.domain.model.HandbookNotification
import com.handbook.app.feature.home.domain.model.NotificationData
import com.handbook.app.feature.home.domain.model.request.NotificationRequest
import com.handbook.app.common.util.paging.PagedData

interface NotificationRepository {

    fun notificationStream(): Flow<List<HandbookNotification>>

    suspend fun refreshNotifications(request: NotificationRequest): Result<PagedData<Int, HandbookNotification>>
}