package com.handbook.app.feature.home.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.handbook.app.feature.home.data.source.local.dao.AccountEntryDao
import com.handbook.app.feature.home.data.source.local.dao.AttachmentDao
import com.handbook.app.feature.home.data.source.local.dao.CategoryDao
import com.handbook.app.feature.home.data.source.local.dao.PartyDao
import com.handbook.app.feature.home.data.source.local.entity.AccountEntryEntity
import com.handbook.app.feature.home.data.source.local.entity.AccountEntryFtsEntity
import com.handbook.app.feature.home.data.source.local.entity.AttachmentEntity
import com.handbook.app.feature.home.data.source.local.entity.CategoryEntity
import com.handbook.app.feature.home.data.source.local.entity.PartyEntity

@Database(
    entities = [AccountEntryEntity::class, CategoryEntity::class, PartyEntity::class,
        AttachmentEntity::class, AccountEntryFtsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AccountsDatabase : RoomDatabase() {

    abstract fun accountEntryDao(): AccountEntryDao
    abstract fun categoryDao(): CategoryDao
    abstract fun partyDao(): PartyDao
    abstract fun attachmentDao(): AttachmentDao

    class Factory {
        fun createInstance(appContext: Context): AccountsDatabase =
            Room.databaseBuilder(appContext, AccountsDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
    }

    companion object {
        private const val DATABASE_NAME = "Handbook.accounts.db"

        /* Table names */
        const val TABLE_PARTIES = "parties"
        const val TABLE_CATEGORIES = "categories"
        const val TABLE_ACCOUNT_ENTRIES = "account_entries"
        const val TABLE_ATTACHMENTS = "attachments"
        const val TABLE_ACCOUNT_ENTRIES_FTS = "account_entries_fts"
    }
}