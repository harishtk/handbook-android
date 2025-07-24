package com.handbook.app.feature.home.domain.model

data class Category(
    val id: Long,
    val name: String,
    val description: String?,
    val transactionType: TransactionType,
    val createdAt: Long,
    val updatedAt: Long,
) {
    companion object {
        fun create(
            id: Long = 0,
            name: String,
            transactionType: TransactionType = TransactionType.EXPENSE,
            description: String? = null,
        ) = Category(
            id = id,
            name = name,
            description = description,
            transactionType = transactionType,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
    }
}
