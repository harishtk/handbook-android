package com.handbook.app.feature.home.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.handbook.app.feature.home.data.source.local.AccountsDatabase
import com.handbook.app.feature.home.domain.model.Bank
import java.time.Instant

@Entity(
    tableName = BankTable.NAME,
    indices = [
        Index(BankTable.Columns.NAME, unique = true),
    ]
)
data class BankEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = BankTable.Columns.ID)
    var _id: Long? = null,

    @ColumnInfo(name = BankTable.Columns.NAME)
    val name: String,

    @ColumnInfo(name = BankTable.Columns.DESCRIPTION)
    val description: String? = null,

    @ColumnInfo(name = BankTable.Columns.CREATED_AT)
    val createdAt: Long = Instant.now().toEpochMilli(),

    @ColumnInfo(name = BankTable.Columns.UPDATED_AT)
    var updatedAt: Long = Instant.now().toEpochMilli(),
)

fun BankEntity.toBank(): Bank {
    return Bank(
        id = _id ?: 0,
        name = name,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun Bank.asEntity(): BankEntity {
    val isNew = this.id == 0L
    val currentTime = Instant.now().toEpochMilli()
    return BankEntity(
        _id = if (isNew) null else id,
        name = name,
        description = description,
        createdAt = if (isNew) currentTime else createdAt,
        updatedAt = updatedAt,
    )
}

object BankTable {
    const val NAME = AccountsDatabase.TABLE_BANKS

    object Columns {
        const val ID            = "bank_id"
        const val NAME          = "name"
        const val DESCRIPTION   = "description"
        const val CREATED_AT    = "created_at"
        const val UPDATED_AT    = "updated_at"
    }
}

