package com.subulalhuda.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * More screen — navigation options for Contact, Settings, About, Search.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onContactClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onSearchClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "المزيد",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(16.dp),
        )

        ListItem(
            headlineContent = { Text("البحث") },
            leadingContent = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onSearchClick),
        )
        HorizontalDivider()

        ListItem(
            headlineContent = { Text("التواصل معنا") },
            leadingContent = { Icon(Icons.Default.Phone, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onContactClick),
        )
        HorizontalDivider()

        ListItem(
            headlineContent = { Text("الإعدادات") },
            leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onSettingsClick),
        )
        HorizontalDivider()

        ListItem(
            headlineContent = { Text("عن التطبيق") },
            leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onAboutClick),
        )
        HorizontalDivider()
    }
}
