package com.handbook.app.feature.home.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import com.handbook.app.feature.home.data.source.local.converter.Converters
import com.handbook.app.feature.home.data.source.local.dao.AccountEntryDao
import com.handbook.app.feature.home.data.source.local.dao.AttachmentDao
import com.handbook.app.feature.home.data.source.local.dao.BankDao
import com.handbook.app.feature.home.data.source.local.dao.CategoryDao
import com.handbook.app.feature.home.data.source.local.dao.PartyDao
import com.handbook.app.feature.home.data.source.local.dao.SummaryDao
import com.handbook.app.feature.home.data.source.local.entity.AccountEntryEntity
import com.handbook.app.feature.home.data.source.local.entity.AccountEntryFtsEntity
import com.handbook.app.feature.home.data.source.local.entity.AttachmentEntity
import com.handbook.app.feature.home.data.source.local.entity.BankEntity
import com.handbook.app.feature.home.data.source.local.entity.CategoryEntity
import com.handbook.app.feature.home.data.source.local.entity.PartyEntity

@TypeConverters(Converters::class)
@Database(
    entities = [AccountEntryEntity::class, CategoryEntity::class, PartyEntity::class,
        AttachmentEntity::class, AccountEntryFtsEntity::class, BankEntity::class],
    version = 4,
    exportSchema = true
)
abstract class AccountsDatabase : RoomDatabase() {

    abstract fun accountEntryDao(): AccountEntryDao
    abstract fun categoryDao(): CategoryDao
    abstract fun partyDao(): PartyDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun bankDao(): BankDao
    abstract fun summaryDao(): SummaryDao

    class Factory {
        fun createInstance(appContext: Context): AccountsDatabase =
            Room.databaseBuilder(appContext, AccountsDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration(false)
                .addMigrations(
                    MIGRATION_1_2
                )
                .addCallback(RoomCallback)
                .build()
    }

    companion object {
        const val DATABASE_NAME = "Handbook.accounts.db"

        /* Table names */
        const val TABLE_PARTIES = "parties"
        const val TABLE_CATEGORIES = "categories"
        const val TABLE_ACCOUNT_ENTRIES = "account_entries"
        const val TABLE_ATTACHMENTS = "attachments"
        const val TABLE_BANKS = "banks"
        const val TABLE_ACCOUNT_ENTRIES_FTS = "account_entries_fts"

        private val RoomCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Pre-populate the database if required.
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                // Handle account based reset on destructive migration.
            }
        }
    }
}