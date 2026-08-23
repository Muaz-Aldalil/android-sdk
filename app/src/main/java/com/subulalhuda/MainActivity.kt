package com.subulalhuda

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.subulalhuda.data.local.ContentRepository
import com.subulalhuda.data.repository.YouTubeRepository
import com.subulalhuda.ui.navigation.SubulNavGraph
import com.subulalhuda.ui.theme.SubulTheme

/**
 * Main entry point.
 * Wires up ContentRepository, YouTubeRepository, and navigation.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val contentRepository = remember { ContentRepository(context) }

            // TODO: Replace with actual API key from BuildConfig
            // Must be a SEPARATE key from the website's VITE_YOUTUBE_API_KEY
            val youtubeRepository = remember {
                val prefs = context.getSharedPreferences("subul_prefs", Context.MODE_PRIVATE)
                val apiKey = prefs.getString("youtube_api_key", "") ?: ""
                if (apiKey.isNotBlank()) {
                    YouTubeRepository(context, apiKey, "UCoj4ymRxoI4hVJPXdhOlgkw")
                } else {
                    null
                }
            }

            SubulTheme {
                SubulNavGraph(
                    contentRepository = contentRepository,
                    youtubeRepository = youtubeRepository,
                )
            }
        }
    }
}
