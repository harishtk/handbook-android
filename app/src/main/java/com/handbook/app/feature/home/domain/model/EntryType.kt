package com.handbook.app.feature.home.domain.model

// Enum for EntryType (consider defining this in a separate file or within the entity)
enum class EntryType {
    CASH, BANK, OTHER
}

// Enum for TransactionType
enum class TransactionType {
    INCOME, EXPENSE, TRANSFER // Added TRANSFER as it's common
}