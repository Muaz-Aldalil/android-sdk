package com.subulalhuda.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.subulalhuda.data.model.KidGame

/**
 * Kids game screen — supports true-false and matching.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KidsGameScreen(
    gameId: String,
    contentRepository: ContentRepository,
    onBack: () -> Unit,
) {
    val game = contentRepository.getGameById(gameId)
    if (game == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لم يتم العثور على اللعبة")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(game.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
            )
        },
    ) { padding ->
        when (game.type) {
            "true-false" -> TfGame(game = game, modifier = Modifier.padding(padding))
            "matching" -> MatchingGame(game = game, modifier = Modifier.padding(padding))
            else -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text("نوع اللعبة غير معروف")
            }
        }
    }
}

@Composable
private fun TfGame(game: KidGame, modifier: Modifier = Modifier) {
    val allQuestions = game.questions ?: return

    // Shuffled question order — reshuffled on replay; survives process death
    var questionIds by rememberSaveable {
        mutableStateOf(allQuestions.shuffled().map { it.id })
    }
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var score by rememberSaveable { mutableIntStateOf(0) }
    var answered by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableStateOf<Boolean?>(null) }

    val questions = questionIds.mapNotNull { id -> allQuestions.find { it.id == id } }
    val finished = questionIds.isNotEmpty() && currentIndex >= questions.size

    if (finished) {
        val message = when {
            score == questions.size -> "نتيجة مئوية! أنت بطل!"
            score >= questions.size / 2 -> "عمل ممتاز! واصل التعلم"
            else -> "حاول مرة أخرى — ستتحسن!"
        }
        Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("النتيجة", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$score / ${questions.size}",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    questionIds = allQuestions.shuffled().map { it.id }
                    currentIndex = 0
                    score = 0
                    answered = false
                    selected = null
                },
            ) {
                Text("العب مرة أخرى")
            }
        }
        return
    }

    val question = questions.getOrNull(currentIndex) ?: return

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / questions.size },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${currentIndex + 1} / ${questions.size}",
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = question.q,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            listOf(true to "صواب", false to "خطأ").forEach { (value, label) ->
                val isCorrect = value == question.answer
                val isSelected = selected == value
                // Web parity: correct option always green after answering;
                // only the selected-wrong option turns red; others stay neutral.
                val containerColor = when {
                    answered && isCorrect -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                    answered && isSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                OutlinedButton(
                    onClick = {
                        if (!answered) {
                            selected = value
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

        if (answered) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Text(
                    text = question.explanation,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    currentIndex++
                    answered = false
                    selected = null
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (currentIndex < questions.size - 1) "التالي" else "النتيجة")
            }
        }
    }
}

@Composable
private fun MatchingGame(game: KidGame, modifier: Modifier = Modifier) {
    val pairs = game.pairs ?: return

    // Right column order — reshuffled each round (stdlib shuffled() is Fisher–Yates)
    var shuffledRight by remember { mutableStateOf(pairs.shuffled()) }
    // Left index → chosen right value ("" = not yet matched)
    var matchValues by rememberSaveable { mutableStateOf(List(pairs.size) { "" }) }
    var selectedLeft by rememberSaveable { mutableIntStateOf(-1) }
    var showResults by rememberSaveable { mutableStateOf(false) }

    val allMatched = matchValues.all { it.isNotEmpty() }
    val correctCount = pairs.indices.count { matchValues[it] == pairs[it].right }

    if (showResults) {
        Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("النتيجة", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$correctCount / ${pairs.size}",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
            Spacer(modifier = Modifier.height(16.dp))
            pairs.forEachIndexed { i, pair ->
                val isCorrect = matchValues[i] == pair.right
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCorrect) {
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        },
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = pair.left,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text("←", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = matchValues[i].ifEmpty { "—" },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = if (isCorrect) "✓" else "✗",
                            color = if (isCorrect) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    shuffledRight = pairs.shuffled()
                    matchValues = List(pairs.size) { "" }
                    selectedLeft = -1
                    showResults = false
                },
            ) {
                Text("العب مرة أخرى")
            }
        }
        return
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = game.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "المطابقات: ${matchValues.count { it.isNotEmpty() }} / ${pairs.size}",
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (selectedLeft >= 0) "اختر الإجابة الصحيحة على اليمين"
            else "اضغط على العنصر على اليسار أولاً",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Left column — fixed order
        pairs.forEachIndexed { i, pair ->
            val matchedValue = matchValues[i]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { selectedLeft = i },
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        selectedLeft == i -> MaterialTheme.colorScheme.secondaryContainer
                        matchedValue.isNotEmpty() -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                ),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = pair.left,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    if (matchedValue.isNotEmpty()) {
                        Text(
                            text = "→ $matchedValue",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Right column — shuffled; used values are disabled
        shuffledRight.forEach { pair ->
            val used = matchValues.contains(pair.right)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable(enabled = !used && selectedLeft >= 0) {
                        matchValues = matchValues.toMutableList().also { it[selectedLeft] = pair.right }
                        selectedLeft = -1
                    },
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        used -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        selectedLeft >= 0 -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                ),
            ) {
                Text(
                    text = pair.right,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        if (allMatched) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { showResults = true }, modifier = Modifier.fillMaxWidth()) {
                Text("تحقق من الإجابات")
            }
        }
    }
}
