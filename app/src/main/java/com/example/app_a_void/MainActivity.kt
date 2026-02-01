package com.example.app_a_void

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app_a_void.ui.theme.AppAVoidTheme
import com.example.app_a_void.ui.theme.BrightRed
import com.example.app_a_void.ui.theme.CharcoalBlack
import com.example.app_a_void.ui.theme.ElectricCyan
import com.example.app_a_void.ui.theme.LimeGreen
import com.example.app_a_void.ui.theme.NavyBlue
import com.example.app_a_void.ui.theme.NeonBlue
import com.example.app_a_void.ui.theme.Orange
import com.example.app_a_void.ui.theme.TextLightGrey
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Math.toRadians
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Check and prompt overlay permission before setContent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            Toast.makeText(this, "Please allow 'Display over other apps' permission", Toast.LENGTH_LONG).show()
            startActivity(intent)
        }

        setContent {
            AppAVoidTheme {
                Surface(
                    modifier = Modifier,
                    color = CharcoalBlack
                ) {
                    val taskViewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(
                        AppDatabase.getDatabase(LocalContext.current).taskDao()
                    ))
                    val timeWarpViewModel: TimeWarpViewModel = viewModel()

                    MainUI(
                        taskViewModel = taskViewModel,
                        timeWarpViewModel = timeWarpViewModel
                    )
                }
            }
        }
    }
}




