package com.example.app_a_void

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

@Composable
fun TimeWarpSettingsScreen(
    timeWarpViewModel: TimeWarpViewModel,
    onBackClick: () -> Unit
) {
    // Observe saved limits from the ViewModel.
    val dbLimits by timeWarpViewModel.limits.collectAsState()

    val context = LocalContext.current
    val packageManager = context.packageManager

    // Query all installed apps that have a launcher entry.
    val installedApps = remember {
        val launchIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        packageManager.queryIntentActivities(launchIntent, PackageManager.MATCH_ALL).map { resolveInfo ->
            val drawable: Drawable = resolveInfo.loadIcon(packageManager)
            AppUsageStats(
                packageName = resolveInfo.activityInfo.packageName,
                appName = resolveInfo.loadLabel(packageManager).toString(),
                formattedTime = "0 min",
                appIcon = drawable,
                totalTimeInForeground = 0L
            )
        }
    }

    // State for the search query input.
    var searchQuery by remember { mutableStateOf("") }
    val filteredApps = installedApps.filter {
        it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBackClick) {
                Text(text = "Back")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "Time Warp Settings", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search App") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredApps) { appUsage ->
                val usageFlow = remember(appUsage.packageName) {
                    timeWarpViewModel.getUsageFlow(appUsage.packageName)
                }
                val usageData by usageFlow.collectAsState(initial = null)

                val realtimeUsageText = usageData?.totalUsage?.let {
                    AppUtils.formatUsageTime(it) // e.g., "1h 23m", "15m", etc.
                } ?: appUsage.formattedTime

                TimeWarpAppItem(
                    appUsage = appUsage.copy(formattedTime = realtimeUsageText),
                    timeWarpViewModel = timeWarpViewModel,
                    existingLimit = dbLimits.firstOrNull { it.packageName == appUsage.packageName }
                )
            }
        }
    }
}

@Composable
fun TimeWarpAppItem(
    appUsage: AppUsageStats,
    timeWarpViewModel: TimeWarpViewModel,
    existingLimit: TimeWarpLimit?
) {
    var limitInput by remember { mutableStateOf("") }
    val currentLimitText = existingLimit?.limit ?: "None"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val iconPainter = remember(appUsage.appIcon) {
                BitmapPainter(appUsage.appIcon.toBitmap().asImageBitmap())
            }
            Image(
                painter = iconPainter,
                contentDescription = appUsage.appName,
                modifier = Modifier
                    .height(40.dp)
                    .padding(end = 8.dp)
            )
            Text(
                text = "${appUsage.appName} - ${appUsage.formattedTime}",
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = limitInput,
            onValueChange = { limitInput = it },
            label = { Text("Limit (min)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    val minutes = limitInput.toLongOrNull()
                    if (minutes != null && minutes > 0) {
                        val limitToSave = TimeWarpLimit(
                            packageName = appUsage.packageName,
                            appName = appUsage.appName,
                            limit = "$minutes min"
                        )
                        timeWarpViewModel.saveLimit(limitToSave)
                        limitInput = ""
                    } else {
                        Log.w("TimeWarpSettings", "Invalid limit input: $limitInput. Must be a positive number.")
                    }
                }
            ) {
                Text("Save Limit")
            }
            Text(
                text = "Current Limit: $currentLimitText",
                color = Color.White
            )
        }
    }
}
