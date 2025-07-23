package com.handbook.app.feature.home.data.source.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.handbook.app.feature.home.data.source.local.entity.BankEntity
import com.handbook.app.feature.home.data.source.local.entity.BankTable
import kotlinx.coroutines.flow.Flow

@Dao
interface BankDao {
    @Query(
        value = """
        SELECT * FROM ${BankTable.NAME}
        ORDER BY ${BankTable.Columns.CREATED_AT} DESC
    """
    )
    fun banksStream(): Flow<List<BankEntity>>

    @Query(
        value = """
        SELECT * FROM ${BankTable.NAME}
        WHERE (:query = '' OR ${BankTable.Columns.NAME} LIKE '%' || :query || '%')
        ORDER BY ${BankTable.Columns.CREATED_AT} DESC
    """
    )
    fun banksPagingSource(query: String): PagingSource<Int, BankEntity>

    @Query(
        value = """
        SELECT * FROM ${BankTable.NAME}
        WHERE ${BankTable.Columns.ID} = :id
    """
    )
    suspend fun getBank(id: Long): BankEntity?

    @Query(
        value = """
        SELECT * FROM ${BankTable.NAME}
        WHERE ${BankTable.Columns.ID} = :id
    """
    )
    fun observeBank(id: Long): Flow<BankEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bankDetail: BankEntity): Long

    @Upsert
    suspend fun upsertAll(banks: List<BankEntity>)

    @Query("DELETE FROM ${BankTable.NAME} WHERE ${BankTable.Columns.ID} = :bankId")
    suspend fun delete(bankId: Long): Int

    @Query(value = "DELETE FROM ${BankTable.NAME}")
    suspend fun deleteAll()
}