package com.handbook.app.feature.home.domain.model.request

import com.handbook.app.common.util.paging.PagedRequest

data class NotificationRequest(
    val pagedRequest: PagedRequest<Int>,
)