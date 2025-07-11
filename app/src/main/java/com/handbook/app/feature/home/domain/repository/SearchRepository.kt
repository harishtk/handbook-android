package com.handbook.app.feature.home.domain.repository

import com.handbook.app.core.util.Result
import com.handbook.app.feature.home.domain.model.SearchResultData
import com.handbook.app.feature.home.domain.model.request.SearchRequest

interface SearchRepository {

    suspend fun search(request: SearchRequest): Result<SearchResultData>
}