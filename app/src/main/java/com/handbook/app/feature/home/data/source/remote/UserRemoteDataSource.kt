package com.handbook.app.feature.home.data.source.remote

import com.google.gson.Gson
import okhttp3.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import com.handbook.app.BuildConfig
import com.handbook.app.core.data.source.remote.BaseRemoteDataSource
import com.handbook.app.core.util.NetworkMonitor
import com.handbook.app.core.util.NetworkResult
import com.handbook.app.feature.home.data.source.remote.model.GetUsersResponse
import com.handbook.app.feature.home.data.source.remote.model.UserProfileResponse
import javax.inject.Inject

private const val USERS_BASE_URL = BuildConfig.API_URL

class UserRemoteDataSource @Inject constructor(
    networkHelper: NetworkMonitor,
    gson: Gson,
    okhttpCallFactory: dagger.Lazy<Call.Factory>,
) : BaseRemoteDataSource(networkHelper) {

    private val userApi = Retrofit.Builder()
        .baseUrl(USERS_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .callFactory { okhttpCallFactory.get().newCall(it) }
        .build().create<UserApi>()

    suspend fun getUser(userId: String): NetworkResult<UserProfileResponse> =
        safeApiCall { userApi.getUser(userId) }

    suspend fun getOwnUser(): NetworkResult<UserProfileResponse> =
        safeApiCall { userApi.getOwnUser() }

    suspend fun getUsers(
        sortBy: String,
        page: Int,
        pageSize: Int
    ): NetworkResult<GetUsersResponse> =
        safeApiCall { userApi.getUsers(sortBy, page, pageSize) }

    suspend fun followUser(userId: String): NetworkResult<UserProfileResponse> =
        safeApiCall { userApi.followUser(userId) }

    suspend fun unfollowUser(userId: String): NetworkResult<UserProfileResponse> =
        safeApiCall { userApi.unfollowUser(userId) }

    suspend fun getFollowing(page: Int, pageSize: Int): NetworkResult<GetUsersResponse> =
        safeApiCall { userApi.getFollowing(page, pageSize) }

    suspend fun getFollowers(page: Int, pageSize: Int): NetworkResult<GetUsersResponse> =
        safeApiCall { userApi.getFollowers(page, pageSize) }

    suspend fun getFollowing(
        userId: String,
        page: Int,
        pageSize: Int
    ): NetworkResult<GetUsersResponse> =
        safeApiCall { userApi.getFollowing(userId, page, pageSize) }

    suspend fun getFollowers(
        userId: String,
        page: Int,
        pageSize: Int
    ): NetworkResult<GetUsersResponse> =
        safeApiCall { userApi.getFollowers(userId, page, pageSize) }
}