package com.handbook.app.feature.home.domain.model

data class AccountEntryWithDetails(
    val entry: AccountEntry,
    val category: Category,
    val party: Party? = null,
    val bank: Bank? = null,
    val attachments: List<Attachment> = emptyList()
)

