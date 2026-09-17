package com.subulalhuda.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.subulalhuda.data.local.ContentRepository
import com.subulalhuda.data.model.Question

/**
 * Quiz-taking screen — setup (difficulty + count) then one question at a time.
 *
 * Flow: setup → shuffled subset → questions → onQuizComplete(score, total, diff, count).
 * Re-entering with [initialCount] > 0 (retry flow) skips setup and starts
 * immediately with the same settings, reshuffled.
 *
 * Session state (question order, index, score) is rememberSaveable so process
 * death doesn't lose a mid-quiz run.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    quizId: String,
    contentRepository: ContentRepository,
    initialDifficulty: String?,
    initialCount: Int,
    onQuizComplete: (score: Int, total: Int, difficulty: String, count: Int) -> Unit,
    onBack: () -> Unit,
) {
    val quiz = contentRepository.getQuizById(quizId)
    if (quiz == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لم يتم العثور على الاختبار")
        }
        return
    }

    // Session state — saveable across process death
    var difficulty by rememberSaveable { mutableStateOf(initialDifficulty ?: "medium") }
    var count by rememberSaveable { mutableIntStateOf(initialCount) }
    var questionIds by rememberSaveable { mutableStateOf<List<Int>>(emptyList()) }
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var score by rememberSaveable { mutableIntStateOf(0) }
    var answered by rememberSaveable { mutableStateOf(false) }
    var selectedAnswer by rememberSaveable { mutableIntStateOf(-1) }

    val questions = questionIds.mapNotNull { id -> quiz.questions.find { it.id == id } }
    val total = questions.size
    val inSetup = questionIds.isEmpty()

    fun startQuiz(diff: String, cnt: Int) {
        val pool = quiz.questions.filter { it.difficulty == diff }
        if (pool.isEmpty()) {
            questionIds = emptyList()
            return
        }
        val effectiveCount = if (cnt == 0) pool.size else cnt.coerceIn(1, pool.size)
        questionIds = pool.shuffled().take(effectiveCount).map { it.id }
        currentIndex = 0
        score = 0
        answered = false
        selectedAnswer = -1
    }

    // Retry flow: entered with explicit settings → start immediately, reshuffled
    LaunchedEffect(Unit) {
        if (questionIds.isEmpty() && initialCount > 0) {
            startQuiz(initialDifficulty ?: "medium", initialCount)
        }
    }

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
        if (inSetup) {
            QuizSetup(
                quizTitle = quiz.title,
                quizDescription = quiz.description,
                questions = quiz.questions,
                difficulty = difficulty,
                count = count,
                onDifficultyChange = { diff ->
                    difficulty = diff
                    // Clamp count to what the new difficulty actually has (web parity)
                    val avail = quiz.questions.count { it.difficulty == diff }
                    if (count != 0 && count > avail) count = avail
                },
                onCountChange = { count = it },
                onStart = { startQuiz(difficulty, count) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Progress — guarded against zero questions
                LinearProgressIndicator(
                    progress = { if (total > 0) (currentIndex + 1).toFloat() / total else 0f },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "سؤال ${currentIndex + 1} من $total",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))

                val question = questions.getOrNull(currentIndex)
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
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
                                    // Pass the effective count (total) so retry replays
                                    // the same number of questions even when "الكل" was chosen
                                    onQuizComplete(score, total, difficulty, total)
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
}

/**
 * Setup stage — difficulty (سهل/متوسط/صعب) and question count, capped at
 * what the chosen difficulty actually has.
 */
@Composable
private fun QuizSetup(
    quizTitle: String,
    quizDescription: String,
    questions: List<Question>,
    difficulty: String,
    count: Int,
    onDifficultyChange: (String) -> Unit,
    onCountChange: (Int) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val difficulties = listOf(
        "easy" to "سهل",
        "medium" to "متوسط",
        "hard" to "صعب",
    )
    val available = questions.count { it.difficulty == difficulty }
    val countOptions = listOf(5, 10, 15, 20, 0) // 0 = all available

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = quizTitle,
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = quizDescription,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Difficulty
        Text(
            text = "الصعوبة",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.align(Alignment.Start),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            difficulties.forEach { (key, label) ->
                val questionCount = questions.count { it.difficulty == key }
                FilterChip(
                    selected = difficulty == key,
                    onClick = { onDifficultyChange(key) },
                    label = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label)
                            Text(
                                text = "$questionCount سؤال",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Count
        Text(
            text = "عدد الأسئلة",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.align(Alignment.Start),
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (available == 0) {
            Text(
                text = "لا توجد أسئلة في هذه الصعوبة — جرّب صعوبة أخرى",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                countOptions.forEach { option ->
                    val label = if (option == 0) "الكل" else "$option"
                    val enabled = option == 0 || option <= available
                    FilterChip(
                        selected = if (count == 0) option == 0 else count == option,
                        onClick = { onCountChange(option) },
                        label = { Text(label) },
                        enabled = enabled,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStart,
            enabled = available > 0,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("ابدأ الاختبار")
        }
    }
}