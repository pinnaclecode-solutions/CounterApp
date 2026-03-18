package com.example.counterapp.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "counters",
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = ["id"],
        childColumns = ["category_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("category_id")]
)
data class Counter(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "category_id") val categoryId: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "count") val count: Int = 0,
    @ColumnInfo(name = "total_time_ms") val totalTimeMs: Long = 0,
    @ColumnInfo(name = "last_opened_at") val lastOpenedAt: Long? = null,
    @ColumnInfo(name = "is_active") val isActive: Boolean = false,
    @ColumnInfo(name = "active_session_start_ms") val activeSessionStartMs: Long? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
