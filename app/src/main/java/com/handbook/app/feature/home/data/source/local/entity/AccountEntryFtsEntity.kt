package com.handbook.app.feature.home.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

// FTS table for AccountEntryEntity
@Entity(tableName = AccountEntryFtsTable.NAME)
@Fts4(contentEntity = AccountEntryEntity::class) // Or Fts3
data class AccountEntryFtsEntity(
    @ColumnInfo(name = AccountEntryFtsTable.Columns.TITLE) // Must match contentEntity column name
    val title: String,
    @ColumnInfo(name = AccountEntryFtsTable.Columns.DESCRIPTION)
    val description: String?,
)

object AccountEntryFtsTable {
    const val NAME = "account_entries_fts"

    object Columns {
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        // Note: FTS tables don't have PrimaryKey in the usual sense for the FTS entity definition
        // The rowid from the contentEntity is used implicitly.
    }
}