package com.subulalhuda.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.subulalhuda.data.local.ContentRepository

/**
 * Contact screen — phone, location, social media links.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    contentRepository: ContentRepository,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val socialData = contentRepository.socialLinksData

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("التواصل معنا") },
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
            socialData?.let { data ->
                // Contact info
                item {
                    Text(
                        text = "معلومات التواصل",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
                item {
                    ListItem(
                        headlineContent = { Text("الهاتف") },
                        supportingContent = { Text(data.contactInfo.phone) },
                        modifier = Modifier.clickable {
                            try {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${data.contactInfo.phone}")))
                            } catch (_: android.content.ActivityNotFoundException) { }
                        },
                    )
                }
                item {
                    ListItem(
                        headlineContent = { Text("الموقع") },
                        supportingContent = { Text(data.contactInfo.location) },
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
                item {
                    Text(
                        text = "تابعنا على",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }

                // Social links
                val links = listOf(
                    "يوتيوب" to data.contactInfo.youtube,
                    "فيسبوك" to data.contactInfo.facebook,
                    "تيك توك" to data.contactInfo.tiktok,
                    "تيليجرام" to data.contactInfo.telegram,
                )

                items(links) { (label, url) ->
                    ListItem(
                        headlineContent = { Text(label) },
                        modifier = Modifier.clickable {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            } catch (_: android.content.ActivityNotFoundException) { }
                        },
                    )
                }
            }
        }
    }
}
