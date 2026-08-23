package com.subulalhuda.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.subulalhuda.data.local.ContentRepository
import com.subulalhuda.data.model.KidGame
import com.subulalhuda.data.model.Quiz

/**
 * Interactive screen — quizzes (4) and kids games (3).
 */
@Composable
fun InteractiveScreen(
    contentRepository: ContentRepository,
    onQuizClick: (String) -> Unit,
    onGameClick: (String) -> Unit,
) {
    val quizzes = contentRepository.quizzes
    val games = contentRepository.kidsGames

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                text = "التفاعلي",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        // Quizzes section
        item {
            Text(
                text = "اختبارات",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }

        items(quizzes) { quiz ->
            QuizCard(quiz = quiz, onClick = { onQuizClick(quiz.id) })
        }

        // Kids games section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ألعاب الأطفال",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }

        items(games) { game ->
            GameCard(game = game, onClick = { onGameClick(game.id) })
        }
    }
}

@Composable
fun QuizCard(quiz: Quiz, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = quiz.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = quiz.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${quiz.questions.size} سؤال",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (quiz.type == "multiple-choice") "اختيار من متعدد" else "صواب / خطأ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun GameCard(game: KidGame, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = game.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = game.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${game.questionCount} سؤال",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
