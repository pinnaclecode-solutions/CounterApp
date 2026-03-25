package com.example.counterapp.ui.counter

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.counterapp.data.local.Counter
import com.example.counterapp.data.local.CounterSessionEntry
import com.example.counterapp.data.repository.CounterAppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CounterDetailViewModel @Inject constructor(
    private val repository: CounterAppRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val counterId: Long = savedStateHandle["counterId"]!!

    val counter: StateFlow<Counter?> = repository.getCounterById(counterId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val sessions: StateFlow<List<CounterSessionEntry>> = repository.getCounterSessions(counterId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Visit-level tracking (one history entry per screen visit) ---
    private var visitStartCount: Int? = null          // count when screen was opened
    private var visitStartWallTime: Long = 0L         // wall clock when screen was opened
    private var visitAccumulatedDurationMs: Long = 0L  // total timer-on duration across pause/resume cycles
    private var visitHasActivity: Boolean = false       // any increment, edit, or timer run happened

    // --- Current timer segment tracking ---
    private var segmentStartElapsed: Long? = null       // SystemClock.elapsedRealtime() when current segment started
    private var segmentStartWallTime: Long = 0L         // wall clock when current timer segment started

    private val _liveElapsedMs = MutableStateFlow(0L)
    val liveElapsedMs: StateFlow<Long> = _liveElapsedMs.asStateFlow()

    private val _isPaused = MutableStateFlow(true)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _isEditDialogOpen = MutableStateFlow(false)
    val isEditDialogOpen: StateFlow<Boolean> = _isEditDialogOpen.asStateFlow()

    private val _incrementTrigger = MutableStateFlow(0)
    val incrementTrigger: StateFlow<Int> = _incrementTrigger.asStateFlow()

    init {
        startTimerTicker()
        initVisitTracking()
    }

    private fun initVisitTracking() {
        viewModelScope.launch {
            // Wait for the first counter value to arrive from Room
            val c = repository.getCounterById(counterId).first()
            if (c != null && visitStartCount == null) {
                visitStartCount = c.count
                visitStartWallTime = System.currentTimeMillis()
            }
        }
    }

    private fun startTimerTicker() {
        viewModelScope.launch {
            while (true) {
                val start = segmentStartElapsed
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

    /**
     * Starts the timer: begins a new timer segment and marks the counter active in DB.
     */
    private fun startTimerSegment() {
        segmentStartWallTime = System.currentTimeMillis()
        viewModelScope.launch {
            repository.startSession(counterId)
            segmentStartElapsed = SystemClock.elapsedRealtime()
        }
    }

    /**
     * Stops the current timer segment: flushes elapsed time to DB and accumulates
     * the segment duration into the visit total. Does NOT log history.
     */
    private fun stopTimerSegment() {
        val start = segmentStartElapsed ?: return
        segmentStartElapsed = null
        val segmentDuration = if (segmentStartWallTime > 0) {
            System.currentTimeMillis() - segmentStartWallTime
        } else {
            0L
        }
        visitAccumulatedDurationMs += segmentDuration
        visitHasActivity = true
        segmentStartWallTime = 0L
        viewModelScope.launch {
            repository.flushSession(counterId)
        }
    }

    fun pauseTimer() {
        stopTimerSegment()
        _isPaused.value = true
    }

    fun resumeTimer() {
        _isPaused.value = false
        visitHasActivity = true
        startTimerSegment()
    }

    fun increment(amount: Int) {
        visitHasActivity = true
        viewModelScope.launch {
            repository.incrementCounter(counterId, amount)
            _incrementTrigger.value++
        }
    }

    fun openEditDialog() {
        // Stop timer segment while editing (accumulates duration) but don't pause
        stopTimerSegment()
        _isEditDialogOpen.value = true
    }

    fun closeEditDialog() {
        _isEditDialogOpen.value = false
        // Resume timer segment if not paused
        if (!_isPaused.value) {
            startTimerSegment()
        }
    }

    fun saveEdit(newCount: Int, newTimeMs: Long) {
        visitHasActivity = true
        viewModelScope.launch {
            repository.updateCounterManually(counterId, newCount, newTimeMs)
            _isEditDialogOpen.value = false
            // Resume timer segment if not paused
            if (!_isPaused.value) {
                startTimerSegment()
            }
        }
    }

    /**
     * Called when the user leaves the screen (ON_STOP or onCleared).
     * Logs a single history entry for the entire visit if anything happened.
     */
    fun onScreenExit() {
        // Stop any running timer segment first
        stopTimerSegment()
        _isPaused.value = true

        val startCount = visitStartCount ?: return
        val currentCount = counter.value?.count ?: startCount
        val now = System.currentTimeMillis()

        // Only log if something changed during this visit
        val countChanged = currentCount != startCount
        if (!countChanged && !visitHasActivity) return

        val entry = CounterSessionEntry(
            counterId = counterId,
            countBefore = startCount,
            countAfter = currentCount,
            durationMs = visitAccumulatedDurationMs,
            startedAt = visitStartWallTime,
            endedAt = now
        )

        // Mark visit as logged to prevent duplicate logging
        visitStartCount = null

        viewModelScope.launch {
            repository.logSession(
                counterId = entry.counterId,
                countBefore = entry.countBefore,
                countAfter = entry.countAfter,
                durationMs = entry.durationMs,
                startedAt = entry.startedAt,
                endedAt = entry.endedAt
            )
        }
    }

    fun deleteCounter(onDeleted: () -> Unit) {
        // Clear visit so onScreenExit doesn't log for a deleted counter
        visitStartCount = null
        viewModelScope.launch {
            counter.value?.let { repository.deleteCounter(it) }
            onDeleted()
        }
    }

    fun renameCounter(newName: String) {
        viewModelScope.launch {
            repository.renameCounter(counterId, newName)
        }
    }

    override fun onCleared() {
        super.onCleared()
        val startCount = visitStartCount ?: return
        val currentCount = counter.value?.count ?: startCount

        // Stop any running timer segment
        val segStart = segmentStartElapsed
        if (segStart != null) {
            segmentStartElapsed = null
            val segmentDuration = if (segmentStartWallTime > 0) {
                System.currentTimeMillis() - segmentStartWallTime
            } else {
                0L
            }
            visitAccumulatedDurationMs += segmentDuration
            visitHasActivity = true
        }

        val now = System.currentTimeMillis()
        val countChanged = currentCount != startCount
        if (!countChanged && !visitHasActivity) return

        visitStartCount = null

        // Best-effort flush + log using NonCancellable since viewModelScope is cancelled
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob()).launch {
            withContext(NonCancellable) {
                if (segStart != null) {
                    repository.flushSession(counterId)
                }
                repository.logSession(
                    counterId = counterId,
                    countBefore = startCount,
                    countAfter = currentCount,
                    durationMs = visitAccumulatedDurationMs,
                    startedAt = visitStartWallTime,
                    endedAt = now
                )
            }
        }
    }
}
