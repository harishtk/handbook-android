package com.handbook.app.feature.home.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.handbook.app.feature.home.data.source.local.AccountsDatabase
import com.handbook.app.feature.home.domain.model.AccountEntry
import com.handbook.app.feature.home.domain.model.EntryType
import com.handbook.app.feature.home.domain.model.TransactionType
import java.time.Instant

object AccountEntryTable {
    const val NAME = AccountsDatabase.TABLE_ACCOUNT_ENTRIES
    object Columns {
        const val ID                 = "entry_id"
        const val TITLE              = "title"
        const val DESCRIPTION        = "description"
        const val AMOUNT             = "amount"
        const val ENTRY_TYPE         = "entry_type"
        const val TRANSACTION_TYPE   = "transaction_type"
        const val TRANSACTION_DATE   = "transaction_date"
        const val FK_PARTY_ID        = "fk_party_id"
        const val FK_CATEGORY_ID     = "fk_category_id"
        const val FK_BANK_ID         = "fk_bank_id"
        const val CREATED_AT         = "created_at"
        const val UPDATED_AT         = "updated_at"
    }
}

@Entity(
    tableName = AccountEntryTable.NAME,
    foreignKeys = [
        ForeignKey(
            entity = PartyEntity::class,
            parentColumns = [PartyTable.Columns.ID],
            childColumns = [AccountEntryTable.Columns.FK_PARTY_ID],
            onDelete = ForeignKey.SET_NULL // Or RESTRICT, CASCADE as per your needs
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = [CategoryTable.Columns.ID],
            childColumns = [AccountEntryTable.Columns.FK_CATEGORY_ID],
            onDelete = ForeignKey.RESTRICT // Don't delete entry if category is deleted, or handle it
        ),
        ForeignKey(
            entity = BankEntity::class,
            parentColumns = [BankTable.Columns.ID],
            childColumns = [AccountEntryTable.Columns.FK_BANK_ID],
            onDelete = ForeignKey.SET_NULL // Or RESTRICT, CASCADE as per your needs
        )
    ],
    indices = [
        Index(AccountEntryTable.Columns.FK_PARTY_ID),
        Index(AccountEntryTable.Columns.FK_CATEGORY_ID),
        Index(AccountEntryTable.Columns.FK_BANK_ID),
        Index(AccountEntryTable.Columns.TRANSACTION_DATE),
        Index(AccountEntryTable.Columns.ENTRY_TYPE),
        Index(AccountEntryTable.Columns.TRANSACTION_TYPE)
    ]
)
data class AccountEntryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = AccountEntryTable.Columns.ID)
    val entryId: Long = 0,

    @ColumnInfo(name = AccountEntryTable.Columns.TITLE)
    val title: String,

    @ColumnInfo(name = AccountEntryTable.Columns.DESCRIPTION)
    val description: String? = null,

    @ColumnInfo(name = AccountEntryTable.Columns.AMOUNT)
    val amount: Double, // Positive for income, negative for expense

    @ColumnInfo(name = AccountEntryTable.Columns.ENTRY_TYPE)
    val entryType: EntryType, // Use the enum

    @ColumnInfo(name = AccountEntryTable.Columns.TRANSACTION_TYPE)
    val transactionType: TransactionType, // Use the enum

    @ColumnInfo(name = AccountEntryTable.Columns.TRANSACTION_DATE)
    val transactionDate: Long = Instant.now().toEpochMilli(),

    @ColumnInfo(name = AccountEntryTable.Columns.FK_PARTY_ID)
    val partyId: Long? = null, // Nullable foreign key

    @ColumnInfo(name = AccountEntryTable.Columns.FK_CATEGORY_ID)
    val categoryId: Long, // Non-null foreign key

    @ColumnInfo(name = AccountEntryTable.Columns.FK_BANK_ID)
    val bankId: Long? = null, // Nullable foreign key

    @ColumnInfo(name = AccountEntryTable.Columns.CREATED_AT)
    val createdAt: Long = Instant.now().toEpochMilli(),

    @ColumnInfo(name = AccountEntryTable.Columns.UPDATED_AT)
    val updatedAt: Long = Instant.now().toEpochMilli()
)

fun AccountEntryEntity.toAccountEntry(): AccountEntry {
    return AccountEntry(
        entryId = entryId,
        title = title,
        description = description,
        amount = amount,
        entryType = entryType,
        transactionType = transactionType,
        transactionDate = transactionDate,
        partyId = partyId,
        categoryId = categoryId,
        bankId = bankId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun AccountEntry.asEntity(): AccountEntryEntity {
    val isNew = (this.entryId == 0L)
    val currentTime = Instant.now().toEpochMilli()

    return AccountEntryEntity(
        entryId = this.entryId,
        title = this.title,
        description = this.description,
        amount = this.amount,
        entryType = this.entryType,
        transactionType = this.transactionType,
        transactionDate = this.transactionDate,
        partyId = this.partyId,
        categoryId = this.categoryId,
        bankId = this.bankId,
        createdAt = if (isNew) currentTime else this.createdAt,
        updatedAt = currentTime
    )
}
