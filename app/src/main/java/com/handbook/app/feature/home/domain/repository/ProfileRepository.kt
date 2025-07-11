package com.handbook.app.feature.home.domain.repository

import com.handbook.app.core.util.Result
import com.handbook.app.feature.home.domain.model.UserProfile

interface ProfileRepository {

    suspend fun getOwnUserProfile(): Result<UserProfile>

    suspend fun getUserProfile(userId: String): Result<UserProfile>

    suspend fun followUser(userId: String): Result<Unit>

    suspend fun unfollowUser(userId: String): Result<Unit>
}