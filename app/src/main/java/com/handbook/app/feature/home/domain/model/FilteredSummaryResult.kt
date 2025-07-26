package com.handbook.app.feature.home.domain.model

data class FilteredSummaryResult(
    val entries: List<AccountEntryWithDetails>,
    val totalIncome: Double,
    val totalExpenses: Double,
    val balance: Double
)
