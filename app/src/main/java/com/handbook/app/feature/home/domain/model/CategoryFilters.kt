package com.handbook.app.feature.home.domain.model

data class CategoryFilters(
    val query: String = "",
    val transactionType: TransactionType? = null,
)
