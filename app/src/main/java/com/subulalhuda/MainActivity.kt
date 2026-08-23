package com.subulalhuda

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.subulalhuda.ui.screens.YouTubePocActivity
import com.subulalhuda.ui.theme.SubulTheme

/**
 * Main entry point. Currently shows POC launcher.
 * Will be replaced with full navigation in Phase 8.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SubulTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PocLauncherScreen()
                }
            }
        }
    }
}

@Composable
fun PocLauncherScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "سُبُل الهُدى",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "YouTube Proof of Concept",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val intent = Intent(context, YouTubePocActivity::class.java).apply {
                    // TODO: Replace with actual API key from BuildConfig
                    putExtra(YouTubePocActivity.EXTRA_API_KEY, "YOUR_API_KEY_HERE")
                    putExtra(YouTubePocActivity.EXTRA_CHANNEL_ID, "UCoj4ymRxoI4hVJPXdhOlgkw")
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Open YouTube POC")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Set your separate YouTube API key before testing.\n" +
                "Do NOT use the website's API key.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
