package com.example.counterapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Category::class, Counter::class],
    version = 1,
    exportSchema = false
)
abstract class CounterAppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun counterDao(): CounterDao
}
