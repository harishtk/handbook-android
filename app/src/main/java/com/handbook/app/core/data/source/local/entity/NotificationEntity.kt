package com.handbook.app.core.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.handbook.app.core.data.source.local.AppDatabase
import com.handbook.app.core.domain.model.HandbookNotification

@Entity(tableName = NotificationTable.name)
data class NotificationEntity(
    @ColumnInfo(name = "content")
    val content: String,
    @ColumnInfo(name = "timestamp")
    val timestamp: String,
    @ColumnInfo(name = "read")
    val read: Int,
    @ColumnInfo(name = "category")
    val category: String,
    @ColumnInfo(name = "subCategory")
    val subCategory: String,
    @ColumnInfo(name = "userId")
    val userId: String,
) {
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = false)
    var _id: Long? = null

    @ColumnInfo(name = "title")
    var title: String? = null

    @ColumnInfo(name = "shopId")
    var shopId: String? = null

    @ColumnInfo(name = "productId")
    var productId: String? = null
}

fun NotificationEntity.toAiaNotification(): HandbookNotification {
    return HandbookNotification(
        content = content,
        timestamp = timestamp,
        read = read == 1,
        category = category,
        subCategory = subCategory,
        userId = userId
    ).also {
        it.id = _id
        it.title = title
        it.productId = productId
        it.shopId = shopId
    }
}

object NotificationTable {
    const val name = AppDatabase.TABLE_NOTIFICATION

    object Columns {
        const val ID            = "id"
        const val CONTENT       = "content"
        const val TIMESTAMP     = "timestamp"
        const val READ          = "read"
        const val CATEGORY      = "category"
        const val SUB_CATEGORY  = "subCategory"
        const val TITLE         = "title"
    }
}