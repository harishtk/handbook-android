package com.handbook.app.feature.home.data.source.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.handbook.app.feature.home.data.source.local.entity.CategoryEntity
import com.handbook.app.feature.home.data.source.local.entity.CategoryTable
import com.handbook.app.feature.home.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query(
        value = """
        SELECT * FROM ${CategoryTable.NAME}
        ORDER BY ${CategoryTable.Columns.CREATED_AT} DESC
    """
    )
    fun categoriesStream(): Flow<List<CategoryEntity>>

    @Query(
        value = """
        SELECT * FROM ${CategoryTable.NAME}
        WHERE (:query = '' OR ${CategoryTable.Columns.NAME} LIKE '%' || :query || '%')
        AND (:transactionType IS NULL OR ${CategoryTable.Columns.TRANSACTION_TYPE} = :transactionType)
        ORDER BY ${CategoryTable.Columns.CREATED_AT} DESC
    """
    )
    fun categoriesPagingSource(query: String, transactionType: TransactionType?): PagingSource<Int, CategoryEntity>

    @Query(
        value = """
        SELECT * FROM ${CategoryTable.NAME}
        WHERE ${CategoryTable.Columns.ID} = :id
    """
    )
    suspend fun getCategory(id: Long): CategoryEntity?

    @Query(
        value = """
        SELECT * FROM ${CategoryTable.NAME}
        WHERE ${CategoryTable.Columns.ID} = :id
    """
    )
    fun observeCategory(id: Long): Flow<CategoryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM ${CategoryTable.NAME} WHERE ${CategoryTable.Columns.ID} = :categoryId")
    suspend fun delete(categoryId: Long): Int

    @Query(value = "DELETE FROM ${CategoryTable.NAME}")
    suspend fun deleteAll()
}