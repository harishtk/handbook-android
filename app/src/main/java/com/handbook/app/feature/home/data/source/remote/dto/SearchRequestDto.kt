package com.handbook.app.feature.home.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import com.handbook.app.feature.home.domain.model.SearchType
import com.handbook.app.feature.home.domain.model.request.SearchRequest

data class SearchRequestDto(
    @SerializedName("q")
    val query: String,
    @SerializedName("page")
    val page: Int,
    @SerializedName("size")
    val pageSize: Int,
    @SerializedName("type")
    val type: SearchType
)

fun SearchRequest.asDto() = SearchRequestDto(
    query = query,
    type = type,
    page = pagedRequest.key ?: 0,
    pageSize = pagedRequest.loadSize
)

