package com.subulalhuda.util

import android.content.Context
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Manages HTTP disk caching for YouTube API responses.
 *
 * Cache strategy:
 * - Video metadata: 24 hours TTL
 * - Uploads playlist ID: 7 days TTL (stable for channel lifetime)
 * - Live status: 5 minutes TTL (best-effort detection)
 *
 * Total cache size: 10MB
 * Cache location: Android app cache directory
 *
 * IMPORTANT: This is NOT a background polling cache.
 * All cache reads happen when the user is actively viewing relevant UI.
 */
object CacheManager {

    private const val CACHE_SIZE_MB = 10L
    private const val CACHE_DIR_NAME = "youtube_cache"

    /** Video metadata cache — 24 hours */
    private const val CACHE_MAX_AGE_HOURS = 24

    /** Uploads playlist ID cache — 7 days */
    private const val PLAYLIST_CACHE_MAX_AGE_DAYS = 7

    /** Live status cache — 5 minutes */
    private const val LIVE_CACHE_MAX_AGE_MINUTES = 5

    /**
     * Create a configured OkHttp client with disk caching.
     *
     * @param context Application context
     */
    fun createCachedClient(context: Context): OkHttpClient {
        val cacheDir = File(context.cacheDir, CACHE_DIR_NAME)
        val cache = Cache(cacheDir, CACHE_SIZE_MB * 1024 * 1024)

        return OkHttpClient.Builder()
            .cache(cache)
            .build()
    }

    /**
     * Cache control for video metadata — 24 hours.
     */
    fun videoMetadataCacheControl(): CacheControl {
        return CacheControl.Builder()
            .maxAge(CACHE_MAX_AGE_HOURS, TimeUnit.HOURS)
            .build()
    }

    /**
     * Cache control for uploads playlist ID — 7 days.
     * The uploads playlist ID is stable for a channel's lifetime,
     * so a long cache duration is safe.
     */
    fun playlistIdCacheControl(): CacheControl {
        return CacheControl.Builder()
            .maxAge(PLAYLIST_CACHE_MAX_AGE_DAYS, TimeUnit.DAYS)
            .build()
    }

    /**
     * Cache control for live status — 5 minutes.
     * Live status changes frequently, so short TTL.
     */
    fun liveStatusCacheControl(): CacheControl {
        return CacheControl.Builder()
            .maxAge(LIVE_CACHE_MAX_AGE_MINUTES, TimeUnit.MINUTES)
            .build()
    }

    /**
     * Clear all cached data.
     */
    fun clearCache(context: Context) {
        val cacheDir = File(context.cacheDir, CACHE_DIR_NAME)
        if (cacheDir.exists()) {
            cacheDir.deleteRecursively()
        }
    }
}
