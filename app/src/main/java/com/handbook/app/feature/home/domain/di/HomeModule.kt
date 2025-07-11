package com.handbook.app.feature.home.domain.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.handbook.app.feature.home.data.repository.DefaultNotificationRepository
import com.handbook.app.feature.home.data.repository.NetworkOnlyPostRepository
import com.handbook.app.feature.home.data.repository.NetworkOnlySearchRepository
import com.handbook.app.feature.home.data.repository.NetworkOnlyUserRepository
import com.handbook.app.feature.home.domain.repository.NotificationRepository
import com.handbook.app.feature.home.domain.repository.PostRepository
import com.handbook.app.feature.home.domain.repository.SearchRepository
import com.handbook.app.feature.home.domain.repository.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface HomeModule {

    @Binds
    @Singleton
    fun bindUserRepository(
        repository: NetworkOnlyUserRepository
    ): UserRepository

    @Binds
    @Singleton
    fun bindPostRepository(
        repository: NetworkOnlyPostRepository
    ): PostRepository

    @Binds
    @Singleton
    fun bindSearchRepository(
        repository: NetworkOnlySearchRepository
    ): SearchRepository

    @Binds
    @Singleton
    fun bindNotificationRepository(
        repository: DefaultNotificationRepository
    ): NotificationRepository
}