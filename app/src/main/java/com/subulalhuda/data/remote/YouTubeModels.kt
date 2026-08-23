package com.subulalhuda.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * YouTube Data API v3 response models.
 *
 * These are minimal models for the specific API calls the app makes:
 * 1. playlistItems.list — fetch recent uploads
 * 2. videos.list — fetch video metadata + live streaming details
 *
 * Quota cost:
 * - playlistItems.list: 1 unit per request
 * - videos.list: 1 unit per request
 * - search.list: 100 units per request (AVOID — used only as fallback)
 */

@Serializable
data class PlaylistItemsResponse(
    val kind: String? = null,
    val items: List<PlaylistItem> = emptyList(),
    val pageInfo: PageInfo? = null,
)

@Serializable
data class PlaylistItem(
    val kind: String? = null,
    val id: String? = null,
    val snippet: Snippet? = null,
    val contentDetails: PlaylistItemContentDetails? = null,
)

@Serializable
data class PlaylistItemContentDetails(
    val videoId: String? = null,
    val videoPublishedAt: String? = null,
)

@Serializable
data class VideosResponse(
    val kind: String? = null,
    val items: List<VideoItem> = emptyList(),
    val pageInfo: PageInfo? = null,
)

@Serializable
data class VideoItem(
    val kind: String? = null,
    val id: String? = null,
    val snippet: Snippet? = null,
    val contentDetails: VideoContentDetails? = null,
    val liveStreamingDetails: LiveStreamingDetails? = null,
)

@Serializable
data class Snippet(
    val publishedAt: String? = null,
    val channelId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val thumbnails: Thumbnails? = null,
    val channelTitle: String? = null,
    val tags: List<String>? = null,
    val categoryId: String? = null,
)

@Serializable
data class Thumbnails(
    val default: Thumbnail? = null,
    val medium: Thumbnail? = null,
    val high: Thumbnail? = null,
    val standard: Thumbnail? = null,
    val maxres: Thumbnail? = null,
)

@Serializable
data class Thumbnail(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null,
)

@Serializable
data class VideoContentDetails(
    val duration: String? = null,
    val dimension: String? = null,
    val definition: String? = null,
    val caption: String? = null,
    val licensedContent: Boolean? = null,
    @SerialName("contentRating") val contentRating: ContentRating? = null,
)

@Serializable
data class ContentRating()

/**
 * Live streaming details — only present when video is/was live.
 *
 * IMPORTANT LIMITATIONS:
 * - actualStartTime/actualEndTime may be null if the stream hasn't started/ended
 * - concurrentViewers is only available during an active live stream
 * - This data is fetched via videos.list(part=liveStreamingDetails) which costs 1 unit
 *
 * QUOTA NOTE: Live status is best-effort. The latest 5 uploads may not always contain
 * an active livestream. We only refresh when the user is actively viewing relevant UI.
 * No background polling. If detection proves unreliable, we stop before introducing
 * a more expensive fallback (search.list at 100 units).
 */
@Serializable
data class LiveStreamingDetails(
    val actualStartTime: String? = null,
    val actualEndTime: String? = null,
    val scheduledStartTime: String? = null,
    val scheduledEndTime: String? = null,
    val concurrentViewers: Long? = null,
    val activeLiveChatId: String? = null,
)

@Serializable
data class PageInfo(
    val totalResults: Int? = null,
    val resultsPerPage: Int? = null,
)
