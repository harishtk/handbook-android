package com.handbook.app.feature.home.domain.repository

import com.handbook.app.common.util.paging.PagedRequest
import com.handbook.app.core.util.Result
import com.handbook.app.feature.home.domain.model.PostsWithUsers
import com.handbook.app.feature.home.domain.model.request.CreatePostRequest

interface PostRepository {

    suspend fun globalFeed(request: PagedRequest<Int>): Result<PostsWithUsers>

    suspend fun getPrivateFeed(request: PagedRequest<Int>): Result<PostsWithUsers>

    suspend fun getPostsByAuthorId(authorId: String, request: PagedRequest<Int>): Result<PostsWithUsers>

    suspend fun createPost(request: CreatePostRequest): Result<PostsWithUsers>

    suspend fun getPostById(postId: String): Result<PostsWithUsers>

    suspend fun likePost(postId: String): Result<PostsWithUsers>

    suspend fun unlikePost(postId: String): Result<PostsWithUsers>

}