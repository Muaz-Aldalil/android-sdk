package com.subulalhuda.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.subulalhuda.data.remote.YouTubeApiClient
import com.subulalhuda.data.remote.PlaylistItem
import com.subulalhuda.data.remote.VideoItem
import com.subulalhuda.util.CacheManager

/**
 * Repository for YouTube Data API v3 calls.
 *
 * Architecture:
 * - Uses videos.list for live status detection (1 unit per call)
 * - Uses playlistItems.list for recent uploads (1 unit per call)
 * - NEVER uses search.list (100 units per call) unless explicitly needed
 *
 * Live detection strategy:
 *   Recent uploads → videos.list(liveStreamingDetails) → Live found? → Show/Not detected
 *
 * IMPORTANT LIMITATIONS:
 * - The latest N uploads may not contain an active livestream
 * - No background polling — refresh only when user is actively viewing
 * - If livestreams regularly fall outside the recent window, stop and report
 *   before introducing a more expensive fallback
 *
 * Quota note: Actual quota behavior must be verified in the Google Cloud project.
 * The quota numbers documented here are based on historical YouTube API documentation
 * and may not reflect 2026 quota system changes.
 *
 * @param context Application context
 * @param apiKey Separate YouTube API key (NOT the website's key)
 * @param channelId YouTube channel ID
 */
class YouTubeRepository(
    private val context: Context,
    private val apiKey: String,
    private val channelId: String,
) {
    private val client = CacheManager.createCachedClient(context)
    private val apiClient = YouTubeApiClient(apiKey, channelId, client)

    // Cached uploads playlist ID (7-day TTL in HTTP cache)
    @Volatile
    private var uploadsPlaylistId: String? = null

    /**
     * Get the uploads playlist ID, fetching it if not cached.
     *
     * Quota cost: 1 unit (if cache miss)
     */
    fun getUploadsPlaylistId(): String? {
        uploadsPlaylistId?.let { return it }
        val id = apiClient.getUploadsPlaylistId()
        uploadsPlaylistId = id
        return id
    }

    /**
     * Fetch recent uploads from the channel.
     *
     * @param maxResults Number of recent videos to fetch
     * @return List of playlist items (video IDs + snippets)
     *
     * Quota cost: 1 unit (if cache miss)
     */
    fun getRecentUploads(maxResults: Int = 5): List<PlaylistItem> {
        val playlistId = getUploadsPlaylistId() ?: return emptyList()
        return apiClient.getRecentUploads(playlistId, maxResults)
    }

    /**
     * Get video details including live streaming status.
     *
     * This is the preferred live detection method.
     * Cost: 1 unit for up to 50 video IDs.
     *
     * IMPORTANT: Only videos in the recent uploads are checked.
     * A livestream that's no longer in the recent window won't be detected.
     */
    fun getVideoDetails(videoIds: List<String>): List<VideoItem> {
        return apiClient.getVideoDetails(videoIds)
    }

    /**
     * Check if any of the recent uploads are currently live.
     *
     * Strategy: Fetch recent uploads → get video details → check liveStreamingDetails.
     *
     * LIMITATION: Only detects live streams in the most recent uploads.
     * If the channel's livestreams are older than the recent window,
     * they won't be detected. We accept this to keep quota usage at ~3 units/day.
     *
     * No background polling. This method is called only when the user is actively
     * viewing the home screen or a relevant section.
     */
    fun checkLiveStatus(maxVideosToCheck: Int = 5): LiveCheckResult {
        return try {
            val recentUploads = getRecentUploads(maxVideosToCheck)
            if (recentUploads.isEmpty()) {
                return LiveCheckResult.NoVideos
            }

            val videoIds = recentUploads.mapNotNull { it.contentDetails?.videoId }
            if (videoIds.isEmpty()) {
                return LiveCheckResult.NoVideos
            }

            val videoDetails = getVideoDetails(videoIds)

            // Find any video that has live streaming details with an actual start time
            // but no end time (meaning it's currently live)
            val liveVideo = videoDetails.find { video ->
                val liveDetails = video.liveStreamingDetails
                liveDetails != null &&
                    liveDetails.actualStartTime != null &&
                    liveDetails.actualEndTime == null
            }

            if (liveVideo != null) {
                LiveCheckResult.Live(
                    videoId = liveVideo.id ?: "",
                    title = liveVideo.snippet?.title ?: "",
                    viewers = liveVideo.liveStreamingDetails?.concurrentViewers,
                )
            } else {
                LiveCheckResult.NotLive
            }
        } catch (e: Exception) {
            LiveCheckResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Close the OkHttp client and release resources.
     */
    fun close() {
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
    }
}

/**
 * Result of a live status check.
 */
sealed class LiveCheckResult {
    /** A live stream is currently active. */
    data class Live(
        val videoId: String,
        val title: String,
        val viewers: Long? = null,
    ) : LiveCheckResult()

    /** No active live stream detected in recent uploads. */
    data object NotLive : LiveCheckResult()

    /** No recent uploads available to check. */
    data object NoVideos : LiveCheckResult()

    /** An error occurred during the check. */
    data class Error(val message: String) : LiveCheckResult()
}
