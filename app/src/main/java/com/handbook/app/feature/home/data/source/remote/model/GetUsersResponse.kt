package com.handbook.app.feature.home.data.source.remote.model

import com.google.gson.annotations.SerializedName
import com.handbook.app.feature.home.data.source.remote.dto.UserSummaryDto

data class GetUsersResponse(
    val statusCode: Int,
    val message: String,
    val data: Data?,
) {
    data class Data(
        @SerializedName("users")
        val users: List<UserSummaryDto>,
        @SerializedName("total")
        val total: Int,
        @SerializedName("page")
        val page: Int,
        @SerializedName("size")
        val pageSize: Int,
        @SerializedName("totalPages")
        val totalPages: Int,
        @SerializedName("isLastPage")
        val isLastPage: Boolean,
    )
}

