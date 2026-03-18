package com.example.counterapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.counterapp.data.local.Category
import com.example.counterapp.data.local.CategoryWithStats
import com.example.counterapp.data.repository.CounterAppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CounterAppRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryWithStats>> = repository.getCategoriesWithStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(name: String) {
        viewModelScope.launch {
            try {
                repository.addCategory(name)
            } catch (_: Exception) {
                // Duplicate name - silently ignore, UI should validate
            }
        }
    }

    fun deleteCategory(categoryWithStats: CategoryWithStats) {
        viewModelScope.launch {
            val category = Category(
                id = categoryWithStats.id,
                name = categoryWithStats.name,
                createdAt = categoryWithStats.createdAt,
                sortOrder = categoryWithStats.sortOrder
            )
            repository.deleteCategory(category)
        }
    }

    fun renameCategory(categoryWithStats: CategoryWithStats, newName: String) {
        viewModelScope.launch {
            val category = Category(
                id = categoryWithStats.id,
                name = categoryWithStats.name,
                createdAt = categoryWithStats.createdAt,
                sortOrder = categoryWithStats.sortOrder
            )
            try {
                repository.renameCategory(category.id, newName, category)
            } catch (_: Exception) {
                // Duplicate name
            }
        }
    }
}
