package com.swiftflow.utils

import java.text.SimpleDateFormat
import java.util.Locale

object DateFormatter {
    /**
     * Format ISO date string to "1 Jan 2026" format
     * @param isoDate ISO 8601 date string (e.g., "2026-01-05T01:38:46.797823Z")
     * @return Formatted date string (e.g., "5 Jan 2026")
     */
    fun formatToDisplay(isoDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(isoDate.substring(0, 19))
            date?.let { outputFormat.format(it) } ?: isoDate.take(10)
        } catch (e: Exception) {
            // Fallback to simple substring if parsing fails
            isoDate.take(10)
        }
    }

    fun formatToTime(isoDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = inputFormat.parse(isoDate.substring(0, 19))
            date?.let { outputFormat.format(it) } ?: isoDate.take(5)
        } catch (e: Exception) {
            isoDate.take(5)
        }
    }
}
