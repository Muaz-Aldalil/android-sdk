package com.subulalhuda.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.subulalhuda.data.local.ContentRepository

/**
 * Quiz result screen — shows score and encouragement.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultScreen(
    quizId: String,
    score: Int,
    total: Int,
    contentRepository: ContentRepository,
    onBack: () -> Unit,
) {
    val quiz = contentRepository.getQuizById(quizId)
    val percentage = if (total > 0) (score * 100 / total) else 0

    val message = when {
        percentage >= 80 -> "ممتاز! أداء رائع"
        percentage >= 60 -> "جيد جداً! واصل التعلم"
        percentage >= 40 -> "جيد! يمكنك التحسن"
        else -> "حاول مرة أخرى — التعلم رحلة"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("النتيجة") },
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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = quiz?.title ?: "",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "$score / $total",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Light,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("العودة للاختبارات")
            }
        }
    }
}
