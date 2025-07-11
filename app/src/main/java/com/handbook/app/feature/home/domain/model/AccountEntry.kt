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
)
