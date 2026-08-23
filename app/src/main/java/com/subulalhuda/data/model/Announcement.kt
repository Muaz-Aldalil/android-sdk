package com.subulalhuda.data.model

import kotlinx.serialization.Serializable

/**
 * An announcement item.
 * Derived from the website's src/constants/ANNOUNCEMENTS.js — extracted as-is.
 *
 * Nullable fields preserved exactly as in the website source.
 * Links can be absolute URLs (YouTube) or internal SPA routes (/sheikhs/..., /lectures, etc.).
 * Android maps SPA routes to screens; absolute URLs open externally.
 */
@Serializable
data class Announcement(
    val id: Int,
    val type: String, // "live", "new", "event", "upcoming"
    val title: String,
    val description: String,
    val date: String? = null, // nullable — ISO date string or null
    val sheikhId: String? = null, // nullable — foreign key into Sheikh.id
    val youtubeVideoId: String? = null, // nullable
    val featured: Boolean = false,
    val link: String? = null, // nullable — absolute URL or SPA route
    val linkLabel: String? = null, // nullable
)
