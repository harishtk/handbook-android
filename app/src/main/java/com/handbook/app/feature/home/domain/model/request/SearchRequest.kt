package com.handbook.app.feature.home.domain.model.request

import com.handbook.app.common.util.paging.PagedRequest
import com.handbook.app.feature.home.domain.model.SearchType

data class SearchRequest(
    val query: String,
    val type: SearchType,
    val pagedRequest: PagedRequest<Int>
)
