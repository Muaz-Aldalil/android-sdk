package com.subulalhuda.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Settings screen — theme toggle (v1 only setting).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("subul_prefs", Context.MODE_PRIVATE) }
    var isDark by remember { mutableStateOf(prefs.getBoolean("dark_theme", false)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات") },
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
                .padding(padding)
                .padding(16.dp),
        ) {
            ListItem(
                headlineContent = { Text("المظهر الداكن") },
                supportingContent = { Text(if (isDark) "مفعّل" else "معطّل") },
                trailingContent = {
                    Switch(
                        checked = isDark,
                        onCheckedChange = {
                            isDark = it
                            prefs.edit().putBoolean("dark_theme", it).apply()
                        },
                    )
                },
            )
            HorizontalDivider()

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "الإصدار 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}
