package com.handbook.app.feature.home.data.source.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.handbook.app.feature.home.data.source.local.entity.CategoryEntity
import com.handbook.app.feature.home.data.source.local.entity.CategoryTable
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

    @Insert
    fun insert(category: CategoryEntity): Long

    @Upsert
    fun upsertAll(categories: List<CategoryEntity>)

    @Delete
    fun delete(category: CategoryEntity)

    @Query(value = "DELETE FROM ${CategoryTable.NAME}")
    fun deleteAll()
}