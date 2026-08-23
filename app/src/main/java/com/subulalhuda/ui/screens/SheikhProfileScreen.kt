package com.subulalhuda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.subulalhuda.data.local.ContentRepository

/**
 * Sheikh profile — avatar, name, bio, and their videos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheikhProfileScreen(
    sheikhId: String,
    contentRepository: ContentRepository,
    onVideoClick: (String) -> Unit,
    onBack: () -> Unit,
) {
    val sheikh = contentRepository.getSheikhById(sheikhId)

    if (sheikh == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("لم يتم العثور على الشيخ", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onBack) { Text("رجوع") }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sheikh.name) },
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
            // Profile header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AsyncImage(
                        model = sheikh.avatar,
                        contentDescription = sheikh.name,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = sheikh.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Normal,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sheikh.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${sheikh.videoIds.size} فيديو",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Videos list
            item {
                Text(
                    text = "فيديوهات الشيخ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
            }

            items(sheikh.videoIds) { videoId ->
                VideoListItem(videoId = videoId, onClick = { onVideoClick(videoId) })
            }
        }
    }
}

@Composable
fun VideoListItem(videoId: String, onClick: () -> Unit) {
    val thumbnailUrl = "https://img.youtube.com/vi/$videoId/mqdefault.jpg"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = videoId,
                modifier = Modifier
                    .size(120.dp, 68.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = videoId,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
