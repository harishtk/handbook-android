package com.handbook.app.feature.home.domain.repository

import com.handbook.app.common.util.paging.PagedData
import com.handbook.app.core.util.Result
import com.handbook.app.feature.home.domain.model.UserSummary
import com.handbook.app.feature.home.domain.model.UserProfile
import com.handbook.app.feature.home.domain.model.request.GetUsersRequest

interface UserRepository {

    suspend fun getUser(userId: String): Result<UserProfile>

    suspend fun getOwnUser(): Result<UserProfile>

    suspend fun getUsers(request: GetUsersRequest): Result<PagedData<Int, UserSummary>>

    suspend fun followUser(userId: String): Result<UserProfile>

    suspend fun unfollowUser(userId: String): Result<UserProfile>

    suspend fun getFollowing(request: GetUsersRequest): Result<PagedData<Int, UserSummary>>

    suspend fun getFollowers(request: GetUsersRequest): Result<PagedData<Int, UserSummary>>

}