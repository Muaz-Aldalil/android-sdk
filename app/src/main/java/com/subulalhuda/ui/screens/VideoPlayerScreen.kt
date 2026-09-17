package com.subulalhuda.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Video player screen — plays a YouTube video via the embed iframe in a WebView.
 * Falls back to the external YouTube app on load failure.
 *
 * The previous implementation used android-youtube-player, whose underlying
 * YouTube Android Player API was shut down by Google. A WebView iframe embed
 * needs no extra dependencies and keeps the external-app fallback.
 *
 * @param videoId YouTube video ID
 * @param title Optional lecture title (wired by the data layer; falls back to a generic label)
 * @param onBack Navigation callback
 */
@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    videoId: String,
    onBack: () -> Unit,
    title: String? = null,
) {
    val context = LocalContext.current
    var playerError by remember { mutableStateOf(false) }
    var pageLoaded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title ?: "الدرس") },
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
                .padding(padding),
        ) {
            if (playerError) {
                // Fallback: open in the external YouTube app
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clickable {
                            try {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://www.youtube.com/watch?v=$videoId"),
                                    ),
                                )
                            } catch (_: android.content.ActivityNotFoundException) { }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "اضغط للفتح في يوتيوب",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                ) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.mediaPlaybackRequiresUserGesture = false
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView, url: String?) {
                                        pageLoaded = true
                                    }

                                    override fun onReceivedError(
                                        view: WebView,
                                        request: WebResourceRequest,
                                        error: WebResourceError,
                                    ) {
                                        if (request.isForMainFrame) playerError = true
                                    }
                                }
                                loadUrl("https://www.youtube.com/embed/$videoId?autoplay=1")
                            }
                        },
                        onRelease = { view -> view.destroy() },
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (!pageLoaded) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
            }

            // Secondary affordance — embeds can fail silently (age-restricted, region-locked)
            TextButton(
                onClick = {
                    try {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.youtube.com/watch?v=$videoId"),
                            ),
                        )
                    } catch (_: android.content.ActivityNotFoundException) { }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text("افتح في تطبيق يوتيوب")
            }
        }
    }
}