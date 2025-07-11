package com.handbook.app.feature.home.data.source.remote

import com.google.gson.Gson
import okhttp3.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import com.handbook.app.BuildConfig
import com.handbook.app.core.data.source.remote.BaseRemoteDataSource
import com.handbook.app.core.domain.model.BaseResponse
import com.handbook.app.core.util.NetworkMonitor
import com.handbook.app.feature.home.data.source.remote.dto.IdsRequestDto
import com.handbook.app.feature.home.data.source.remote.dto.NotificationRequestDto
import com.handbook.app.feature.home.data.source.remote.model.NotificationsResponse
import com.handbook.app.feature.home.data.source.remote.model.UnreadCountResponse
import javax.inject.Inject

private const val NOTIFICATIONS_API = BuildConfig.API_URL

class NotificationRemoteDataSource @Inject constructor(
    networkHelper: NetworkMonitor,
    gson: Gson,
    okhttpCallFactory: dagger.Lazy<Call.Factory>
) : BaseRemoteDataSource(networkHelper) {

    private val notificationApi = Retrofit.Builder()
        .baseUrl(NOTIFICATIONS_API)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .callFactory { okhttpCallFactory.get().newCall(it) }
        .build()
        .create<NotificationApi>()

    suspend fun getNotifications(notificationRequestDto: NotificationRequestDto) = safeApiCall {
        notificationApi.getNotifications(
            page = notificationRequestDto.page,
            pageSize = notificationRequestDto.pageSize
        )
    }

    suspend fun markNotificationsAsRead(idsRequestDto: IdsRequestDto) = safeApiCall {
        notificationApi.markNotificationsAsRead(idsRequestDto)
    }

    suspend fun getUnreadNotificationCount() = safeApiCall {
        notificationApi.getUnreadNotificationCount()
    }

}

private interface NotificationApi {
    @GET("notifications")
    suspend fun getNotifications(
        @Query("page") page: Int,
        @Query("size") pageSize: Int,
    ): Response<NotificationsResponse>

    @POST("notifications/mark-read")
    suspend fun markNotificationsAsRead(@Body idsRequestDto: IdsRequestDto): Response<BaseResponse>

    @POST("notifications/unread-count")
    suspend fun getUnreadNotificationCount(): Response<UnreadCountResponse>
}