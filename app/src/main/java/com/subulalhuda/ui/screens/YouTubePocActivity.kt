package com.subulalhuda.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.subulalhuda.data.remote.PlaylistItem
import com.subulalhuda.data.remote.VideoItem
import com.subulalhuda.data.repository.LiveCheckResult
import com.subulalhuda.data.repository.YouTubeRepository
import com.subulalhuda.ui.theme.SubulTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * YouTube Proof of Concept screen.
 *
 * This screen validates:
 * 1. YouTube Data API v3 integration (recent uploads, video details)
 * 2. android-youtube-player library (normal video playback)
 * 3. Live status detection via videos.list(liveStreamingDetails)
 * 4. External YouTube app fallback
 *
 * POC VERIFICATION CHECKLIST:
 * [ ] Normal video plays
 * [ ] Live video plays (or fails gracefully)
 * [ ] Android 16 compatible (test on device)
 * [ ] Player initializes reliably
 * [ ] Fullscreen works
 * [ ] Pause/resume works
 * [ ] Back navigation works
 * [ ] Rotation/configuration behavior acceptable
 * [ ] Player error → useful fallback (external YouTube intent)
 * [ ] External YouTube intent works
 *
 * If any serious playback issue is found, STOP and report before proceeding.
 *
 * IMPORTANT: Requires a separate YouTube API key configured in BuildConfig.
 * Do NOT use the website's API key.
 */
