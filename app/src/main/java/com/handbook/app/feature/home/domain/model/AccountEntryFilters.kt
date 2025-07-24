package com.handbook.app.feature.home.domain.model


enum class SortOption { // Keep SortOption or remove if not needed
    NEWEST_FIRST, OLDEST_FIRST, AMOUNT_HIGH_LOW, AMOUNT_LOW_HIGH
}

data class AccountEntryFilters(
    val categoryId: Long? = null,
    val party: Party? = null,
    val entryType: EntryType? = null,
    val transactionType: TransactionType? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val titleQuery: String? = null,
    val isPinned: Boolean? = null,
    val sortBy: SortOption? = SortOption.NEWEST_FIRST,
) {
    companion object {
        val None = AccountEntryFilters()
    }

    fun count(): Int {
        var i = 0
        if (categoryId != null) i++
        if (party != null) i++
        if (entryType != null) i++
        if (transactionType != null) i++
        if (startDate != null) i++
        if (endDate != null) i++
        if (titleQuery != null) i++
        if (isPinned != null) i++
        return i
    }
}
