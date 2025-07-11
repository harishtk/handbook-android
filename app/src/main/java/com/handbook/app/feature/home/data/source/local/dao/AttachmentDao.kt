package com.handbook.app.feature.home.data.source.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.handbook.app.feature.home.data.source.local.entity.AttachmentEntity
import com.handbook.app.feature.home.data.source.local.entity.AttachmentTable
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {
    @Query(
        value = """
        SELECT * FROM ${AttachmentTable.NAME}
        ORDER BY ${AttachmentTable.Columns.CREATED_AT} DESC
    """
    )
    fun attachmentsStream(): Flow<List<AttachmentEntity>>

    @Query(
        value = """
        SELECT * FROM ${AttachmentTable.NAME}
        WHERE ${AttachmentTable.Columns.ID} = :id
    """
    )
    suspend fun getAttachment(id: Long): AttachmentEntity?

    @Upsert
    fun upsertAll(attachments: List<AttachmentEntity>)

    @Query(value = "DELETE FROM ${AttachmentTable.NAME} WHERE ${AttachmentTable.Columns.ID} = :id")
    suspend fun deleteById(id: Long)

    @Query(value = "DELETE FROM ${AttachmentTable.NAME}")
    fun deleteAll()
}