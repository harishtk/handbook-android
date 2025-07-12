package com.handbook.app.feature.home.data.source.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.handbook.app.feature.home.data.source.local.entity.PartyEntity
import com.handbook.app.feature.home.data.source.local.entity.PartyTable
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyDao {
    @Query(value = """
        SELECT * FROM ${PartyTable.NAME} 
        ORDER BY ${PartyTable.Columns.CREATED_AT} DESC
    """)
    fun partiesStream(): Flow<List<PartyEntity>>

    @Query(value = """
        SELECT * FROM ${PartyTable.NAME}
        WHERE (:query = '' OR ${PartyTable.Columns.NAME} LIKE '%' || :query || '%')
        ORDER BY ${PartyTable.Columns.CREATED_AT} DESC
    """)
    fun partiesPagingSource(query: String): PagingSource<Int, PartyEntity>

    @Query(value = """
        SELECT * FROM ${PartyTable.NAME} 
        WHERE ${PartyTable.Columns.NAME} LIKE '%' || :query || '%' 
        ORDER BY ${PartyTable.Columns.CREATED_AT} DESC
    """)
    fun partiesStream(query: String): Flow<List<PartyEntity>>

    @Query(value = """
        SELECT * FROM ${PartyTable.NAME} 
        WHERE ${PartyTable.Columns.ID} = :id
    """)
    suspend fun getParty(id: Long): PartyEntity?

    @Query(value = """
        SELECT * FROM ${PartyTable.NAME} 
        WHERE ${PartyTable.Columns.ID} = :id
    """)
    fun observeParty(id: Long): Flow<PartyEntity?>

    @Upsert
    fun insert(party: PartyEntity): Long

    @Upsert
    fun upsertAll(parties: List<PartyEntity>)

    @Delete
    fun delete(party: PartyEntity): Int

    @Query(value = "DELETE FROM ${PartyTable.NAME}")
    fun deleteAll(): Int
}