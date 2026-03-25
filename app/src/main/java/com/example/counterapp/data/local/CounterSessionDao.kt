package com.example.counterapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CounterSessionDao {
    @Query("SELECT * FROM counter_sessions WHERE counter_id = :counterId ORDER BY ended_at DESC")
    fun getSessionsForCounter(counterId: Long): Flow<List<CounterSessionEntry>>

    @Insert
    suspend fun insert(entry: CounterSessionEntry)
}
