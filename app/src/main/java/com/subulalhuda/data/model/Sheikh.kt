package com.subulalhuda.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A sheikh (Islamic scholar) profile.
 * Derived from the website's src/constants/SHEIKHS.js — extracted as-is.
 */
@Serializable
data class Sheikh(
    val id: String,
    val name: String,
    val bio: String,
    val avatar: String,
    val lectureCount: Int,
    val videoIds: List<String>,
)
