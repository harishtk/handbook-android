package com.handbook.app.feature.home.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.handbook.app.feature.home.data.source.local.AccountsDatabase
import com.handbook.app.feature.home.domain.model.Party
import java.time.Instant

@Entity(tableName = PartyTable.NAME)
data class PartyEntity(
    @ColumnInfo(name = PartyTable.Columns.NAME)
    val name: String,

    @ColumnInfo(name = PartyTable.Columns.ID)
    @PrimaryKey(autoGenerate = true)
    var _id: Long? = null,

    @ColumnInfo(name = PartyTable.Columns.CONTACT_NUMBER)
    val contactNumber: String? = null,

    @ColumnInfo(name = PartyTable.Columns.DESCRIPTION)
    var description: String? = null,

    @ColumnInfo(name = PartyTable.Columns.ADDRESS)
    var address: String? = null,

    @ColumnInfo(name = PartyTable.Columns.CREATED_AT)
    val createdAt: Long = Instant.now().toEpochMilli(),

    @ColumnInfo(name = PartyTable.Columns.UPDATED_AT)
    var updatedAt: Long = Instant.now().toEpochMilli()
)

fun PartyEntity.toParty(): Party {
    return Party(
        id = _id ?: 0,
        name = name,
        contactNumber = contactNumber,
        description = description,
        address = address,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun Party.asEntity(): PartyEntity {
    return PartyEntity(
        name = name,
        _id = if (id == 0L) null else id,
        contactNumber = contactNumber,
        description = this@asEntity.description,
        address = this@asEntity.address,
        updatedAt = this@asEntity.updatedAt,
        createdAt = if (id == 0L) Instant.now().toEpochMilli() else this@asEntity.createdAt
    )
}

object PartyTable {
    const val NAME = AccountsDatabase.TABLE_PARTIES

    object Columns {
        const val ID                = "party_id"
        const val NAME              = "name"
        const val CONTACT_NUMBER    = "contact_number"
        const val ADDRESS           = "address"
        const val DESCRIPTION       = "description"
        const val CREATED_AT        = "created_at"
        const val UPDATED_AT        = "updated_at"
    }
}