@Composable
fun MainUI(taskViewModel: TaskViewModel, timeWarpViewModel: TimeWarpViewModel) {
    // TIMER-RELATED STATE
    var timeLeft by remember { mutableStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var triggerExplosion by remember { mutableStateOf(false) }

    // SCREEN NAVIGATION / MENU STATES
    var showMenu by remember { mutableStateOf(false) }
    var showToDoList by remember { mutableStateOf(false) }
    var showTimeWarpSettings by remember { mutableStateOf(false) }

    // TASKS FROM VIEWMODEL
    val tasks by taskViewModel.tasks.collectAsState(initial = emptyList())
    // Track the currently selected Task.
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    // For showing the TaskStatus dialog.
    var showTaskStatusDialog by remember { mutableStateOf(false) }
    var statusTask by remember { mutableStateOf<Task?>(null) }

    // COUNTDOWN LOGIC
    LaunchedEffect(isRunning) {
        if (isRunning) {
            triggerExplosion = false
            while (timeLeft > 0) {
                kotlinx.coroutines.delay(1000L)
                timeLeft--
            }
            // When timeLeft reaches 0, stop the timer and display the dialog.
            isRunning = false
            triggerExplosion = true
            statusTask = selectedTask
            showTaskStatusDialog = true
        }
    }

    // SCREEN NAVIGATION / SELECTION
    when {
        // 1) To-Do List Screen
        showToDoList -> {
            BackHandler(enabled = true) {
                showToDoList = false
            }
            ToDoListScreen(
                tasks = tasks,
                onAddTask = { newTask -> taskViewModel.addTask(newTask) },
                onDeleteTask = { task -> taskViewModel.deleteTask(task) },
                onBackClick = { showToDoList = false },
                onUpdateTaskStatus = { task, newStatus ->
                    taskViewModel.updateTask(task.copy(status = newStatus))
                }
            )
        }
        // 2) Time Warp Settings Screen
        showTimeWarpSettings -> {
            TimeWarpSettingsScreen(
                timeWarpViewModel = timeWarpViewModel,
                onBackClick = { showTimeWarpSettings = false }
            )
        }
        // 3) Main UI
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Icon (top-left)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    ProfileIcon(onClick = { showMenu = true })
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Circular Timer + Explosion
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val initialTime = 25 * 60
                    CircularTimeIndicatorWithParticles(
                        timerDurationSeconds = selectedTask
                            ?.duration
                            ?.removeSuffix(" min")
                            ?.toIntOrNull()
                            ?.times(60)
                            ?: initialTime,
                        timeLeft = timeLeft,
                        isRunning = isRunning,
                        triggerExplosion = triggerExplosion
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Currently Selected Task Text
                Text(
                    text = selectedTask?.name ?: "Select Your Task",
                    fontSize = 16.sp,
                    modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Horizontal list of tasks from ViewModel
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    items(tasks) { task ->
                        GlowingButtonWithParticles(
                            label = task.name,
                            colorStart = NeonBlue,
                            colorEnd = NavyBlue,
                            onClick = {
                                selectedTask = task
                                timeLeft = task.duration
                                    .removeSuffix(" min")
                                    .toIntOrNull()
                                    ?.times(60)
                                    ?: (25 * 60)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Progress Bars (Focus, Cooldown, Pause)
                val focusProgress = timeLeft.toFloat() /
                        (selectedTask?.duration
                            ?.removeSuffix(" min")
                            ?.toIntOrNull()
                            ?.times(60)
                            ?: (25 * 60))
                ProgressBarWithLabel(
                    label = "Focus",
                    progress = focusProgress,
                    progressColor = NeonBlue
                )
                ProgressBarWithLabel(
                    label = "Cooldown",
                    progress = 0.5f,
                    progressColor = LimeGreen
                )
                ProgressBarWithLabel(
                    label = "Pause",
                    progress = 0.25f,
                    progressColor = Orange
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Start & Reset Buttons
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                ) {
                    GlowingButtonWithParticles(
                        label = "Start",
                        onClick = {
                            isRunning = true
                            triggerExplosion = false
                        },
                        colorStart = NeonBlue,
                        colorEnd = NavyBlue
                    )
                    GlowingButtonWithParticles(
                        label = "Reset",
                        onClick = {
                            isRunning = false
                            timeLeft = selectedTask
                                ?.duration
                                ?.removeSuffix(" min")
                                ?.toIntOrNull()
                                ?.times(60)
                                ?: (25 * 60)
                            triggerExplosion = false
                        },
                        colorStart = BrightRed,
                        colorEnd = Orange
                    )
                }

                // Show the menu if profile icon tapped.
                if (showMenu) {
                    ControlNexusWithAvatar(
                        onDismiss = { showMenu = false },
                        onToDoListClick = {
                            showMenu = false
                            showToDoList = true
                        },
                        onTimeWarpClick = {
                            showMenu = false
                            showTimeWarpSettings = true
                        }
                    )
                }

                // Task Status Dialog
                ShowTaskStatusDialog(
                    showTaskStatusDialog = showTaskStatusDialog,
                    statusTask = statusTask,
                    onDismiss = { showTaskStatusDialog = false },
                    onStatusChange = { newStatus ->
                        statusTask?.let { oldTask ->
                            taskViewModel.updateTask(oldTask.copy(status = newStatus))
                        }
                        showTaskStatusDialog = false
                    }
                )
            }
        }
    }
}




/**
 * Updated ShowTaskStatusDialog that does NOT directly mutate 'tasks'.
 * Instead, it calls 'onStatusChange' to update in the DB.
 */
@Composable
fun ShowTaskStatusDialog(
    showTaskStatusDialog: Boolean,
    statusTask: Task?,
    onDismiss: () -> Unit,
    onStatusChange: (TaskStatus) -> Unit
) {
    if (showTaskStatusDialog && statusTask != null) {
        TaskStatusDialog(
            options = TaskStatus.entries.toList(),
            title = "Update Task Status",
            onDismiss = { onDismiss() },
            onConfirm = { chosenStatus ->
                onStatusChange(chosenStatus)
                onDismiss()
            }
        )
    }
}










@Composable
fun ControlNexusWithAvatar(
    onDismiss: () -> Unit,
    onToDoListClick: () -> Unit,
    onTimeWarpClick: () -> Unit
) {
    val context = LocalContext.current

    // Define only the 6 items you want to show
    val menuItems = listOf(
        MenuItem(R.drawable.screentimestats, "Screen Time Stats"),
        MenuItem(R.drawable.todolist, "To-Do List"),
        MenuItem(R.drawable.cooldowntimer, "Cooldown Timer"),
        MenuItem(R.drawable.logs, "Time Warp"),
        MenuItem(R.drawable.timetravel, "Time Travel"),
        MenuItem(R.drawable.about, "About")
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)) // Dimmed background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
                    .shadow(16.dp, CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF001F3F).copy(alpha = 0.8f),
                                Color(0xFF00BCD4).copy(alpha = 0.9f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar icon at the top
                Image(
                    painter = painterResource(id = R.drawable.avatar_placeholder),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Control Nexus",
                    color = NeonBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Lazy grid of the 6 items
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(menuItems.size) { index ->
                        val item = menuItems[index]
                        MenuItemComposable(menuItem = item) {
                            // Handle each item’s click
                            when (item.title) {
                                "Screen Time Stats" -> {
                                    context.startActivity(
                                        Intent(context, ScreenTimeStatsActivity::class.java)
                                    )
                                }
                                "To-Do List" -> onToDoListClick()
                                "Time Warp" -> onTimeWarpClick()
                                else -> println("Clicked: ${item.title}")
                            }
                        }
                    }
                }
            }
        }
    }
}






@Composable
fun ProfileIcon(onClick: () -> Unit) {
    Image(
        painter = painterResource(id = R.drawable.avatar_placeholder),
        contentDescription = "Profile Icon",
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .clickable { onClick() }
    )
}


@Composable
fun DigitalAvatarSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.avatar_placeholder),
            contentDescription = "Digital Avatar",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonBlue.copy(alpha = 0.6f), Color.Transparent)
                    )
                )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Control Nexus",
            color = NeonBlue,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
