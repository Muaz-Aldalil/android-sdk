package com.subulalhuda.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    val questions = game.questions ?: return
    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var answered by remember { mutableStateOf(false) }

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
                val containerColor = when {
                    answered && isCorrect -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                    answered && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                OutlinedButton(
                    onClick = {
                        if (!answered) {
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
                    if (currentIndex < questions.size - 1) {
                        currentIndex++
                        answered = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (currentIndex < questions.size - 1) "التالي" else "انتهت اللعبة — النتيجة: $score / ${questions.size}")
            }
        }
    }
}

@Composable
private fun MatchingGame(game: KidGame, modifier: Modifier = Modifier) {
    val pairs = game.pairs ?: return
    var matchedCount by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = game.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "المطابقات: $matchedCount / ${pairs.size}",
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(modifier = Modifier.height(16.dp))

        pairs.forEach { pair ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(pair.left, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text("←", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                    Text(pair.right, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
