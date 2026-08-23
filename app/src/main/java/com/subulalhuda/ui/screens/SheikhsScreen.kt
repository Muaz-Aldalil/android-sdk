package com.subulalhuda.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.subulalhuda.data.local.ContentRepository
import com.subulalhuda.data.model.Sheikh

/**
 * Sheikhs grid — displays all 15 sheikh profiles.
 * Clicking a sheikh navigates to their profile.
 */
@Composable
fun SheikhsScreen(
    contentRepository: ContentRepository,
    onSheikhClick: (String) -> Unit,
) {
    val sheikhs = contentRepository.sheikhs

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "العلماء",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        items(sheikhs) { sheikh ->
            SheikhListItem(
                sheikh = sheikh,
                onClick = { onSheikhClick(sheikh.id) },
            )
        }
    }
}

@Composable
fun SheikhListItem(
    sheikh: Sheikh,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = sheikh.avatar,
                contentDescription = sheikh.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sheikh.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sheikh.bio,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${sheikh.videoIds.size} فيديو",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
