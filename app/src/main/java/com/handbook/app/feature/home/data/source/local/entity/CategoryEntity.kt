package com.handbook.app.feature.home.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.handbook.app.feature.home.data.source.local.AccountsDatabase
import com.handbook.app.feature.home.domain.model.Category
import java.time.Instant

@Entity(
    tableName = CategoryTable.NAME,
    indices = [
        Index(CategoryTable.Columns.NAME, unique = true),
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = CategoryTable.Columns.ID)
    var _id: Long? = null,

    @ColumnInfo(name = CategoryTable.Columns.NAME)
    val name: String,

    @ColumnInfo(name = CategoryTable.Columns.DESCRIPTION)
    val description: String? = null,

    @ColumnInfo(name = CategoryTable.Columns.CREATED_AT)
    val createdAt: Long = Instant.now().toEpochMilli(),

    @ColumnInfo(name = CategoryTable.Columns.UPDATED_AT)
    var updatedAt: Long = Instant.now().toEpochMilli(),
)

fun CategoryEntity.toCategory(): Category {
    return Category(
        id = _id ?: 0,
        name = name,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun Category.asEntity(): CategoryEntity {
    val isNew = this.id == 0L
    val currentTime = Instant.now().toEpochMilli()
    return CategoryEntity(
        _id = if (isNew) null else id,
        name = name,
        description = description,
        createdAt = if (isNew) currentTime else createdAt,
        updatedAt = updatedAt,
    )
}

object CategoryTable {
    const val NAME = AccountsDatabase.TABLE_CATEGORIES

    object Columns {
        const val ID            = "category_id"
        const val NAME          = "name"
        const val DESCRIPTION   = "description"
        const val CREATED_AT    = "created_at"
        const val UPDATED_AT    = "updated_at"
    }
}
