package com.handbook.app.feature.home.data.source.local.entity

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.handbook.app.core.designsystem.component.forms.MediaType
import com.handbook.app.feature.home.data.source.local.AccountsDatabase
import com.handbook.app.feature.home.domain.model.Attachment
import java.time.Instant
import androidx.core.net.toUri

@Entity(
    tableName = AttachmentTable.NAME,
    foreignKeys = [
        ForeignKey(
            entity = AccountEntryEntity::class,
            parentColumns = [AccountEntryTable.Columns.ID],
            childColumns = [AttachmentTable.Columns.ENTRY_ID],
            onDelete = ForeignKey.CASCADE // Delete attachments if the entry is deleted
        )
    ],
    indices = [Index(AttachmentTable.Columns.ENTRY_ID)]
)
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = AttachmentTable.Columns.ID)
    val attachmentId: Long = 0,

    @ColumnInfo(name = AttachmentTable.Columns.ENTRY_ID)
    val entryId: Long,

    // Store URI as String
    @ColumnInfo(name = AttachmentTable.Columns.FILE_PATH)
    val filePath: String,

    @ColumnInfo(name = AttachmentTable.Columns.FILE_NAME)
    val fileName: String? = null,

    @ColumnInfo(name = AttachmentTable.Columns.MIME_TYPE)
    val mimeType: String? = null,

    @ColumnInfo(name = AttachmentTable.Columns.UPLOADED_AT)
    val uploadedAt: Long? = null,

    @ColumnInfo(name = AttachmentTable.Columns.CREATED_AT)
    val createdAt: Long = Instant.now().toEpochMilli()
)

fun AttachmentEntity.toAttachment(): Attachment {
    return Attachment(
        attachmentId = attachmentId,
        entryId = entryId,
        uri = filePath.toUri(),
        filePath = filePath,
        fileName = fileName,
        contentMediaType = MediaType.fromMimeType(mimeType),
        uploadedAt = uploadedAt,
        createdAt = createdAt,
    )
}

fun Attachment.toAttachmentEntity(): AttachmentEntity {
    return AttachmentEntity(
        attachmentId = attachmentId,
        entryId = entryId,
        filePath = uri.toString(),
        fileName = fileName,
        mimeType = contentMediaType.mimeType,
        uploadedAt = uploadedAt,
        createdAt = createdAt,
    )
}

object AttachmentTable {
    const val NAME = AccountsDatabase.TABLE_ATTACHMENTS

    object Columns {
        const val ID = "attachment_id"
        const val ENTRY_ID = "fk_entry_id"
        const val FILE_PATH = "file_path"
        const val FILE_NAME = "file_name"
        const val MIME_TYPE = "mime_type"
        const val UPLOADED_AT = "uploaded_at"
        const val CREATED_AT = "created_at"
    }
}