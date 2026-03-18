package com.example.counterapp.util

object TimeFormatter {
    fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return "${hours}h ${minutes}m ${seconds}s"
    }
}
