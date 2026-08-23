package com.subulalhuda.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

/**
 * A quiz with questions. Can be multiple-choice or true-false.
 * Derived from the website's src/constants/QUIZZES.js — extracted as-is.
 *
 * MCQ: [Question.correct] is the 0-based index into [Question.options].
 * T/F: [Question.correct] is a boolean ("true"/"false" as string for JSON compat).
 */
@Serializable
data class Quiz(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val type: String, // "multiple-choice" or "true-false"
    val icon: String? = null,
    val questions: List<Question>,
)

@Serializable
data class Question(
    val id: Int,
    val q: String,
    val options: List<String>? = null,
    @SerialName("correct") val correctRaw: kotlinx.serialization.json.JsonElement,
    val explanation: String,
    val difficulty: String,
    val source: String,
) {
    /**
     * For multiple-choice: correct is 0-based index into options.
     * For true-false: correct is boolean.
     * We store as JsonElement and resolve at runtime based on quiz type.
     */
    fun correctIndex(): Int = correctRaw.toString().toIntOrNull() ?: -1

    fun correctBoolean(): Boolean = when {
        correctRaw.toString() == "true" -> true
        correctRaw.toString() == "false" -> false
        else -> correctRaw.toString().toIntOrNull()?.let { it == 1 } ?: false
    }
}
