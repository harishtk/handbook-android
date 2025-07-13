package com.handbook.app.feature.home.domain.model

import java.time.Instant

data class AccountEntry(
    val entryId: Long = 0,
    val title: String,
    val description: String? = null,
    val amount: Double,
    val entryType: EntryType,
    val transactionType: TransactionType,
    val transactionDate: Long = Instant.now().toEpochMilli(),
    val partyId: Long? = null,
    val categoryId: Long,
    val createdAt: Long = Instant.now().toEpochMilli(),
    val updatedAt: Long = Instant.now().toEpochMilli()
) {
    companion object {
        fun create(
            id: Long = 0,
            title: String,
            description: String? = null,
            amount: Double,
            entryType: EntryType,
            transactionType: TransactionType,
            transactionDate: Long = Instant.now().toEpochMilli(),
            partyId: Long? = null,
            categoryId: Long,
        ) = AccountEntry(
            entryId = id,
            title = title,
            description = description,
            amount = amount,
            entryType = entryType,
            transactionType = transactionType,
            transactionDate = transactionDate,
            partyId = partyId,
            categoryId = categoryId
        )
    }
}
