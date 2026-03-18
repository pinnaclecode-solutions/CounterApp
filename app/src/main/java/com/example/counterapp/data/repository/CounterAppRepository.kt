package com.example.counterapp.data.repository

import android.os.SystemClock
import com.example.counterapp.data.local.Category
import com.example.counterapp.data.local.CategoryDao
import com.example.counterapp.data.local.CategoryStats
import com.example.counterapp.data.local.CategoryWithStats
import com.example.counterapp.data.local.Counter
import com.example.counterapp.data.local.CounterDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CounterAppRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val counterDao: CounterDao
) {
    fun getCategoriesWithStats(): Flow<List<CategoryWithStats>> =
        categoryDao.getCategoriesWithStats()

    fun getAllCategories(): Flow<List<Category>> =
        categoryDao.getAllCategories()

    fun getCategoryById(id: Long): Flow<Category?> =
        categoryDao.getCategoryById(id)

    fun getCountersByCategory(categoryId: Long): Flow<List<Counter>> =
        counterDao.getCountersByCategory(categoryId)

    fun getCounterById(id: Long): Flow<Counter?> =
        counterDao.getCounterById(id)

    fun getCategoryStats(categoryId: Long): Flow<CategoryStats> =
        counterDao.getCategoryStats(categoryId)

    suspend fun addCategory(name: String): Long {
        val category = Category(name = name.trim())
        return categoryDao.insert(category)
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.update(category)
    }

    suspend fun renameCategory(categoryId: Long, newName: String, currentCategory: Category) {
        categoryDao.update(currentCategory.copy(name = newName.trim()))
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.delete(category)
        if (categoryDao.getCategoryCount() == 0) {
            recreateDefault()
        }
    }

    suspend fun addCounter(categoryId: Long, name: String): Long {
        val counter = Counter(categoryId = categoryId, name = name.trim())
        return counterDao.insert(counter)
    }

    suspend fun deleteCounter(counter: Counter) {
        counterDao.delete(counter)
    }

    suspend fun incrementCounter(counterId: Long, amount: Int) {
        val counter = counterDao.getCounterByIdOnce(counterId) ?: return
        counterDao.update(counter.copy(count = (counter.count + amount).coerceAtLeast(0)))
    }

    suspend fun startSession(counterId: Long) {
        val counter = counterDao.getCounterByIdOnce(counterId) ?: return
        counterDao.update(
            counter.copy(
                isActive = true,
                activeSessionStartMs = SystemClock.elapsedRealtime(),
                lastOpenedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun flushSession(counterId: Long) {
        val counter = counterDao.getCounterByIdOnce(counterId) ?: return
        if (!counter.isActive || counter.activeSessionStartMs == null) return
        val elapsed = SystemClock.elapsedRealtime() - counter.activeSessionStartMs
        counterDao.update(
            counter.copy(
                totalTimeMs = counter.totalTimeMs + elapsed.coerceAtLeast(0),
                isActive = false,
                activeSessionStartMs = null
            )
        )
    }

    suspend fun updateCounterManually(counterId: Long, newCount: Int, newTimeMs: Long) {
        val counter = counterDao.getCounterByIdOnce(counterId) ?: return
        counterDao.update(
            counter.copy(
                count = newCount.coerceAtLeast(0),
                totalTimeMs = newTimeMs.coerceAtLeast(0)
            )
        )
    }

    suspend fun renameCounter(counterId: Long, newName: String) {
        val counter = counterDao.getCounterByIdOnce(counterId) ?: return
        counterDao.update(counter.copy(name = newName.trim()))
    }

    private suspend fun recreateDefault() {
        val categoryId = categoryDao.insert(Category(name = "General"))
        counterDao.insert(Counter(categoryId = categoryId, name = "My Counter"))
    }
}
