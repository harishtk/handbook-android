package com.handbook.app.feature.home.domain.model

data class AccountEntryFilters(
    val categoryId: Long? = null,
    val partyId: Long? = null,
    val entryType: EntryType? = null,
    val transactionType: TransactionType? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val titleQuery: String? = null
)
