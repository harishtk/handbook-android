package com.handbook.app.feature.home.domain.usecase

import com.handbook.app.common.util.paging.PagedRequest
import com.handbook.app.feature.home.domain.model.PostsWithUsers
import com.handbook.app.feature.home.domain.repository.PostRepository
import javax.inject.Inject

class GetGlobalFeedUseCase @Inject constructor(
    private val postRepository: PostRepository,
) {
    suspend operator fun invoke(pagedRequest: PagedRequest<Int>): PostsWithUsers {
        return PostsWithUsers(emptyList(), emptyList(), null)
    }
}