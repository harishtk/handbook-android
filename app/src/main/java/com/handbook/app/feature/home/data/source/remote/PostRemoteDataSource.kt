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
import com.handbook.app.feature.home.data.source.remote.dto.CreatePostRequestDto
import com.handbook.app.feature.home.data.source.remote.model.PostFeedResponse
import com.handbook.app.feature.home.data.source.remote.model.SinglePostResponse
import javax.inject.Inject

private const val POST_API = BuildConfig.API_URL

class PostRemoteDataSource @Inject constructor(
    networkHelper: NetworkMonitor,
    gson: Gson,
    okhttpCallFactory: dagger.Lazy<Call.Factory>,
) : BaseRemoteDataSource(networkHelper) {

    private val postApi = Retrofit.Builder()
        .baseUrl(POST_API)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .callFactory { okhttpCallFactory.get().newCall(it) }
        .build()
        .create<PostApi>()

    suspend fun getGlobalFeed(page: Int, pageSize: Int): NetworkResult<PostFeedResponse> =
        safeApiCall { postApi.getGlobalFeed(page, pageSize) }

    suspend fun getPrivateFeed(page: Int, pageSize: Int): NetworkResult<PostFeedResponse> =
        safeApiCall { postApi.getPrivateFeed(page, pageSize) }

    suspend fun getPostsByAuthorId(authorId: String, page: Int, pageSize: Int): NetworkResult<PostFeedResponse> =
        safeApiCall { postApi.getPostsByAuthorId(authorId, page, pageSize) }

    suspend fun createPost(createPostRequestDto: CreatePostRequestDto): NetworkResult<SinglePostResponse> =
        safeApiCall { postApi.createPost(createPostRequestDto) }

    suspend fun getPostById(postId: String): NetworkResult<SinglePostResponse> = safeApiCall { postApi.getPostById(postId) }

    suspend fun likePost(postId: String): NetworkResult<SinglePostResponse> = safeApiCall { postApi.likePost(postId) }

    suspend fun unlikePost(postId: String): NetworkResult<SinglePostResponse> = safeApiCall { postApi.unlikePost(postId) }
}