package com.subulalhuda.data.remote

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Low-level YouTube Data API v3 client using OkHttp.
 *
 * This client handles:
 * 1. Fetching recent uploads from the channel's uploads playlist
 * 2. Fetching video metadata with live streaming details
 *
 * Each API call costs 1 unit of quota.
 * search.list costs 100 units — DO NOT use unless absolutely necessary.
 *
 * IMPORTANT: The API key must be configured separately from the website's key.
 * Do NOT reuse the website's .env key.
 *
 * @param apiKey YouTube Data API v3 key
 * @param channelId YouTube channel ID
 * @param client OkHttp client (shared instance recommended)
 */
class YouTubeApiClient(
    private val apiKey: String,
    private val channelId: String,
    private val client: OkHttpClient,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    companion object {
        private const val BASE_URL = "https://www.googleapis.com/youtube/v3"
    }

    /**
     * Fetch the channel's uploads playlist ID.
     * This is a channel content detail, not a direct playlist reference.
     * The uploads playlist ID is stable for the channel's lifetime.
     *
     * Quota cost: 1 unit
     */
    fun getUploadsPlaylistId(): String? {
        val url = "$BASE_URL/channels" +
            "?part=contentDetails" +
            "&id=$channelId" +
            "&key=$apiKey"

        val request = Request.Builder().url(url).get().build()
        val response = client.newCall(request).execute()

        return response.use { resp ->
            if (!resp.isSuccessful) return@use null

            val body = resp.body?.string() ?: return@use null
            val channelResponse = json.parseToJsonElement(body)
            val items = channelResponse
                .jsonObject["items"]
                ?.jsonArray

            if (items.isNullOrEmpty()) return@use null

            items[0]
                .jsonObject["contentDetails"]
                ?.jsonObject["relatedPlaylists"]
                ?.jsonObject["uploads"]
                ?.jsonPrimitive
                ?.content
        }
    }

    /**
     * Fetch recent videos from the uploads playlist.
     *
     * @param playlistId The uploads playlist ID
     * @param maxResults Number of videos to fetch (default 5, max 50)
     * Quota cost: 1 unit
     */
    fun getRecentUploads(playlistId: String, maxResults: Int = 5): List<PlaylistItem> {
        val url = "$BASE_URL/playlistItems" +
            "?part=snippet,contentDetails" +
            "&playlistId=$playlistId" +
            "&maxResults=$maxResults" +
            "&key=$apiKey"

        val request = Request.Builder().url(url).get().build()
        val response = client.newCall(request).execute()

        return response.use { resp ->
            if (!resp.isSuccessful) return@use emptyList()

            val body = resp.body?.string() ?: return@use emptyList()
            val playlistResponse = json.decodeFromString<PlaylistItemsResponse>(body)
            playlistResponse.items
        }
    }

    /**
     * Fetch video details including live streaming details.
     *
     * This is the preferred way to check live status (3 units for 50 IDs
     * vs 100 units per search.list call).
     *
     * LIMITATION: Only videos in the recent uploads window are checked.
     * If a livestream is older than the most recent uploads, it won't be detected.
     * We accept this trade-off to keep quota usage low.
     *
     * @param videoIds List of video IDs to check
     * Quota cost: 1 unit (regardless of number of IDs, up to 50)
     */
    fun getVideoDetails(videoIds: List<String>): List<VideoItem> {
        if (videoIds.isEmpty()) return emptyList()

        val ids = videoIds.joinToString(",")
        val url = "$BASE_URL/videos" +
            "?part=snippet,contentDetails,liveStreamingDetails" +
            "&id=$ids" +
            "&key=$apiKey"

        val request = Request.Builder().url(url).get().build()
        val response = client.newCall(request).execute()

        return response.use { resp ->
            if (!resp.isSuccessful) return@use emptyList()

            val body = resp.body?.string() ?: return@use emptyList()
            val videosResponse = json.decodeFromString<VideosResponse>(body)
            videosResponse.items
        }
    }
}
