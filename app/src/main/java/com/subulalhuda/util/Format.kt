package com.subulalhuda.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Arabic plural label for a count of videos.
 *
 * Arabic has distinct forms for 1, 2, 3–10, and 11+:
 * 1 → "فيديو واحد", 2 → "فيديوان", 3–10 → "X فيديوهات", 11+ → "X فيديو".
 */
fun videoCountLabel(count: Int): String = when (count) {
    1 -> "فيديو واحد"
    2 -> "فيديوان"
    in 3..10 -> "$count فيديوهات"
    else -> "$count فيديو"
}

private val arabicDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ar"))

/**
 * Format an ISO-8601 date string as an Arabic date ("17 سبتمبر 2026").
 * Falls back to the raw "yyyy-MM-dd" prefix when parsing fails.
 */
fun formatArabicDate(isoDate: String): String = try {
    Instant.parse(isoDate)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(arabicDateFormatter)
} catch (_: Exception) {
    isoDate.take(10) // fallback: raw "2024-01-15"
}