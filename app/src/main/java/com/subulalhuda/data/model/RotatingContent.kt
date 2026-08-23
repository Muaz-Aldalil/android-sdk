package com.subulalhuda.data.model

import kotlinx.serialization.Serializable

/**
 * A rotating Quran verse or hadith for display in the hero section.
 * Derived from the website's src/constants/ROTATING_CONTENT.js — extracted as-is.
 */
@Serializable
data class RotatingContent(
    val id: Int,
    val text: String, // fully vocalized Arabic with diacritics
    val source: String,
    val type: String, // "verse" or "hadith"
)
