package com.handbook.app.core.data.source.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.handbook.app.core.data.source.local.entity.NotificationEntity
import com.handbook.app.core.data.source.local.entity.NotificationTable
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Query(value = """
        SELECT * FROM ${NotificationTable.name} 
        ORDER BY ${NotificationTable.Columns.TIMESTAMP} DESC
    """)
    fun notificationStream(): Flow<List<NotificationEntity>>

    @Query(value = """
        SELECT COUNT(*) FROM ${NotificationTable.name} 
        WHERE ${NotificationTable.Columns.READ} = 0
    """)
    fun unreadCountStream(): Flow<Int>

    @Upsert
    fun upsertAll(notifications: List<NotificationEntity>)

    @Query(value = """
        UPDATE ${NotificationTable.name} 
        SET ${NotificationTable.Columns.READ} = :readStatus 
        WHERE ${NotificationTable.Columns.ID} = :id
    """)
    fun markNotificationRead(id: Long, readStatus: Int)

    @Query(value = """
        UPDATE ${NotificationTable.name} 
        SET ${NotificationTable.Columns.READ} = :readStatus
    """)
    fun markAllNotificationRead(readStatus: Int)

    @Query(value = "DELETE FROM ${NotificationTable.name}")
    fun deleteAll()

}