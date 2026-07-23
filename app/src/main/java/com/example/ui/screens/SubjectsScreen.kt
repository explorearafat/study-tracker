package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subject
import com.example.ui.components.IconPickerRow
import com.example.ui.components.SubjectColorPickerRow
import com.example.ui.components.SubjectIcon
import com.example.ui.theme.SubjectColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(
    subjects: List<Subject>,
    onAddSubject: (name: String, category: String, colorHex: Long, targetMinutes: Int, iconName: String) -> Unit,
    onUpdateSubject: (Subject) -> Unit,
    onDeleteSubject: (Subject) -> Unit,
    onLogManualTime: (subjectId: Int, durationMinutes: Int, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var subjectToEdit by remember { mutableStateOf<Subject?>(null) }
    var subjectToLogTime by remember { mutableStateOf<Subject?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_subject_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Subject")
            }
        },
        modifier = modifier.testTag("subjects_screen")
    ) { innerPadding ->
        if (subjects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LibraryBooks,
                                contentDescription = "No Subjects",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Subjects Added Yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the + button below to create your first study subject.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + 80.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Column {
                        Text(
                            text = "ACADEMIC CURRICULUM",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Enrolled Subjects (${subjects.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                items(subjects) { subject ->
                    SubjectCard(
                        subject = subject,
                        onEdit = { subjectToEdit = subject },
                        onDelete = { onDeleteSubject(subject) },
                        onLogTime = { subjectToLogTime = subject }
                    )
                }
            }
        }
    }

    // Add Subject Dialog
    if (showAddDialog) {
        SubjectDialog(
            title = "Add New Subject",
            initialName = "",
            initialCategory = "STEM",
            initialColorHex = SubjectColors.first(),
            initialTargetMins = 60,
            initialIconName = "Book",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, category, colorHex, targetMins, iconName ->
                onAddSubject(name, category, colorHex, targetMins, iconName)
                showAddDialog = false
            }
        )
    }

    // Edit Subject Dialog
    subjectToEdit?.let { subject ->
        SubjectDialog(
            title = "Edit Subject",
            initialName = subject.name,
            initialCategory = subject.category,
            initialColorHex = subject.colorHex,
            initialTargetMins = subject.targetDailyMinutes,
            initialIconName = subject.iconName,
            onDismiss = { subjectToEdit = null },
            onConfirm = { name, category, colorHex, targetMins, iconName ->
                onUpdateSubject(
                    subject.copy(
                        name = name,
                        category = category,
                        colorHex = colorHex,
                        targetDailyMinutes = targetMins,
                        iconName = iconName
                    )
                )
                subjectToEdit = null
            }
        )
    }

    // Quick Manual Time Logging Dialog
    subjectToLogTime?.let { subject ->
        LogTimeDialog(
            subjectName = subject.name,
            onDismiss = { subjectToLogTime = null },
            onConfirm = { minutes, notes ->
                onLogManualTime(subject.id, minutes, notes)
                subjectToLogTime = null
            }
        )
    }
}

@Composable
fun SubjectCard(
    subject: Subject,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onLogTime: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = Color(subject.colorHex)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = color.copy(alpha = 0.15f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            SubjectIcon(
                                iconName = subject.iconName,
                                tint = color,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = subject.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${subject.category} • Target: ${subject.targetDailyMinutes}m/day",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(onClick = onLogTime) {
                        Icon(
                            imageVector = Icons.Default.AddAlarm,
                            contentDescription = "Log Time",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SubjectDialog(
    title: String,
    initialName: String,
    initialCategory: String,
    initialColorHex: Long,
    initialTargetMins: Int,
    initialIconName: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, colorHex: Long, targetMins: Int, iconName: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var category by remember { mutableStateOf(initialCategory) }
    var colorHex by remember { mutableStateOf(initialColorHex) }
    var targetMinsText by remember { mutableStateOf(initialTargetMins.toString()) }
    var iconName by remember { mutableStateOf(initialIconName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. STEM, Languages)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetMinsText,
                    onValueChange = { targetMinsText = it.filter { c -> c.isDigit() } },
                    label = { Text("Daily Study Target (Minutes)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                SubjectColorPickerRow(
                    selectedColorHex = colorHex,
                    onColorSelected = { colorHex = it }
                )

                IconPickerRow(
                    selectedIconName = iconName,
                    onIconSelected = { iconName = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val mins = targetMinsText.toIntOrNull() ?: 60
                        onConfirm(name, category, colorHex, mins, iconName)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LogTimeDialog(
    subjectName: String,
    onDismiss: () -> Unit,
    onConfirm: (minutes: Int, notes: String) -> Unit
) {
    var minutesText by remember { mutableStateOf("30") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Log Study Time: $subjectName", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = minutesText,
                    onValueChange = { minutesText = it.filter { c -> c.isDigit() } },
                    label = { Text("Minutes Studied") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Session Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val mins = minutesText.toIntOrNull() ?: 0
                    if (mins > 0) {
                        onConfirm(mins, notes)
                    }
                }
            ) {
                Text("Record Session")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
