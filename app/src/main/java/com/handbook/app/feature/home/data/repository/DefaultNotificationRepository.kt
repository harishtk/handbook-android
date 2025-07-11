package com.handbook.app.feature.home.data.repository

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import com.handbook.app.common.util.paging.PagedData
import com.handbook.app.core.util.NetworkResult
import com.handbook.app.core.util.NetworkResultParser
import com.handbook.app.core.util.Result
import com.handbook.app.feature.home.data.source.remote.dto.asDto
import com.handbook.app.feature.home.data.source.remote.NotificationRemoteDataSource
import com.handbook.app.feature.home.data.source.remote.model.toNotificationData
import com.handbook.app.feature.home.domain.model.HandbookNotification
import com.handbook.app.feature.home.domain.model.request.NotificationRequest
import com.handbook.app.feature.home.domain.repository.NotificationRepository
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection
import kotlin.collections.emptyList

class DefaultNotificationRepository @Inject constructor(
    private val remoteDataSource: NotificationRemoteDataSource,
) : NotificationRepository, NetworkResultParser {

    private val notificationsCache = MutableSharedFlow<List<HandbookNotification>>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        notificationsCache.tryEmit(emptyList())
    }

    override fun notificationStream(): Flow<List<HandbookNotification>> = notificationsCache

    override suspend fun refreshNotifications(request: NotificationRequest): Result<PagedData<Int, HandbookNotification>> {
        return when (val networkResult = remoteDataSource.getNotifications(request.asDto())) {
            is NetworkResult.Success -> {
                if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_OK) {
                    if (networkResult.data.data != null) {
                        val data = networkResult.data.data.toNotificationData()

                        if (request.pagedRequest.key == null) {
                            notificationsCache.tryEmit(data.notifications)
                        } else {
                            val cache = notificationsCache.replayCache.firstOrNull() ?: emptyList()
                            val newNotifications: List<HandbookNotification> =
                                data.notifications + cache
                            notificationsCache.tryEmit(newNotifications)
                        }
                        Result.Success(
                            PagedData(
                                data = data.notifications,
                                nextKey = if (data.isLastPage) null else data.page + 1,
                                prevKey = null,
                                totalCount = data.totalNotifications,
                            )
                        )
                    } else {
                        emptyResponse(networkResult)
                    }
                } else {
                    badResponse(networkResult)
                }
            }

            else -> parseErrorNetworkResult(networkResult)
        }
    }
}