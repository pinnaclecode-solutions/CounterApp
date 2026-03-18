package com.example.counterapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("""
        SELECT c.id, c.name, c.created_at AS createdAt, c.sort_order AS sortOrder,
               COALESCE(SUM(ct.count), 0) AS totalCount,
               COALESCE(SUM(ct.total_time_ms), 0) AS totalTimeMs,
               COUNT(ct.id) AS counterCount
        FROM categories c
        LEFT JOIN counters ct ON ct.category_id = c.id
        GROUP BY c.id
        ORDER BY c.sort_order ASC, c.created_at ASC
    """)
    fun getCategoriesWithStats(): Flow<List<CategoryWithStats>>

    @Query("SELECT * FROM categories ORDER BY sort_order ASC, created_at ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun getCategoryById(id: Long): Flow<Category?>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)
}
