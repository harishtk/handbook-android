package com.handbook.app.feature.home.data.source.remote

import com.google.gson.Gson
import okhttp3.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.POST
import com.handbook.app.BuildConfig
import com.handbook.app.core.data.source.remote.BaseRemoteDataSource
import com.handbook.app.core.util.NetworkMonitor
import com.handbook.app.core.util.NetworkResult
import com.handbook.app.feature.home.data.source.remote.dto.SearchRequestDto
import com.handbook.app.feature.home.data.source.remote.model.SearchResponse
import timber.log.Timber
import javax.inject.Inject

const val SEARCH_API_URL = BuildConfig.API_URL

class SearchRemoteDataSource @Inject constructor(
    networkHelper: NetworkMonitor,
    gson: Gson,
    okhttpCallFactory: dagger.Lazy<Call.Factory>
) : BaseRemoteDataSource(networkHelper) {

    private val searchApi = Retrofit.Builder()
        .baseUrl(SEARCH_API_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .callFactory { okhttpCallFactory.get().newCall(it) }
        .build()
        .create<SearchApi>()

    suspend fun search(searchRequestDto: SearchRequestDto): NetworkResult<SearchResponse> = safeApiCall {
        searchApi.search(searchRequestDto)
    }
}

private interface SearchApi {
    @POST("search")
    suspend fun search(@Body searchRequestDto: SearchRequestDto): Response<SearchResponse>
}