fun ControlNexus(onDismiss: () -> Unit) {
    // 1) Create a list of only the items you want to keep.
    val menuItems = listOf(
        MenuItem(R.drawable.screentimestats, "Screen Time Stats"),
        MenuItem(R.drawable.todolist, "To-Do List"),
        MenuItem(R.drawable.cooldowntimer, "Cooldown Timer"),
        MenuItem(R.drawable.logs, "Logs"),
        MenuItem(R.drawable.timetravel, "Time Travel"),
        MenuItem(R.drawable.about, "About")
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
        ) {
            // 2) Use the new list's size in LazyVerticalGrid.
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.6f)
                    .align(Alignment.Center)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                NavyBlue.copy(alpha = 0.8f),
                                NeonBlue.copy(alpha = 0.9f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .padding(16.dp)
            ) {
                items(menuItems.size) { index ->
                    MenuItemComposable(menuItem = menuItems[index]) {
                        println("Clicked Item $index: ${menuItems[index].title}")
                    }
                }
            }
        }
    }
}




@Composable
fun MenuItemComposable(menuItem: MenuItem, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        NeonBlue.copy(alpha = 0.3f),
                        ElectricCyan.copy(alpha = 0.5f)
                    )
                )
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = menuItem.iconRes),
            contentDescription = menuItem.title,
            modifier = Modifier
                .size(64.dp)
                .background(Color.Transparent)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = formatMenuItemText(menuItem.title),
            color = TextLightGrey,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally),
            maxLines = 3,  // Allow text to break into three lines if needed
            overflow = TextOverflow.Ellipsis // Avoid overflow and show ellipses when needed
        )
    }
}

fun formatMenuItemText(title: String): String {
    return when (title) {
        "Cooldown Timer" -> "Cool\nDown\nTimer"  // Breaking it into 3 lines
        "Feedback" -> "Feed\nBack" // Breaking feedback into 2 lines
        else -> title
    }
}


data class MenuItem(val iconRes: Int, val title: String)


