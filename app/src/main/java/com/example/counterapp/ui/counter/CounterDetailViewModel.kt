package com.example.counterapp.ui.counter

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.counterapp.data.local.Counter
import com.example.counterapp.data.repository.CounterAppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CounterDetailViewModel @Inject constructor(
    private val repository: CounterAppRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val counterId: Long = savedStateHandle["counterId"]!!

    val counter: StateFlow<Counter?> = repository.getCounterById(counterId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var sessionStartMs: Long? = null

    private val _liveElapsedMs = MutableStateFlow(0L)
    val liveElapsedMs: StateFlow<Long> = _liveElapsedMs.asStateFlow()

    private val _isEditDialogOpen = MutableStateFlow(false)
    val isEditDialogOpen: StateFlow<Boolean> = _isEditDialogOpen.asStateFlow()

    // Trigger for scale animation — increments on each tap
    private val _incrementTrigger = MutableStateFlow(0)
    val incrementTrigger: StateFlow<Int> = _incrementTrigger.asStateFlow()

    init {
        startTimerTicker()
    }

    private fun startTimerTicker() {
        viewModelScope.launch {
            while (true) {
                val start = sessionStartMs
                val baseTime = counter.value?.totalTimeMs ?: 0
                _liveElapsedMs.value = if (start != null) {
                    baseTime + (SystemClock.elapsedRealtime() - start)
                } else {
                    baseTime
                }
                delay(1000)
            }
        }
    }

    fun startSession() {
        viewModelScope.launch {
            repository.startSession(counterId)
            sessionStartMs = SystemClock.elapsedRealtime()
        }
    }

    fun flushSession() {
        val start = sessionStartMs ?: return
        sessionStartMs = null
        viewModelScope.launch {
            repository.flushSession(counterId)
        }
    }

    fun increment(amount: Int) {
        viewModelScope.launch {
            repository.incrementCounter(counterId, amount)
            _incrementTrigger.value++
        }
    }

    fun openEditDialog() {
        flushSession()
        _isEditDialogOpen.value = true
    }

    fun closeEditDialog() {
        _isEditDialogOpen.value = false
        startSession()
    }

    fun saveEdit(newCount: Int, newTimeMs: Long) {
        viewModelScope.launch {
            repository.updateCounterManually(counterId, newCount, newTimeMs)
            _isEditDialogOpen.value = false
            startSession()
        }
    }

    fun renameCounter(newName: String) {
        viewModelScope.launch {
            repository.renameCounter(counterId, newName)
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (sessionStartMs != null) {
            // Best-effort flush — coroutine may not complete if process is dying,
            // but lifecycle flush in the UI should have already handled it
            sessionStartMs = null
        }
    }
}
