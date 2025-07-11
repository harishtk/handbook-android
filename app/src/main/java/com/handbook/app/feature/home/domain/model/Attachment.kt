package com.handbook.app.feature.home.domain.model

import java.time.Instant

data class Attachment(
    val attachmentId: Long = 0,
    val entryId: Long,
    // Store URI as String
    val filePath: String,
    val fileName: String? = null,
    val mimeType: String? = null,
    val uploadedAt: Long = Instant.now().toEpochMilli(),
    val createdAt: Long = Instant.now().toEpochMilli()
)