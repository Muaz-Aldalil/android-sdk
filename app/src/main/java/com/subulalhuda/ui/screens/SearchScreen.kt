package com.subulalhuda.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.subulalhuda.data.local.ContentRepository

/**
 * Search screen — search across sheikhs and quizzes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    contentRepository: ContentRepository,
    onVideoClick: (String) -> Unit,
    onSheikhClick: (String) -> Unit,
    onBack: () -> Unit,
) {
    var query by remember { mutableStateOf("") }

    val results = remember(query) {
        if (query.isBlank()) null
        else {
            val sheikhs = contentRepository.sheikhs.filter {
                it.name.contains(query, ignoreCase = true) || it.bio.contains(query, ignoreCase = true)
            }
            val quizzes = contentRepository.quizzes.filter {
                it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
            }
            val games = contentRepository.kidsGames.filter {
                it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
            }
            Triple(sheikhs, quizzes, games)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("بحث...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (results != null) {
                val (sheikhs, quizzes, games) = results
                val total = sheikhs.size + quizzes.size + games.size

                if (total == 0 && query.isNotBlank()) {
                    item {
                        Text(
                            text = "لا توجد نتائج لـ \"$query\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(32.dp),
                        )
                    }
                }

                if (sheikhs.isNotEmpty()) {
                    item { Text("العلماء", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium) }
                    items(sheikhs) { sheikh ->
                        ListItem(
                            headlineContent = { Text(sheikh.name) },
                            supportingContent = { Text(sheikh.bio) },
                            modifier = Modifier.clickable { onSheikhClick(sheikh.id) },
                        )
                    }
                }

                if (quizzes.isNotEmpty()) {
                    item { Text("اختبارات", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium) }
                    items(quizzes) { quiz ->
                        ListItem(
                            headlineContent = { Text(quiz.title) },
                            supportingContent = { Text(quiz.description) },
                        )
                    }
                }

                if (games.isNotEmpty()) {
                    item { Text("ألعاب", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium) }
                    items(games) { game ->
                        ListItem(
                            headlineContent = { Text(game.title) },
                            supportingContent = { Text(game.description) },
                        )
                    }
                }
            } else {
                item {
                    Text(
                        text = "ابحث عن العلماء والاختبارات والألعاب",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(32.dp),
                    )
                }
            }
        }
    }
}
