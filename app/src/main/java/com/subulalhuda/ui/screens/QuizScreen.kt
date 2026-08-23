package com.subulalhuda.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.subulalhuda.data.local.ContentRepository
import com.subulalhuda.data.model.Quiz

/**
 * Quiz-taking screen — one question at a time.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    quizId: String,
    contentRepository: ContentRepository,
    onQuizComplete: (score: Int, total: Int) -> Unit,
    onBack: () -> Unit,
) {
    val quiz = contentRepository.getQuizById(quizId)
    if (quiz == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لم يتم العثور على الاختبار")
        }
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var answered by remember { mutableStateOf(false) }
    var selectedAnswer by remember { mutableStateOf(-1) }

    val question = quiz.questions.getOrNull(currentIndex)
    val total = quiz.questions.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(quiz.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Progress
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / total },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "سؤال ${currentIndex + 1} من $total",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))

            question?.let { q ->
                // Question
                Text(
                    text = q.q,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (quiz.type == "multiple-choice" && q.options != null) {
                    // MCQ options
                    q.options.forEachIndexed { index, option ->
                        val isSelected = selectedAnswer == index
                        val isCorrect = index == q.correctIndex()
                        val containerColor = when {
                            answered && isCorrect -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                            answered && isSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }

                        OutlinedButton(
                            onClick = {
                                if (!answered) {
                                    selectedAnswer = index
                                    answered = true
                                    if (isCorrect) score++
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = containerColor),
                        ) {
                            Text(option, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    // True/False
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        val correctAnswer = q.correctBoolean()
                        listOf(true to "صواب", false to "خطأ").forEach { (value, label) ->
                            val isCorrect = value == correctAnswer
                            val isSelected = selectedAnswer == if (value) 1 else 0
                            val containerColor = when {
                                answered && isCorrect -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                answered && isSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            OutlinedButton(
                                onClick = {
                                    if (!answered) {
                                        selectedAnswer = if (value) 1 else 0
                                        answered = true
                                        if (isCorrect) score++
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = containerColor),
                            ) {
                                Text(label, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }

                // Explanation after answering
                if (answered) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                    ) {
                        Text(
                            text = q.explanation,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (currentIndex < total - 1) {
                                currentIndex++
                                answered = false
                                selectedAnswer = -1
                            } else {
                                onQuizComplete(score, total)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (currentIndex < total - 1) "السؤال التالي" else "النتيجة")
                    }
                }
            }
        }
    }
}
