package com.handbook.app.feature.home.domain.model

data class SearchResultData(
    val users: List<UserSummary>,
    val posts: List<PostSummary>,
    val totalUsers: Int,
    val totalPosts: Int
)
