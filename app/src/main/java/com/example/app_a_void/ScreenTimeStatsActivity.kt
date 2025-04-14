package com.example.app_a_void

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.app_a_void.ui.theme.AppAVoidTheme

class ScreenTimeStatsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppAVoidTheme {
                ScreenTimeStatsScreen(
                    onNavigateToSettings = { navigateToUsageAccessSettings() },
                    onNavigateBack = { finish() },
                    isPermissionGranted = { checkUsageStatsPermission() },
                    fetchUsageStats = { UsageStatsHelper.getTodayUsageStats(this) }
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun checkUsageStatsPermission(): Boolean {
        val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOpsManager.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            ) == AppOpsManager.MODE_ALLOWED
        } else {
            appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            ) == AppOpsManager.MODE_ALLOWED
        }
    }

    private fun navigateToUsageAccessSettings() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTimeStatsScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateBack: () -> Unit,
    isPermissionGranted: () -> Boolean,
    fetchUsageStats: () -> List<AppUsageStats>
) {
    val context = LocalContext.current
    val usageStats = remember { mutableStateOf<List<AppUsageStats>>(emptyList()) }
    var totalScreenTime by remember { mutableStateOf(0L) }

    if (isPermissionGranted()) {
        usageStats.value = fetchUsageStats()
        totalScreenTime = usageStats.value.sumOf { it.totalTimeInForeground }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Screen Time Stats", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            if (isPermissionGranted()) {
                UsageStatsContent(
                    usageStats = usageStats.value,
                    totalScreenTime = totalScreenTime,
                    modifier = Modifier.padding(padding)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Button(onClick = onNavigateToSettings) {
                        Text("Grant Permission")
                    }
                }
            }
        }
    )
}

@Composable
fun UsageStatsContent(
    usageStats: List<AppUsageStats>,
    totalScreenTime: Long,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TotalScreenTimeDisplay(totalScreenTime)

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn {
            items(usageStats) { appUsage ->
                AppUsageItemWithBar(appUsage, usageStats)
            }
        }
    }
}

@Composable
fun TotalScreenTimeDisplay(totalScreenTime: Long) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.Black)
            .drawBehind {
                drawRoundRect(
                    color = Color(0xFF00FFFF).copy(alpha = 0.5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                    style = Stroke(width = 4.dp.toPx()),
                    alpha = 0.8f
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = UsageStatsHelper.formatUsageTime(totalScreenTime),
            color = Color.White,
            fontSize = 36.sp
        )
    }
}

@Composable
fun AppUsageItemWithBar(appUsage: AppUsageStats, usageStats: List<AppUsageStats>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = appUsage.appIcon.toBitmap().asImageBitmap(),
            contentDescription = appUsage.appName,
            modifier = Modifier.size(40.dp).clip(shape = CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = appUsage.appName,
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text = appUsage.formattedTime,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        AnimatedBarGraph(
            progress = (appUsage.totalTimeInForeground.toFloat() / (usageStats.maxOfOrNull { it.totalTimeInForeground }?.toFloat() ?: 1f)),
            color = getNeonColor(usageStats.indexOf(appUsage))
        )
    }
}

@Composable
fun AnimatedBarGraph(progress: Float, color: Color) {
    val animatedProgress = animateFloatAsState(targetValue = progress, animationSpec = tween(durationMillis = 500)).value

    Canvas(
        modifier = Modifier
            .width(100.dp)
            .height(8.dp)
    ) {
        drawRect(
            color = color.copy(alpha = 0.5f),
            size = size
        )
        drawRect(
            color = color,
            size = androidx.compose.ui.geometry.Size(size.width * animatedProgress, size.height)
        )
    }
}

@Composable
fun getNeonColor(index: Int): Color {
    val neonColors = listOf(
        Color(0xFF00FF00), Color(0xFFFF00FF), Color(0xFF00FFFF),
        Color(0xFFFFFF00), Color(0xFFFF0000), Color(0xFF0000BB),
        Color(0xFFFFA500), Color(0xFF800080), Color(0xFF00FF7F),
        Color(0xFFFF1493)
    )
    return neonColors[index % neonColors.size]
}
