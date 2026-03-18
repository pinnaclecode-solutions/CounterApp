package com.example.counterapp.data.local

data class CategoryStats(
    val totalCount: Int = 0,
    val totalTimeMs: Long = 0,
    val counterCount: Int = 0,
    val mostActiveCounterName: String? = null,
    val mostActiveCount: Int = 0
)
