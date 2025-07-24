package com.handbook.app.feature.home.domain.model

import timber.log.Timber

// Enum for EntryType (consider defining this in a separate file or within the entity)
enum class EntryType {
    CASH, BANK, OTHER;

    companion object {
        fun fromString(value: String): EntryType {
            return when (value) {
                "CASH" -> CASH
                "BANK" -> BANK
                "OTHER" -> OTHER
                else -> throw IllegalArgumentException("Invalid EntryType: $value")
            }
        }
    }
}

// Enum for TransactionType
enum class TransactionType {
    INCOME, EXPENSE, TRANSFER; // Added TRANSFER as it's common

    companion object {
        fun fromString(value: String): TransactionType {
            return when (value) {
                "INCOME" -> INCOME
                "EXPENSE" -> EXPENSE
                "TRANSFER" -> TRANSFER
                else -> throw IllegalArgumentException("Invalid TransactionType: $value")
            }
        }
    }
}