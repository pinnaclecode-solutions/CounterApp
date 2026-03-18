package com.example.counterapp.ui.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.counterapp.data.local.Category
import com.example.counterapp.data.local.CategoryStats
import com.example.counterapp.data.local.Counter
import com.example.counterapp.data.repository.CounterAppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    private val repository: CounterAppRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: Long = savedStateHandle["categoryId"]!!

    val category: StateFlow<Category?> = repository.getCategoryById(categoryId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val counters: StateFlow<List<Counter>> = repository.getCountersByCategory(categoryId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<CategoryStats> = repository.getCategoryStats(categoryId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoryStats())

    fun updateCategoryName(newName: String) {
        viewModelScope.launch {
            val cat = category.value ?: return@launch
            try {
                repository.renameCategory(cat.id, newName, cat)
            } catch (_: Exception) { }
        }
    }

    fun addCounter(name: String) {
        viewModelScope.launch {
            repository.addCounter(categoryId, name)
        }
    }

    fun deleteCounter(counter: Counter) {
        viewModelScope.launch {
            repository.deleteCounter(counter)
        }
    }
}
