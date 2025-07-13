package com.handbook.app.feature.home.data.source.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.handbook.app.feature.home.data.source.local.entity.AccountEntryEntity
import com.handbook.app.feature.home.data.source.local.model.AccountEntryWithDetailsEntity
import com.handbook.app.feature.home.data.source.local.model.SearchedAccountEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountEntryDao {
    @Transaction // Important for queries with @Relation
    @Query("SELECT * FROM account_entries WHERE entry_id = :entryId")
    suspend fun getAccountEntryWithDetails(entryId: Long): AccountEntryWithDetailsEntity?

    @Transaction // Important for queries with @Relation
    @Query("SELECT * FROM account_entries WHERE entry_id = :entryId")
    fun observeAccountEntryWithDetails(entryId: Long): Flow<AccountEntryWithDetailsEntity?>

    @Transaction
    @Query("SELECT * FROM account_entries ORDER BY transaction_date DESC")
    fun getAllAccountEntriesWithDetails(): Flow<List<AccountEntryWithDetailsEntity>>

    @Upsert
    suspend fun upsertAccountEntry(entry: AccountEntryEntity)

    @Query("DELETE FROM account_entries WHERE entry_id = :entryId")
    suspend fun delete(entryId: Long)

    @Query("DELETE FROM account_entries")
    suspend fun deleteAllAccountEntries()

    @Transaction
    @Query("""
    SELECT * FROM account_entries
    WHERE (:categoryId IS NULL OR fk_category_id = :categoryId)
    AND (:partyId IS NULL OR fk_party_id = :partyId)
    AND (:entryType IS NULL OR entry_type = :entryType)
    AND (:transactionType IS NULL OR transaction_type = :transactionType)
    AND (:startDate IS NULL OR transaction_date >= :startDate)
    AND (:endDate IS NULL OR transaction_date <= :endDate)
    AND (:titleQuery IS NULL OR title LIKE '%' || :titleQuery || '%')
    ORDER BY transaction_date DESC
""")
    fun getFilteredEntries(
        categoryId: Long? = null,
        partyId: Long? = null,
        entryType: String? = null, // Pass enum.name or use TypeConverter
        transactionType: String? = null, // Pass enum.name or use TypeConverter
        startDate: Long? = null,
        endDate: Long? = null,
        titleQuery: String? = null
    ): Flow<List<AccountEntryWithDetailsEntity>>

    @Transaction
    @Query("""
    SELECT * FROM account_entries
    WHERE (:categoryId IS NULL OR fk_category_id = :categoryId)
    AND (:partyId IS NULL OR fk_party_id = :partyId)
    AND (:entryType IS NULL OR entry_type = :entryType)
    AND (:transactionType IS NULL OR transaction_type = :transactionType)
    AND (:startDate IS NULL OR transaction_date >= :startDate)
    AND (:endDate IS NULL OR transaction_date <= :endDate)
    AND (:titleQuery IS NULL OR title LIKE '%' || :titleQuery || '%')
    ORDER BY transaction_date DESC
""")
    fun getFilteredEntriesPagingSource(
        categoryId: Long? = null,
        partyId: Long? = null,
        entryType: String? = null, // Pass enum.name or use TypeConverter
        transactionType: String? = null, // Pass enum.name or use TypeConverter
        startDate: Long? = null,
        endDate: Long? = null,
        titleQuery: String? = null
    ): PagingSource<Int, AccountEntryWithDetailsEntity>

    @Transaction
    @Query("SELECT * FROM account_entries WHERE entry_id IN (:entryIds)")
    fun getAccountEntriesWithDetailsByIds(entryIds: List<Long>): Flow<List<AccountEntryWithDetailsEntity>>

    @Query("""
        SELECT ae.*, c.name AS category_name
        FROM account_entries_fts AS fts_table
        JOIN account_entries AS ae ON fts_table.rowid = ae.entry_id
        JOIN categories AS c ON ae.fk_category_id = c.category_id
        WHERE fts_table.account_entries_fts MATCH :query
    """)
    // ORDER BY fts_table.rank -- If you selected rank and want to order by it
    fun searchEntriesWithJoin(query: String): Flow<List<SearchedAccountEntry>>

}