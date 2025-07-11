package com.handbook.app.feature.home.domain.model.request

import com.handbook.app.common.util.paging.PagedRequest

data class GetUsersRequest(
    val otherUserId: String?,
    val sortBy: String,
    val pagedRequest: PagedRequest<Int>,
)
