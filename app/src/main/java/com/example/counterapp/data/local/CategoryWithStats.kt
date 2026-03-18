package com.example.counterapp.data.local

data class CategoryWithStats(
    val id: Long,
    val name: String,
    val totalCount: Int,
    val totalTimeMs: Long,
    val counterCount: Int,
    val createdAt: Long,
    val sortOrder: Int
)
