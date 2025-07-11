package com.handbook.app.feature.home.data.source.remote.model

import com.google.gson.annotations.SerializedName
import com.handbook.app.feature.home.data.source.remote.dto.PostDto
import com.handbook.app.feature.home.data.source.remote.dto.UserSummaryDto

data class PostFeedResponse(
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: PostFeedData?
) {
    data class PostFeedData(
        @SerializedName("posts")
        val posts: List<PostDto>,
        @SerializedName("users")
        val users: List<UserSummaryDto>,
        @SerializedName("page")
        val page: PageMetadata,
    )

    data class PageMetadata(
        @SerializedName("totalPosts")
        val totalPosts: Int,
        @SerializedName("totalPages")
        val totalPages: Int,
        @SerializedName("currentPage")
        val currentPage: Int,
        @SerializedName("pageSize")
        val pageSize: Int,
        @SerializedName("isLastPage")
        val isLastPage: Boolean
    )
}

