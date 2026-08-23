package com.subulalhuda.data.local

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * Reads JSON content files from the assets/content/ directory.
 * Each file is a static snapshot of the website's JavaScript constants.
 *
 * Files (derived from website's src/constants/):
 *   sheikhs.json          ← SHEIKHS.js
 *   quizzes.json          ← QUIZZES.js
 *   kids_games.json       ← KIDS_GAMES.js
 *   announcements.json    ← ANNOUNCEMENTS.js
 *   rotating_content.json ← ROTATING_CONTENT.js
 *   categories.json       ← CATEGORIES.js
 *   social_links.json     ← SOCIAL_LINKS.jsx (data portion only)
 */
object ContentReader {

    @PublishedApi internal val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * Read a JSON file from assets/content/ and return its raw content as a string.
     * Returns null if the file doesn't exist or can't be read.
     */
    fun readAsset(context: Context, fileName: String): String? {
        return try {
            context.assets.open("content/$fileName").bufferedReader().use { it.readText().removePrefix("\uFEFF") }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Read and parse a JSON file from assets/content/ into a typed object.
     */
    inline fun <reified T> readAndParse(context: Context, fileName: String): T? {
        val rawJson = readAsset(context, fileName) ?: return null
        return try {
            json.decodeFromString<T>(rawJson)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Read a JSON file and return it as a raw JsonElement.
     * Useful for debugging and data comparison.
     */
    fun readAsJsonElement(context: Context, fileName: String): JsonElement? {
        val rawJson = readAsset(context, fileName) ?: return null
        return try {
            json.parseToJsonElement(rawJson)
        } catch (e: Exception) {
            null
        }
    }

    /** Available content files in assets/content/ */
    object Files {
        const val SHEIKHS = "sheikhs.json"
        const val QUIZZES = "quizzes.json"
        const val KIDS_GAMES = "kids_games.json"
        const val ANNOUNCEMENTS = "announcements.json"
        const val ROTATING_CONTENT = "rotating_content.json"
        const val CATEGORIES = "categories.json"
        const val SOCIAL_LINKS = "social_links.json"
    }
}
