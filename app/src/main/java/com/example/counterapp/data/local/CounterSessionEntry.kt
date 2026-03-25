package com.example.counterapp.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "counter_sessions",
    foreignKeys = [ForeignKey(
        entity = Counter::class,
        parentColumns = ["id"],
        childColumns = ["counter_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("counter_id")]
)
data class CounterSessionEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "counter_id") val counterId: Long,
    @ColumnInfo(name = "count_before") val countBefore: Int,
    @ColumnInfo(name = "count_after") val countAfter: Int,
    @ColumnInfo(name = "duration_ms") val durationMs: Long,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "ended_at") val endedAt: Long,
    @ColumnInfo(name = "is_manual_edit", defaultValue = "0") val isManualEdit: Boolean = false
)
