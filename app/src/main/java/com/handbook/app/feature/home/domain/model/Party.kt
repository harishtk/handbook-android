package com.handbook.app.feature.home.domain.model

import java.time.Instant

data class Party(
    val id: Long,
    val name: String,
    val contactNumber: String,
    val description: String?,
    val address: String?,
    val createdAt: Long,
    val updatedAt: Long,
) {
    companion object {
        fun create(
            name: String,
            contactNumber: String,
            description: String? = null,
            address: String? = null,
        ) = Party(
            id = 0,
            name = name,
            contactNumber = contactNumber,
            description = description,
            address = address,
            createdAt = Instant.now().toEpochMilli(),
            updatedAt = Instant.now().toEpochMilli(),
        )
    }
}
