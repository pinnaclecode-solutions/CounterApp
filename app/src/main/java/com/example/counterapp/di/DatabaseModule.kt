package com.example.counterapp.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.counterapp.data.local.CategoryDao
import com.example.counterapp.data.local.CounterAppDatabase
import com.example.counterapp.data.local.CounterDao
import com.example.counterapp.data.local.CounterSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CounterAppDatabase {
        return Room.databaseBuilder(
            context,
            CounterAppDatabase::class.java,
            "counter_app_db"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    val now = System.currentTimeMillis()
                    db.execSQL(
                        "INSERT INTO categories (name, created_at, sort_order) VALUES ('General', $now, 0)"
                    )
                    db.execSQL(
                        "INSERT INTO counters (category_id, name, count, total_time_ms, is_active, created_at) VALUES (1, 'My Counter', 0, 0, 0, $now)"
                    )
                }
            })
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    fun provideCategoryDao(db: CounterAppDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideCounterDao(db: CounterAppDatabase): CounterDao = db.counterDao()

    @Provides
    fun provideCounterSessionDao(db: CounterAppDatabase): CounterSessionDao = db.counterSessionDao()
}
