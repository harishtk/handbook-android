package com.handbook.app.feature.home.data.repository

import com.handbook.app.common.util.paging.PagedRequest
import com.handbook.app.core.net.ApiException
import com.handbook.app.core.util.NetworkResult
import com.handbook.app.core.util.NetworkResultParser
import com.handbook.app.core.util.Result
import com.handbook.app.feature.home.data.source.remote.PostRemoteDataSource
import com.handbook.app.feature.home.data.source.remote.dto.PostDto
import com.handbook.app.feature.home.data.source.remote.dto.UserSummaryDto
import com.handbook.app.feature.home.data.source.remote.dto.asDto
import com.handbook.app.feature.home.data.source.remote.dto.toPost
import com.handbook.app.feature.home.data.source.remote.dto.toUserSummary
import com.handbook.app.feature.home.data.source.remote.model.PostFeedResponse
import com.handbook.app.feature.home.data.source.remote.model.SinglePostResponse
import com.handbook.app.feature.home.domain.model.PostsWithUsers
import com.handbook.app.feature.home.domain.model.request.CreatePostRequest
import com.handbook.app.feature.home.domain.repository.PostRepository
import com.handbook.app.feature.home.domain.util.PostNotFoundException
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection

class NetworkOnlyPostRepository @Inject constructor(
    private val remoteDataSource: PostRemoteDataSource,
) : PostRepository, NetworkResultParser {

    override suspend fun globalFeed(request: PagedRequest<Int>): Result<PostsWithUsers> {
        val page = request.key ?: 0
        val pageSize = request.loadSize
        return parseFeedResponse(remoteDataSource.getGlobalFeed(page, pageSize))
    }

    override suspend fun getPrivateFeed(request: PagedRequest<Int>): Result<PostsWithUsers> {
        val page = request.key ?: 0
        val pageSize = request.loadSize
        return parseFeedResponse(remoteDataSource.getPrivateFeed(page, pageSize))
    }

    override suspend fun getPostsByAuthorId(
        authorId: String,
        request: PagedRequest<Int>
    ): Result<PostsWithUsers> {
        val page = request.key ?: 0
        val pageSize = request.loadSize
        return parseFeedResponse(remoteDataSource.getPostsByAuthorId(authorId, page, pageSize))
    }

    override suspend fun createPost(request: CreatePostRequest): Result<PostsWithUsers> {
        return when (val networkResult = remoteDataSource.createPost(request.asDto())) {
            is NetworkResult.Success -> {
                if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_CREATED) {
                    if (networkResult.data.data != null) {
                        val data = networkResult.data.data
                        Result.Success(
                            PostsWithUsers(
                                posts = listOf(data.post.toPost()),
                                users = data.users.map(UserSummaryDto::toUserSummary),
                                nextPagingKey = null
                            )
                        )
                    } else {
                        emptyResponse(networkResult)
                    }
                } else {
                    badResponse(networkResult)
                }
            }

            else -> {
                parseErrorNetworkResult(networkResult)
            }
        }
    }

    override suspend fun getPostById(postId: String): Result<PostsWithUsers> {
        return parseSinglePostResponse(remoteDataSource.getPostById(postId))
    }

    override suspend fun likePost(postId: String): Result<PostsWithUsers> {
        return parseSinglePostResponse(remoteDataSource.likePost(postId))
    }

    override suspend fun unlikePost(postId: String): Result<PostsWithUsers> {
        return parseSinglePostResponse(remoteDataSource.unlikePost(postId))
    }

    private fun parseFeedResponse(networkResult: NetworkResult<PostFeedResponse>): Result<PostsWithUsers> {
        return when (networkResult) {
            is NetworkResult.Success -> {
                if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_OK) {
                    if (networkResult.data.data != null) {
                        val data = networkResult.data.data

                        // Flatten the users from posts
                        val users = data.users
                            .distinctBy { it }
                            .map(UserSummaryDto::toUserSummary)
                        // TODO: --build profile picture url here--

                        val posts = data.posts.map(PostDto::toPost)

                        val nextPagingKey = if (data.page.isLastPage.not()) {
                            data.page.currentPage + 1
                        } else {
                            null
                        }
                        Result.Success(
                            PostsWithUsers(
                                posts = posts,
                                users = users,
                                nextPagingKey = nextPagingKey
                            )
                        )
                    } else {
                        emptyResponse(networkResult)
                    }
                } else {
                    badResponse(networkResult)
                }
            }

            else -> {
                parseErrorNetworkResult(networkResult)
            }
        }
    }

    private fun parseSinglePostResponse(networkResult: NetworkResult<SinglePostResponse>): Result<PostsWithUsers> {
        return when (networkResult) {
            is NetworkResult.Success -> {
                if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_OK) {
                    if (networkResult.data.data != null) {
                        val data = networkResult.data.data
                        return Result.Success(
                            PostsWithUsers(
                                posts = listOf(data.post.toPost()),
                                users = data.users.map(UserSummaryDto::toUserSummary),
                                nextPagingKey = null
                            )
                        )
                    } else {
                        emptyResponse(networkResult)
                    }
                } else {
                    badResponse(networkResult)
                }
            }

            else -> {
                when (networkResult.code) {
                    HttpsURLConnection.HTTP_NOT_FOUND -> {
                        val cause = PostNotFoundException("Post not found")
                        Result.Error(ApiException(cause))
                    }

                    else -> parseErrorNetworkResult(networkResult)
                }
            }
        }
    }
}