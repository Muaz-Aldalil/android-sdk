package com.subulalhuda.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.subulalhuda.data.local.ContentRepository
import com.subulalhuda.data.model.Announcement
import com.subulalhuda.data.model.RotatingContent
import com.subulalhuda.data.model.Sheikh
import com.subulalhuda.data.repository.LiveCheckResult
import com.subulalhuda.data.repository.YouTubeRepository
import com.subulalhuda.util.formatArabicDate
import kotlinx.coroutines.delay

/**
 * Home screen — live banner, rotating verse hero with dots, announcements,
 * featured sheikhs, latest videos (from the YouTube API), and quick links.
 * Mirrors the website's HomePage.jsx layout.
 *
 * The live banner and latest-videos section are guarded: with no API key
 * configured (repository null) they render nothing, not a spinner.
 */
@Composable
fun HomeScreen(
    contentRepository: ContentRepository,
    youtubeRepository: YouTubeRepository?,
    onSheikhClick: (String) -> Unit,
    onVideoClick: (String) -> Unit,
    onLecturesClick: () -> Unit,
    onInteractiveClick: () -> Unit,
    onContactClick: () -> Unit,
    onSheikhsClick: () -> Unit,
) {
    val context = LocalContext.current
    val rotatingContent = contentRepository.rotatingContent
    val announcements = contentRepository.getFeaturedAnnouncements() +
        contentRepository.announcements.filter { !it.featured }
    val sheikhs = contentRepository.sheikhs

    // Rotating verse/hadith — auto-rotate every 8 seconds, manual via dots
    var currentIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(rotatingContent.size) {
        while (true) {
            delay(8000)
            if (rotatingContent.isNotEmpty()) {
                currentIndex = (currentIndex + 1) % rotatingContent.size
            }
        }
    }

    // Live status — only when an API key is configured
    var liveVideo by remember { mutableStateOf<LiveCheckResult.Live?>(null) }
    LaunchedEffect(Unit) {
        val repo = youtubeRepository ?: return@LaunchedEffect
        val result = repo.checkLiveStatus()
        if (result is LiveCheckResult.Live) liveVideo = result
    }

    // Latest videos — guarded the same way
    var latestVideos by remember { mutableStateOf<List<LectureItem>>(emptyList()) }
    var videosLoading by remember { mutableStateOf(youtubeRepository != null) }
    var videosError by remember { mutableStateOf<String?>(null) }
    var videosRetry by remember { mutableIntStateOf(0) }

    LaunchedEffect(videosRetry) {
        val repo = youtubeRepository ?: return@LaunchedEffect
        videosLoading = true
        videosError = null
        try {
            latestVideos = repo.getRecentUploads(8).mapNotNull { item ->
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
            videosError = e.message ?: "خطأ غير معروف"
        }
        videosLoading = false
    }

    fun openExternal(url: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: ActivityNotFoundException) { }
    }

    fun handleAnnouncementClick(announcement: Announcement) {
        val link = announcement.link
        when {
            link == null && announcement.sheikhId != null -> onSheikhClick(announcement.sheikhId)
            link == null -> Unit
            link.startsWith("http") -> openExternal(link)
            link.startsWith("/sheikhs/") -> onSheikhClick(link.removePrefix("/sheikhs/"))
            link == "/sheikhs" -> onSheikhsClick()
            link == "/lectures" -> onLecturesClick()
            link == "/interactive" -> onInteractiveClick()
            link == "/contact" -> onContactClick()
            else -> Unit // unknown internal route — ignore
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        // Live banner — only when a live stream is detected
        liveVideo?.let { live ->
            item {
                LiveBanner(
                    title = live.title,
                    onClick = { onVideoClick(live.videoId) },
                )
            }
        }

        // Hero Section — rotating verse/hadith
        if (rotatingContent.isNotEmpty()) {
            item {
                HeroSection(
                    content = rotatingContent[currentIndex],
                    activeIndex = currentIndex,
                    totalCount = rotatingContent.size,
                    onDotClick = { currentIndex = it },
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
                    onClick = { handleAnnouncementClick(announcement) },
                )
            }
        }

        // Featured Sheikhs
        if (sheikhs.isNotEmpty()) {
            item {
                SectionHeader(title = "نخبة من العلماء")
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

        // Latest videos — guarded: hidden entirely without an API key
        if (youtubeRepository != null) {
            item {
                SectionHeader(title = "آخر الدروس")
            }

            when {
                videosLoading -> {
                    items(3) { VideoSkeletonRow() }
                }

                videosError != null -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = videosError!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { videosRetry++ }) {
                                Text("إعادة المحاولة")
                            }
                        }
                    }
                }

                latestVideos.isEmpty() -> {
                    item {
                        Text(
                            text = "لا توجد دروس حالياً",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                else -> {
                    items(latestVideos) { video ->
                        VideoRow(
                            video = video,
                            onClick = { onVideoClick(video.videoId) },
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
            QuickLinksRow(
                onInteractiveClick = onInteractiveClick,
                onContactClick = onContactClick,
            )
        }
    }
}

/** Red live banner — semantic live-red is permitted by the design lock. */
@Composable
fun LiveBanner(title: String, onClick: () -> Unit) {
    val pulse by rememberInfiniteTransition(label = "livePulse").animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "livePulseAlpha",
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDC2626)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = pulse)),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "بث مباشر",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun HeroSection(
    content: RotatingContent,
    activeIndex: Int,
    totalCount: Int,
    onDotClick: (Int) -> Unit,
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

            // Verse progress dots — manual selection
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(totalCount) { i ->
                    val selected = i == activeIndex
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .width(if (selected) 24.dp else 8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (selected) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                            )
                            .clickable { onDotClick(i) },
                    )
                }
            }
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
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
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
            // Date + link label (web parity)
            val date = announcement.date?.let { formatArabicDate(it) }
            if (date != null || announcement.linkLabel != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    date?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (date != null && announcement.linkLabel != null) {
                        Text(
                            text = " · ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    announcement.linkLabel?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnnouncementTypeBadge(type: String) {
    // Semantic tokens; only live-red is hardcoded (design lock permits it)
    val (label, color) = when (type) {
        "live" -> "مباشر" to Color(0xFFDC2626)
        "new" -> "جديد" to MaterialTheme.colorScheme.tertiary
        "event" -> "حدث" to MaterialTheme.colorScheme.secondary
        "upcoming" -> "قريباً" to MaterialTheme.colorScheme.primary
        else -> type to MaterialTheme.colorScheme.onSurfaceVariant
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
fun VideoRow(video: LectureItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = video.title,
                modifier = Modifier
                    .size(120.dp, 68.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatArabicDate(video.publishedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun VideoSkeletonRow() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
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

@Composable
fun QuickLinksRow(
    onInteractiveClick: () -> Unit,
    onContactClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        QuickLinkTile(
            title = "اختبر نفسك",
            description = "أسئلة تفاعلية في القرآن والسنة",
            onClick = onInteractiveClick,
            modifier = Modifier.weight(1f),
        )
        QuickLinkTile(
            title = "تواصل معنا",
            description = "روابطنا على جميع المنصات",
            onClick = onContactClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun QuickLinkTile(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}