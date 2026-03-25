package com.example.counterapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Category::class, Counter::class, CounterSessionEntry::class],
    version = 3,
    exportSchema = false
)
abstract class CounterAppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun counterDao(): CounterDao
    abstract fun counterSessionDao(): CounterSessionDao
}
