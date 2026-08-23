package com.subulalhuda.data.model

import kotlinx.serialization.Serializable

/**
 * A kids game — can be true-false or matching.
 * Derived from the website's src/constants/KIDS_GAMES.js — extracted as-is.
 *
 * Note: The field name is "answer" (boolean) here, unlike quizzes which use "correct".
 * This inconsistency is preserved from the website source.
 */
@Serializable
data class KidGame(
    val id: String,
    val title: String,
    val description: String,
    val type: String, // "true-false" or "matching"
    val icon: String,
    val questionCount: Int,
    val questions: List<KidQuestion>? = null,
    val pairs: List<MatchingPair>? = null,
)

@Serializable
data class KidQuestion(
    val id: Int,
    val q: String,
    val answer: Boolean, // note: field name is "answer", not "correct" (matches website source)
    val explanation: String,
)

@Serializable
data class MatchingPair(
    val left: String,
    val right: String,
)