@Composable
fun CircularTimeIndicatorWithParticles(
    timerDurationSeconds: Int,
    timeLeft: Int,
    isRunning: Boolean,
    triggerExplosion: Boolean
) {
    val tickRotation = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(0.5f) }
    val progress = remember { Animatable(0f) }
    val density = LocalDensity.current
    val particles = remember { List(100) { Animatable(0f) } } // 100 particles for explosion
    val explosionColors = listOf(NeonBlue, ElectricCyan, BrightRed, LimeGreen, Orange)

    // Tick rotation animation
    LaunchedEffect(Unit) {
        tickRotation.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 12000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    // Glow pulsing animation
    LaunchedEffect(Unit) {
        glowAlpha.animateTo(
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    // Progress animation
    LaunchedEffect(isRunning) {
        if (isRunning) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = timerDurationSeconds * 1000)
            )
        } else {
            progress.snapTo(0f)
        }
    }

    // Explosion animation
    LaunchedEffect(triggerExplosion) {
        if (triggerExplosion) {
            particles.forEachIndexed { index, animatable ->
                val delay = index * 10L // Staggered delay for dynamic effect
                launch {
                    delay(delay)
                    animatable.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 800, easing = LinearEasing)
                    )
                }
            }
        } else {
            particles.forEach { it.snapTo(0f) } // Reset particles
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
    ) {
        // Particle explosion layer
        Canvas(modifier = Modifier.size(300.dp)) {
            val radius = size.minDimension / 2
            particles.forEach { animatable ->
                val randomAngle = Math.random() * 360
                val distance = animatable.value * radius * 2 // Expand outward dynamically
                val x = (distance * cos(toRadians(randomAngle))).toFloat()
                val y = (distance * sin(toRadians(randomAngle))).toFloat()

                drawCircle(
                    color = explosionColors.random()
                        .copy(alpha = 1f - animatable.value), // Random color fades out
                    radius = 8.dp.toPx() * (1f - animatable.value), // Shrinks as it fades
                    center = center + Offset(x, y)
                )
            }
        }

        // Outer glow and ticks
        Canvas(modifier = Modifier.size(250.dp)) {
            val radius = size.minDimension / 2

            // Outer glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonBlue.copy(alpha = glowAlpha.value),
                        ElectricCyan.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.5f
                ),
                radius = radius * 1.5f,
                center = center
            )

            // Rotating ticks
            val tickCount = 12
            val rotationAngle = tickRotation.value
            val tickLength = with(density) { 12.dp.toPx() }

            for (i in 0 until tickCount) {
                val angle = toRadians(360.0 / tickCount * i + rotationAngle - 90)
                drawLine(
                    color = ElectricCyan,
                    start = center + Offset(
                        x = (radius - 20.dp.toPx()) * cos(angle).toFloat(),
                        y = (radius - 20.dp.toPx()) * sin(angle).toFloat()
                    ),
                    end = center + Offset(
                        x = (radius - 20.dp.toPx() - tickLength) * cos(angle).toFloat(),
                        y = (radius - 20.dp.toPx() - tickLength) * sin(angle).toFloat()
                    ),
                    strokeWidth = 3f
                )
            }
        }

        // Gradient progress bar
        Canvas(modifier = Modifier.size(300.dp)) {
            val radius = size.minDimension / 2
            val strokeWidth = with(density) { 20.dp.toPx() }

            drawArc(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF003366), // Darker blue
                        Color(0xFF00FFFF)  // Vibrant blue
                    )
                ),
                startAngle = -90f,
                sweepAngle = 360 * progress.value,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(
                    (size.width - 2 * radius) / 2f,
                    (size.height - 2 * radius) / 2f
                ),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
        }

        // Timer text
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(CharcoalBlack)
                .shadow(8.dp, CircleShape)
        ) {
            Text(
                text = formatTime(timeLeft.toLong()),
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(minutes, secs)
}


@Composable
fun ProgressBarWithLabel(label: String, progress: Float, progressColor: Color) {
    Column(
        modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = TextLightGrey,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                color = TextLightGrey,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(CircleShape)
                .background(Color.DarkGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                progressColor.copy(alpha = 0.7f),
                                progressColor
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun GlowingButton(label: String, colorStart: Color, colorEnd: Color) {
    val density = LocalDensity.current
    val glowRadius = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val initialRadius = with(density) { 16.dp.toPx() }
        val maxRadius = with(density) { 24.dp.toPx() }

        glowRadius.snapTo(initialRadius)

        glowRadius.animateTo(
            targetValue = maxRadius,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Box(
        modifier = Modifier
            .size(90.dp)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(colorStart, colorEnd)
                )
            )
            .shadow(20.dp, CircleShape)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = colorStart.copy(alpha = 0.5f),
                radius = size.minDimension / 2 + glowRadius.value,
                center = center
            )
        }
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun TimerProgressBar(timerDurationSeconds: Int, timeLeft: Int) {
    val progress = timeLeft.toFloat() / timerDurationSeconds

    // Pulsing glow animation
    val glowAlpha = remember { Animatable(0.3f) }
    LaunchedEffect(Unit) {
        glowAlpha.animateTo(
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .clip(CircleShape)
            .background(Color.DarkGray) // Background of the progress bar
            .padding(horizontal = 16.dp)
    ) {
        // Progress bar with nebula effect
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF001F3F), // Darker blue
                            Color(0xFF0074D9), // Vibrant blue
                            Color(0xFF00FFFF)  // Cyan touch for nebula effect
                        )
                    )
                )
        )

        // Subtle glowing effect
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFF00FFFF).copy(alpha = glowAlpha.value), // Pulsing glow
                center = Offset(size.width * progress, size.height / 2),
                radius = size.height / 1.5f
            )
        }
    }
}

