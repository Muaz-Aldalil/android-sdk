package com.subulalhuda.data.model

import kotlinx.serialization.Serializable

/**
 * A lecture category filter.
 * Derived from the website's src/constants/CATEGORIES.js — extracted as-is.
 *
 * Note: Includes a synthetic "all" entry ({ id: "all", label: "الكل" }) which is a UI concern.
 * The Android app may choose to exclude this from the data and handle it in the UI layer.
 */
@Serializable
data class Category(
    val id: String,
    val label: String,
)
