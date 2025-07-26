package com.handbook.app.feature.home.domain.di

import android.content.Context
import com.handbook.app.feature.home.data.repository.LocalSummaryRepository
import com.handbook.app.feature.home.data.repository.LocalAccountsRepository
import com.handbook.app.feature.home.data.source.local.AccountsDatabase
import com.handbook.app.feature.home.domain.repository.AccountsRepository
import com.handbook.app.feature.home.domain.repository.SummaryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DatabaseModule {

    @Binds
    @Singleton
    fun bindsAccountsRepository(
        accountsRepository: LocalAccountsRepository
    ): AccountsRepository

    @Binds
    @Singleton
    fun bindsSummaryRepository(
        summaryRepository: LocalSummaryRepository
    ): SummaryRepository

    companion object {
        @Provides
        @Singleton
        fun provideAccountsDatabase(
            @ApplicationContext context: Context
        ): AccountsDatabase = AccountsDatabase.Factory().createInstance(context)

        // all daos
        @Provides
        fun provideAccountEntryDao(
            database: AccountsDatabase
        ) = database.accountEntryDao()

        @Provides
        fun provideCategoryDao(
            database: AccountsDatabase
        ) = database.categoryDao()

        @Provides
        fun providePartyDao(
            database: AccountsDatabase
        ) = database.partyDao()

        @Provides
        fun provideAttachmentDao(
            database: AccountsDatabase
        ) = database.attachmentDao()

        @Provides
        fun provideBankDao(
            database: AccountsDatabase
        ) = database.bankDao()

        @Provides
        fun provideSummaryDao(
            database: AccountsDatabase
        ) = database.summaryDao()
    }
}