@OptIn(ExperimentalMaterial3Api::class)
class YouTubePocActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Replace with actual API key from BuildConfig or secure storage
        // This MUST be a separate key from the website's VITE_YOUTUBE_API_KEY
        val apiKey = intent.getStringExtra(EXTRA_API_KEY) ?: ""
        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID) ?: "UCoj4ymRxoI4hVJPXdhOlgkw"

        val repository = YouTubeRepository(applicationContext, apiKey, channelId)

        setContent {
            SubulTheme {
                YouTubePocScreen(
                    repository = repository,
                    onBack = { finish() },
                    onOpenYouTube = { videoId ->
                        // Fallback: open in YouTube app
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.youtube.com/watch?v=$videoId")
                        )
                        startActivity(intent)
                    },
                )
            }
        }
    }

    companion object {
        const val EXTRA_API_KEY = "extra_api_key"
        const val EXTRA_CHANNEL_ID = "extra_channel_id"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouTubePocScreen(
    repository: YouTubeRepository,
    onBack: () -> Unit,
    onOpenYouTube: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var recentUploads by remember { mutableStateOf<List<PlaylistItem>>(emptyList()) }
    var liveResult by remember { mutableStateOf<LiveCheckResult?>(null) }
    var selectedVideoId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var testLog by remember { mutableStateOf(mutableListOf<String>()) }

    fun log(message: String) {
        testLog = testLog + message
    }

    // Load recent uploads on first composition
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            isLoading = true
            error = null
            try {
                log("Fetching uploads playlist ID...")
                val playlistId = repository.getUploadsPlaylistId()
                if (playlistId == null) {
                    error = "Failed to get uploads playlist ID"
                    log("ERROR: Could not get uploads playlist ID")
                    isLoading = false
                    return@launch
                }
                log("Uploads playlist ID: $playlistId")

                log("Fetching recent uploads...")
                val uploads = repository.getRecentUploads(5)
                recentUploads = uploads
                log("Found ${uploads.size} recent uploads")

                log("Checking live status (videos.list with liveStreamingDetails)...")
                val live = repository.checkLiveStatus(5)
                liveResult = live
                when (live) {
                    is LiveCheckResult.Live -> log("LIVE: ${live.title} (${live.viewers ?: 0} viewers)")
                    is LiveCheckResult.NotLive -> log("Not live (no active stream in recent uploads)")
                    is LiveCheckResult.NoVideos -> log("No recent uploads found")
                    is LiveCheckResult.Error -> log("Live check error: ${live.message}")
                }

                log("Quota usage estimate: ~3 units (1 playlistItems + 1 videos.list)")
            } catch (e: Exception) {
                error = e.message ?: "Unknown error"
                log("ERROR: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("YouTube POC") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch(Dispatchers.IO) {
                            isLoading = true
                            try {
                                val uploads = repository.getRecentUploads(5)
                                recentUploads = uploads
                                liveResult = repository.checkLiveStatus(5)
                            } catch (e: Exception) {
                                error = e.message
                            } finally {
                                isLoading = false
                            }
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Video Player Area
            if (selectedVideoId != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    YouTubePlayer(
                        videoId = selectedVideoId!!,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                        onOpenYouTube = onOpenYouTube,
                    )
                }
            }

            // Live Status Card
            liveResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (result) {
                            is LiveCheckResult.Live -> Color(0xFFDC2626).copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Live Status",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        when (result) {
                            is LiveCheckResult.Live -> {
                                Text(
                                    text = "🔴 LIVE: ${result.title}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFDC2626),
                                )
                                result.viewers?.let { viewers ->
                                    Text(
                                        text = "$viewers viewers",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                            is LiveCheckResult.NotLive -> {
                                Text(
                                    text = "No active live stream in recent uploads",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            is LiveCheckResult.NoVideos -> {
                                Text(
                                    text = "No recent uploads found",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            is LiveCheckResult.Error -> {
                                Text(
                                    text = "Error: ${result.message}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFDC2626),
                                )
                            }
                        }
                    }
                }
            }

            // Recent Uploads List
            Text(
                text = "Recent Uploads (${recentUploads.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            error?.let { errorMsg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFDC2626).copy(alpha = 0.1f)
                    ),
                ) {
                    Text(
                        text = errorMsg,
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFFDC2626),
                    )
                }
            }

            // Upload list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(recentUploads) { item ->
                    UploadItem(
                        item = item,
                        onClick = {
                            item.contentDetails?.videoId?.let { videoId ->
                                selectedVideoId = videoId
                                log("Selected video: $videoId")
                            }
                        },
                        onOpenYouTube = {
                            item.contentDetails?.videoId?.let { videoId ->
                                onOpenYouTube(videoId)
                            }
                        },
                    )
                }
            }

            // Test Log
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Test Log",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    testLog.takeLast(5).forEach { entry ->
                        Text(
                            text = entry,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun YouTubePlayer(
    videoId: String,
    modifier: Modifier = Modifier,
    onOpenYouTube: (String) -> Unit,
) {
    val context = LocalContext.current
    var playerError by remember { mutableStateOf<String?>(null) }

    if (playerError != null) {
        // Fallback: show error with option to open in YouTube app
        Box(
            modifier = modifier
                .background(Color(0xFF1a1a1a))
                .clickable { onOpenYouTube(videoId) },
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "افتح في يوتيوب",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = playerError ?: "",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    } else {
        // Primary: android-youtube-player
        AndroidView(
            factory = { ctx ->
                YouTubePlayerView(ctx).apply {
                    enableAutomaticInitialization = false
                    initialize(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            youTubePlayer.loadVideo(videoId, 0f)
                        }
                    })
                }
            },
            modifier = modifier.clip(RoundedCornerShape(8.dp)),
            onReset = { view ->
                // Player lifecycle managed by the view
            },
            onRelease = { view ->
                // Cleanup handled by AndroidView
            },
        )
    }
}

@Composable
fun UploadItem(
    item: PlaylistItem,
    onClick: () -> Unit,
    onOpenYouTube: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Thumbnail
            val thumbnailUrl = item.snippet?.thumbnails?.medium?.url
                ?: item.snippet?.thumbnails?.default?.url

            if (thumbnailUrl != null) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = item.snippet?.title,
                    modifier = Modifier
                        .size(120.dp, 68.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.LightGray),
                )
            }

            // Title and date
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.snippet?.title ?: "Untitled",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.contentDetails?.videoPublishedAt ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
