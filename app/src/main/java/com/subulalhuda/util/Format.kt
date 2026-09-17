package com.subulalhuda.util

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