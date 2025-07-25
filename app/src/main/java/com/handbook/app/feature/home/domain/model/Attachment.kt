package com.handbook.app.feature.home.domain.model

import android.net.Uri
import com.handbook.app.core.designsystem.component.forms.MediaType
import java.io.File
import java.time.Instant

data class Attachment(
    val attachmentId: Long = 0,
    val entryId: Long,
    // Store URI as String
    val uri: Uri = Uri.EMPTY,
    val filePath: String,
    val fileName: String? = null,
    val contentMediaType: MediaType = MediaType.Unknown,
    val uploadedAt: Long? = null,
    val createdAt: Long = Instant.now().toEpochMilli(),

    val width: Int = 0,
    val height: Int = 0,
    val duration: Long = 0,
    val cachedFile: File? = null,
) {
    companion object {
        fun create(
            entryId: Long,
            uri: Uri,
            filePath: String,
            fileName: String? = null,
            contentMediaType: MediaType = MediaType.Unknown,
            uploadedAt: Long? = null,
            width: Int = 0,
            height: Int = 0,
            duration: Long = 0,
        ) = Attachment(
            entryId = entryId,
            uri = uri,
            filePath = filePath,
            fileName = fileName,
            contentMediaType = contentMediaType,
            uploadedAt = uploadedAt,
            width = width,
            height = height,
            duration = duration,
        )
    }
}