package com.subulalhuda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.subulalhuda.data.local.ContentRepository
import com.subulalhuda.data.repository.YouTubeRepository
import com.subulalhuda.util.formatArabicDate

/**
 * Lectures screen — live feed of recent uploads with category keyword filtering.
 *
 * Loads up to 50 recent uploads via the YouTube Data API when a key is configured.
 * Category chips filter by Arabic keyword in the video title (mirroring the web's
 * LecturesPage.jsx logic, plus the maqamat branch the web is missing).
 *
 * Three states: loading (skeleton), error (message + retry), content (chips + list).
 *
 * @param contentRepository Local content (categories list).
 * @param youtubeRepository YouTube API access; null when API key is blank.
 * @param onVideoClick Navigate to video player: (videoId).
 * @param onSearchClick Navigate to the search screen.
 */
@Composable
fun LecturesScreen(
    contentRepository: ContentRepository,
    youtubeRepository: YouTubeRepository?,
    onVideoClick: (String) -> Unit,
    onSearchClick: () -> Unit,
) {
    val categories = contentRepository.categories
    var selectedCategory by remember { mutableStateOf("all") }

    var lectures by remember { mutableStateOf<List<LectureItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var retryTrigger by remember { mutableIntStateOf(0) }

    // Fetch on mount and on retry
    LaunchedEffect(retryTrigger) {
        val repo = youtubeRepository
        if (repo == null) {
            error = "لم يتم تكوين مفتاح YouTube"
            isLoading = false
            return@LaunchedEffect
        }
        isLoading = true
        error = null
        try {
            val items = repo.getRecentUploads(50)
            lectures = items.mapNotNull { item ->
                val id = item.contentDetails?.videoId ?: return@mapNotNull null
                val snippet = item.snippet ?: return@mapNotNull null
                LectureItem(
                    videoId = id,
                    title = snippet.title ?: "",
                    publishedAt = snippet.publishedAt ?: "",
                    thumbnailUrl = snippet.thumbnails?.high?.url
                        ?: "https://img.youtube.com/vi/$id/mqdefault.jpg",
                )
            }
        } catch (e: Exception) {
            error = e.message ?: "خطأ غير معروف"
        }
        isLoading = false
    }

    val filteredLectures = remember(lectures, selectedCategory) {
        lectures.filter { matchesCategory(selectedCategory, it.title) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "الدروس والمحاضرات",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Light,
            )
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = "بحث")
            }
        }

        // Category chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category.id,
                    onClick = { selectedCategory = category.id },
                    label = { Text(category.label) },
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when {
            isLoading -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(3) { SkeletonRow() }
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { retryTrigger++ }) {
                            Text("إعادة المحاولة")
                        }
                    }
                }
            }

            filteredLectures.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (lectures.isEmpty()) "لا توجد دروس"
                        else "لا توجد نتائج مطابقة",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filteredLectures) { lecture ->
                        LectureRow(lecture = lecture, onClick = { onVideoClick(lecture.videoId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun LectureRow(lecture: LectureItem, onClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = lecture.thumbnailUrl,
                contentDescription = lecture.title,
                modifier = Modifier
                    .size(120.dp, 68.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lecture.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatArabicDate(lecture.publishedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SkeletonRow() {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp, 68.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surface),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surface),
                )
            }
        }
    }
}

// --- Helpers ---

internal data class LectureItem(
    val videoId: String,
    val title: String,
    val publishedAt: String,
    val thumbnailUrl: String,
)

/**
 * Category keyword matching — mirrors web LecturesPage.jsx filter logic.
 * The maqamat branch is added here; the web is missing it (selecting maqamat
 * there matches nothing).
 */
private fun matchesCategory(category: String, title: String): Boolean = when (category) {
    "all" -> true
    "khutbah" -> title.contains("خطبة")
    "lectures" -> title.contains("محاضرة")
    "lessons" -> title.contains("درس")
    "maqamat" -> title.contains("مقام") || title.contains("ابتهال")
    "tafsir" -> title.contains("تفسير")
    "refutations" -> title.contains("شبه") || title.contains("رد")
    "fatawa" -> title.contains("فتوى")
    "clips" -> title.contains("مقتطفات")
    else -> false
}
