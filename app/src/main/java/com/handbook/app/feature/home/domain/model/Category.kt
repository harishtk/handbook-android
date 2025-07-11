package com.handbook.app.feature.home.domain.model

data class Category(
    val id: Long,
    val name: String,
    val description: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
