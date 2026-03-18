package com.example.counterapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CounterDao {

    @Query("SELECT * FROM counters WHERE category_id = :categoryId ORDER BY created_at ASC")
    fun getCountersByCategory(categoryId: Long): Flow<List<Counter>>

    @Query("SELECT * FROM counters WHERE id = :id")
    fun getCounterById(id: Long): Flow<Counter?>

    @Query("SELECT * FROM counters WHERE id = :id")
    suspend fun getCounterByIdOnce(id: Long): Counter?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(counter: Counter): Long

    @Update
    suspend fun update(counter: Counter)

    @Delete
    suspend fun delete(counter: Counter)

    @Query("""
        SELECT COALESCE(SUM(count), 0) AS totalCount,
               COALESCE(SUM(total_time_ms), 0) AS totalTimeMs,
               COUNT(id) AS counterCount,
               (SELECT name FROM counters WHERE category_id = :categoryId ORDER BY count DESC LIMIT 1) AS mostActiveCounterName,
               (SELECT count FROM counters WHERE category_id = :categoryId ORDER BY count DESC LIMIT 1) AS mostActiveCount
        FROM counters
        WHERE category_id = :categoryId
    """)
    fun getCategoryStats(categoryId: Long): Flow<CategoryStats>
}
