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
                else -> {
                    val t = IllegalStateException("Invalid EntryType: $value, selecting OTHER instead.")
                    Timber.w(t)
                    OTHER
                }
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
                else -> {
                    val t = IllegalStateException("Invalid TransactionType: $value, selecting TRANSFER instead.")
                    Timber.w(t)
                    TRANSFER
                }
            }
        }
    }
}