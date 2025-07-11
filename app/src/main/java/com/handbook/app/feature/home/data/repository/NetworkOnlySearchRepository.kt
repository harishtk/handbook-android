package com.handbook.app.feature.home.data.repository

import com.handbook.app.core.util.NetworkResult
import com.handbook.app.core.util.NetworkResultParser
import com.handbook.app.core.util.Result
import com.handbook.app.feature.home.data.source.remote.SearchRemoteDataSource
import com.handbook.app.feature.home.data.source.remote.dto.asDto
import com.handbook.app.feature.home.data.source.remote.model.toSearchResultData
import com.handbook.app.feature.home.domain.model.SearchResultData
import com.handbook.app.feature.home.domain.model.request.SearchRequest
import com.handbook.app.feature.home.domain.repository.SearchRepository
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection

class NetworkOnlySearchRepository @Inject constructor(
    private val remoteDataSource: SearchRemoteDataSource,
) : SearchRepository, NetworkResultParser {

    override suspend fun search(request: SearchRequest): Result<SearchResultData> {
        return when (val networkResult = remoteDataSource.search(request.asDto())) {
            is NetworkResult.Success -> {
                if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_OK) {
                    Result.Success(networkResult.data.data!!.toSearchResultData())
                } else {
                    badResponse(networkResult)
                }
            }
            else -> parseErrorNetworkResult(networkResult)
        }
    }
}