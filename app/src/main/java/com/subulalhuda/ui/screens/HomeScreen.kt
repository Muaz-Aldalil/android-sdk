package com.subulalhuda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.subulalhuda.data.local.ContentRepository
import com.subulalhuda.data.model.Announcement
import com.subulalhuda.data.model.RotatingContent
import com.subulalhuda.data.model.Sheikh
import com.subulalhuda.data.repository.YouTubeRepository
import kotlinx.coroutines.delay

/**
 * Home screen — hero with rotating verse/hadith, announcements, sheikhs, recent videos.
 * Mirrors the website's HomePage.jsx layout.
 */
@Composable
fun HomeScreen(
    contentRepository: ContentRepository,
    youtubeRepository: YouTubeRepository?,
    onSheikhClick: (String) -> Unit,
    onVideoClick: (String) -> Unit,
    onLecturesClick: () -> Unit,
) {
    val rotatingContent = contentRepository.rotatingContent
    val announcements = contentRepository.getFeaturedAnnouncements() + contentRepository.announcements.filter { !it.featured }
    val sheikhs = contentRepository.sheikhs

    // Rotating verse/hadith — auto-rotate every 8 seconds
    var currentIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(rotatingContent.size) {
        while (true) {
            delay(8000)
            if (rotatingContent.isNotEmpty()) {
                currentIndex = (currentIndex + 1) % rotatingContent.size
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        // Hero Section — rotating verse/hadith
        if (rotatingContent.isNotEmpty()) {
            item {
                HeroSection(
                    content = rotatingContent[currentIndex],
                    onVerseClick = { onLecturesClick() },
                )
            }
        }

        // Announcements
        if (announcements.isNotEmpty()) {
            item {
                SectionHeader(title = "آخر الأخبار")
            }
            items(announcements.take(4)) { announcement ->
                AnnouncementCard(
                    announcement = announcement,
                    onSheikhClick = onSheikhClick,
                )
            }
        }

        // Featured Sheikhs
        if (sheikhs.isNotEmpty()) {
            item {
                SectionHeader(title = "العلماء")
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(sheikhs.take(6)) { sheikh ->
                        SheikhCard(
                            sheikh = sheikh,
                            onClick = { onSheikhClick(sheikh.id) },
                        )
                    }
                }
            }
        }

        // Quick links
        item {
            SectionHeader(title = "تصفح المحتوى")
        }
        item {
            QuickLinksRow(onLecturesClick = onLecturesClick)
        }
    }
}

@Composable
fun HeroSection(
    content: RotatingContent,
    onVerseClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = content.text,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable(onClick = onVerseClick),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content.source,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Normal,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
fun AnnouncementCard(
    announcement: Announcement,
    onSheikhClick: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = announcement.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                AnnouncementTypeBadge(type = announcement.type)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = announcement.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            announcement.sheikhId?.let { sheikhId ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "اضغط لعرض صفحة الشيخ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.clickable { onSheikhClick(sheikhId) },
                )
            }
        }
    }
}

@Composable
fun AnnouncementTypeBadge(type: String) {
    val (label, color) = when (type) {
        "live" -> "مباشر" to Color(0xFFDC2626)
        "new" -> "جديد" to Color(0xFF16A34A)
        "event" -> "حدث" to Color(0xFFD4A017)
        "upcoming" -> "قريباً" to Color(0xFF2563EB)
        else -> type to Color.Gray
    }
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.1f),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
fun SheikhCard(
    sheikh: Sheikh,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AsyncImage(
                model = sheikh.avatar,
                contentDescription = sheikh.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = sheikh.name,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun QuickLinksRow(onLecturesClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onLecturesClick),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "الدروس",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
    }
}
