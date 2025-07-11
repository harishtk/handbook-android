package com.handbook.app.feature.home.data.source.local.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.handbook.app.feature.home.data.source.local.entity.AccountEntryEntity

data class SearchedAccountEntry(
    @Embedded
    val entry: AccountEntryEntity,

    // If you want to include category name directly from the join
    @ColumnInfo(name = "category_name") // Assuming CategoryEntity has a 'name' column
    val categoryName: String,
)