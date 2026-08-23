package com.subulalhuda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
            val appContext = context.applicationContext
            val contentRepository = remember { ContentRepository(appContext) }

            // YouTube API key from BuildConfig (set in local.properties via build.gradle.kts)
            // Must be a SEPARATE key from the website's VITE_YOUTUBE_API_KEY
            val youtubeRepository = remember {
                val apiKey = BuildConfig.YOUTUBE_API_KEY
                if (apiKey.isNotBlank()) {
                    YouTubeRepository(appContext, apiKey, "UCoj4ymRxoI4hVJPXdhOlgkw")
                } else {
                    null
                }
            }

            // Dark mode preference — single source of truth
            val prefs = remember { getSharedPreferences("subul_prefs", MODE_PRIVATE) }
            var isDark by remember {
                mutableStateOf(prefs.getBoolean("dark_theme", isSystemInDarkTheme()))
            }

            SubulTheme(darkTheme = isDark) {
                SubulNavGraph(
                    contentRepository = contentRepository,
                    youtubeRepository = youtubeRepository,
                    isDark = isDark,
                    onThemeChanged = { dark ->
                        isDark = dark
                        prefs.edit().putBoolean("dark_theme", dark).apply()
                    },
                )
            }
        }
    }
}