@Composable
fun GlowingButtonWithParticles(
    label: String,
    onClick: () -> Unit,
    colorStart: Color,
    colorEnd: Color
) {
    var isPressed by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val glowRadius =
        remember { Animatable(if (isPressed) with(density) { 16.dp.toPx() } else with(density) { 24.dp.toPx() }) }

    LaunchedEffect(isPressed) {
        glowRadius.animateTo(
            targetValue = if (isPressed) with(density) { 14.dp.toPx() } else with(density) { 28.dp.toPx() },
            animationSpec = tween(durationMillis = 400, easing = LinearEasing)
        )
    }

    Box(
        modifier = Modifier
            .size(100.dp) // Adjust button size as needed
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(colorStart, colorEnd)
                )
            )
            .shadow(12.dp, CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Glowing Effect
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = colorStart.copy(alpha = 0.5f),
                radius = glowRadius.value,
                center = center
            )
        }

        // Particle Effect (Optional for Enhancement)
        Canvas(modifier = Modifier.matchParentSize()) {
            val particleCount = 6
            val angleStep = 360f / particleCount

            for (i in 0 until particleCount) {
                val angle = toRadians(angleStep * i.toDouble())
                val x = center.x + glowRadius.value * 0.7f * cos(angle).toFloat()
                val y = center.y + glowRadius.value * 0.7f * sin(angle).toFloat()

                drawCircle(
                    color = colorEnd.copy(alpha = 0.8f),
                    radius = 3.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }

        // Button Label
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}


@Composable
fun MenuDialog(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)) // Semi-transparent background
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color.White, shape = CircleShape) // Menu container
                .padding(16.dp)
        ) {
            Text(
                text = "Menu Placeholder",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Close Menu",
                color = Color.Blue,
                modifier = Modifier.clickable { onDismiss() }
            )
        }
    }
}


@Composable
fun <T> TaskStatusDialog(
    options: List<T>, // Pass options dynamically (can be TaskStatus.entries or a list of strings)
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (T) -> Unit // Accepts a generic type for the selected option
) {
    var selectedOption by remember { mutableStateOf<T?>(null) } // Generic type state

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White
            )
        },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = option }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = selectedOption == option,
                            onClick = { selectedOption = option }
                        )
                        Text(
                            text = when (option) {
                                is TaskStatus -> option.name.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                                }

                                is String -> option // No transformation for String
                                else -> option.toString() // Fallback for other types
                            },
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedOption?.let(onConfirm) },
                enabled = selectedOption != null
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        },

        containerColor = Color.DarkGray // Optional dialog background
    )
}


@Composable
fun ShowTaskStatusDialog(
    showTaskStatusDialog: Boolean,
    statusTask: Task?,
    tasks: SnapshotStateList<Task>,
    onDismiss: () -> Unit
) {
    if (showTaskStatusDialog && statusTask != null) {
        TaskStatusDialog(
            options = TaskStatus.entries.toList(), // Pass the options as a list of TaskStatus
            title = "Update Task Status", // Provide the title
            onDismiss = { onDismiss() },
            onConfirm = { status: TaskStatus -> // Explicit type declaration
                statusTask.let { task ->
                    val index = tasks.indexOf(task)
                    if (index != -1) {
                        tasks[index] = task.copy(status = status)
                    }
                }
                onDismiss()
            }
        )
    }
}



@Composable
fun ShowPermissionDialog(context: Context) {
    var isDialogVisible by remember { mutableStateOf(true) } // Controls dialog visibility

    // Show dialog only when needed
    if (isDialogVisible) {
        AlertDialog(
            onDismissRequest = {
                isDialogVisible = false  // Close the dialog when dismiss is tapped
            },
            title = {
                Text("Permission Required")
            },
            text = {
                Text("The app needs the Exact Alarm permission to function properly. Please grant the permission in the settings.")
            },
            confirmButton = {
                Button(onClick = {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    context.startActivity(intent)
                    Toast.makeText(context, "Please enable the permission", Toast.LENGTH_SHORT).show()
                    isDialogVisible = false // Close the dialog after action
                }) {
                    Text("Go to Settings")
                }
            },
            dismissButton = {
                Button(onClick = {
                    isDialogVisible = false // Close the dialog on dismiss
                }) {
                    Text("Dismiss")
                }
            }
        )
    }
}





