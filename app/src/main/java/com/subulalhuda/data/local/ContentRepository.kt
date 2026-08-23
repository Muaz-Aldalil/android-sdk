package com.subulalhuda.data.local

import android.content.Context
import com.subulalhuda.data.model.*
import kotlinx.serialization.json.Json

/**
 * Provides access to all static content bundled in assets/content/.
 *
 * Content is derived from the website's src/constants/ directory and copied
 * as-is into the Android project. This class is the single point of access.
 *
 * Data is loaded lazily and cached in memory after first access.
 */
class ContentRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    // region Lazy-loaded content

    val sheikhs: List<Sheikh> by lazy { loadJson(ContentReader.Files.SHEIKHS, emptyList()) }

    val quizzes: List<Quiz> by lazy { loadJson(ContentReader.Files.QUIZZES, emptyList()) }

    val kidsGames: List<KidGame> by lazy { loadJson(ContentReader.Files.KIDS_GAMES, emptyList()) }

    val announcements: List<Announcement> by lazy { loadJson(ContentReader.Files.ANNOUNCEMENTS, emptyList()) }

    val rotatingContent: List<RotatingContent> by lazy { loadJson(ContentReader.Files.ROTATING_CONTENT, emptyList()) }

    val categories: List<Category> by lazy { loadJson(ContentReader.Files.CATEGORIES, emptyList()) }

    val socialLinksData: SocialLinks? by lazy { loadSocialLinks() }

    // endregion

    // region Queries

    /** Get a sheikh by ID. */
    fun getSheikhById(id: String): Sheikh? = sheikhs.find { it.id == id }

    /** Get a quiz by ID. */
    fun getQuizById(id: String): Quiz? = quizzes.find { it.id == id }

    /** Get a kids game by ID. */
    fun getGameById(id: String): KidGame? = kidsGames.find { it.id == id }

    /** Get announcements of a specific type. */
    fun getAnnouncementsByType(type: String): List<Announcement> = announcements.filter { it.type == type }

    /** Get featured announcements. */
    fun getFeaturedAnnouncements(): List<Announcement> = announcements.filter { it.featured }

    // endregion

    // region Private

    private inline fun <reified T> loadJson(fileName: String, fallback: T): T {
        val rawJson = ContentReader.readAsset(context, fileName) ?: return fallback
        return try {
            json.decodeFromString<T>(rawJson)
        } catch (e: Exception) {
            fallback
        }
    }

    private fun loadSocialLinks(): SocialLinks? {
        val rawJson = ContentReader.readAsset(context, ContentReader.Files.SOCIAL_LINKS) ?: return null
        return try {
            json.decodeFromString<SocialLinks>(rawJson)
        } catch (e: Exception) {
            null
        }
    }

    // endregion
}
