package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

import com.example.data.Subject
import com.example.data.Task
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(viewModel: MainViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    var selectedSubjectFilterId by remember { mutableStateOf<String?>(null) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val filteredTasks = remember(tasks, selectedSubjectFilterId) {
        if (selectedSubjectFilterId == null) {
            tasks
        } else {
            tasks.filter { it.subjectId == selectedSubjectFilterId }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Task Checklist",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Organized with subject-assigned colors",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddTaskDialog = true }) {
                        Icon(Icons.Default.AddTask, contentDescription = "Add Task", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Subject Filter Bar with assigned subject colors!
            if (subjects.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedSubjectFilterId == null,
                            onClick = { selectedSubjectFilterId = null },
                            label = { Text("All Subjects (${tasks.size})") }
                        )
                    }
                    items(subjects, key = { it.id }) { subject ->
                        val isSelected = selectedSubjectFilterId == subject.id
                        val subjectColor = subject.toColor()

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedSubjectFilterId = if (isSelected) null else subject.id
                            },
                            label = {
                                Text(
                                    text = subject.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(subjectColor)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = subjectColor,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "No tasks found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(onClick = { showAddTaskDialog = true }) {
                            Text("Create Task")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 88.dp, top = 8.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskItemRow(
                            task = task,
                            onToggleComplete = { viewModel.toggleTaskCompleted(task) },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            subjects = subjects,
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { title, selectedSub, dueDate, estMins ->
                viewModel.addTask(title, selectedSub, dueDate, estMins)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun TaskItemRow(
    task: Task,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val subjectColor = task.toSubjectColor()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (task.isCompleted) subjectColor.copy(alpha = 0.3f) else subjectColor.copy(alpha = 0.8f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Subject Color Left Accent Line / Pill
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(subjectColor)
            )

            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleComplete() },
                colors = CheckboxDefaults.colors(
                    checkedColor = subjectColor,
                    uncheckedColor = subjectColor
                )
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Subject Name Tag in Subject Color
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = subjectColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = task.subjectName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = subjectColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "• ${task.dueDate} • ${task.estimatedMinutes}m",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete Task",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    subjects: List<Subject>,
    onDismiss: () -> Unit,
    onAddTask: (title: String, subject: Subject, dueDate: String, estMins: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()) }
    var dueDate by remember { mutableStateOf("Today") }
    var estMinsText by remember { mutableStateOf("30") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Study Task",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Select Subject:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(subjects) { sub ->
                        val isSel = selectedSubject?.id == sub.id
                        val subColor = sub.toColor()

                        FilterChip(
                            selected = isSel,
                            onClick = { selectedSubject = sub },
                            label = { Text(sub.name) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(subColor)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = subColor,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = estMinsText,
                        onValueChange = { estMinsText = it.filter { c -> c.isDigit() } },
                        label = { Text("Mins") },
                        modifier = Modifier.weight(0.8f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sub = selectedSubject
                    if (title.isNotBlank() && sub != null) {
                        onAddTask(title.trim(), sub, dueDate, estMinsText.toIntOrNull() ?: 30)
                    }
                },
                enabled = title.isNotBlank() && selectedSubject != null
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
