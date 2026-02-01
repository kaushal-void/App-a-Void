package com.example.app_a_void

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_a_void.ui.theme.ElectricCyan
import com.example.app_a_void.ui.theme.NeonBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoListScreen(
    tasks: List<Task>,                          // <-- A simple List<Task>
    onAddTask: (Task) -> Unit,                  // <-- Callback to add a task
    onDeleteTask: (Task) -> Unit,               // <-- Callback to delete a task
    onBackClick: () -> Unit,
    onUpdateTaskStatus: (Task, TaskStatus) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var editTask: Task? by remember { mutableStateOf(null) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var statusTask: Task? by remember { mutableStateOf(null) }

    // Background with gradient
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Black, NeonBlue)
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    title = {
                        Text(
                            text = "To-Do List",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = ElectricCyan
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Task", tint = Color.Black)
                }
            },
            content = { innerPadding ->
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.padding(innerPadding)
                ) {
                    items(tasks) { task ->
                        TaskCard(
                            task = task,
                            onEditClick = {
                                // Clicking the card triggers edit
                                editTask = task
                                showDialog = true
                            },
                            onDeleteClick = {
                                // Deletion handled via callback
                                onDeleteTask(task)
                            },
                            onUpdateStatusClick = {
                                statusTask = task
                                showStatusDialog = true
                            }
                        )
                    }
                }
            }
        )

        // Dialog for adding or editing a task
        if (showDialog || editTask != null) {
            AddTaskDialog(
                taskToEdit = editTask,
                onDismiss = {
                    showDialog = false
                    editTask = null
                },
                onAddTask = { newTask ->
                    // If editing, remove old version & add new
                    if (editTask != null) {
                        onDeleteTask(editTask!!)
                    }
                    onAddTask(newTask)
                    showDialog = false
                    editTask = null
                }
            )
        }

        // Dialog for picking a new status
        if (showStatusDialog && statusTask != null) {
            ToDoTaskStatusDialog(
                onDismiss = { showStatusDialog = false },
                onConfirm = { chosenStatus ->
                    onUpdateTaskStatus(statusTask!!, chosenStatus)
                    showStatusDialog = false
                }
            )
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onUpdateStatusClick: (TaskStatus) -> Unit
) {
    // Pick a background gradient based on the TaskStatus
    val backgroundBrush = when (task.status) {
        TaskStatus.COMPLETED -> Brush.horizontalGradient(
            colors = listOf(Color(0xFF4CAF50), Color(0xFF81C784)) // Greenish for completed
        )
        TaskStatus.SUSPENDED -> Brush.horizontalGradient(
            colors = listOf(Color(0xFF757575), Color(0xFFBDBDBD)) // Gray for suspended
        )
        TaskStatus.PENDING -> Brush.horizontalGradient(
            colors = listOf(ElectricCyan, NeonBlue, Color(0xFFC7EA46)) // Vibrant for pending
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundBrush)
            .clickable { onEditClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Task name + duration
            Text(
                text = "${task.name} - ${task.duration}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            // Delete icon
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Task",
                    tint = Color.White
                )
            }

            // Example icon that triggers status change
            IconButton(onClick = { onUpdateStatusClick(TaskStatus.SUSPENDED) }) {
                Icon(
                    imageVector = Icons.Filled.Add, // e.g. a plus icon
                    contentDescription = "Suspend Task",
                    tint = Color.Red
                )
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    taskToEdit: Task? = null,
    onDismiss: () -> Unit,
    onAddTask: (Task) -> Unit
) {
    var taskName by remember { mutableStateOf(taskToEdit?.name ?: "") }
    var taskDuration by remember { mutableStateOf(taskToEdit?.duration?.removeSuffix(" min") ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (taskToEdit == null) "Add Task" else "Edit Task",
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = { Text("Task Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = taskDuration,
                        onValueChange = { if (it.all { c -> c.isDigit() }) taskDuration = it },
                        label = { Text("Duration") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "min",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (taskName.isNotBlank() && taskDuration.isNotBlank()) {
                        // If no ID is provided, Room will auto-generate
                        val newTask = Task(
                            name = taskName,
                            duration = "$taskDuration min",
                            status = taskToEdit?.status ?: TaskStatus.PENDING
                        )
                        onAddTask(newTask)
                        onDismiss()
                    }
                }
            ) {
                Text(if (taskToEdit == null) "Add Task" else "Save Task")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = Color.DarkGray
    )
}

@Composable
fun ToDoTaskStatusDialog(
    onDismiss: () -> Unit,
    onConfirm: (TaskStatus) -> Unit
) {
    var selectedOption by remember { mutableStateOf<TaskStatus?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Task Status",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White
            )
        },
        text = {
            Column {
                // Let user pick from PENDING, SUSPENDED, or COMPLETED
                TaskStatus.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = option }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOption == option,
                            onClick = { selectedOption = option }
                        )
                        Text(
                            text = option.name,
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
                onClick = { selectedOption?.let { onConfirm(it) } },
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
        containerColor = Color.DarkGray
    )
}
