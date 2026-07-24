package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

import com.example.data.Subject
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(
    viewModel: MainViewModel,
    onNavigateToTimer: (Subject) -> Unit
) {
    val subjects by viewModel.subjects.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<Subject?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Subjects & Color Management",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Assign unique colors to personalize your checklist & charts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Subject", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Subject", tint = Color.White)
            }
        }
    ) { padding ->
        if (subjects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "No subjects added yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(onClick = { showAddDialog = true }) {
                        Text("Add Subject & Pick Color")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 88.dp, top = 8.dp)
            ) {
                items(subjects, key = { it.id }) { subject ->
                    SubjectCardItem(
                        subject = subject,
                        onEditColor = { editingSubject = subject },
                        onStartFocus = { onNavigateToTimer(subject) },
                        onDelete = { viewModel.deleteSubject(subject) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        SubjectEditDialog(
            subjectToEdit = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, colorHex, targetMins ->
                viewModel.addSubject(name, colorHex, targetMins)
                showAddDialog = false
            }
        )
    }

    editingSubject?.let { subject ->
        SubjectEditDialog(
            subjectToEdit = subject,
            onDismiss = { editingSubject = null },
            onSave = { name, colorHex, targetMins ->
                viewModel.updateSubject(subject.copy(name = name, colorHex = colorHex, targetMinutesPerWeek = targetMins))
                editingSubject = null
            }
        )
    }
}

@Composable
fun SubjectCardItem(
    subject: Subject,
    onEditColor: () -> Unit,
    onStartFocus: () -> Unit,
    onDelete: () -> Unit
) {
    val subjectColor = subject.toColor()

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(2.dp, subjectColor.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Subject Color Swatch & Icon
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(subjectColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Color tag chip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = subjectColor.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, subjectColor)
                    ) {
                        Text(
                            text = subject.colorHex.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = subjectColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Weekly Goal: ${subject.targetMinutesPerWeek / 60}h (${subject.targetMinutesPerWeek} mins)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick Actions
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEditColor) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Change Subject Color",
                        tint = subjectColor
                    )
                }

                IconButton(onClick = onStartFocus) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Start Focus Session",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Subject",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun SubjectEditDialog(
    subjectToEdit: Subject?,
    onDismiss: () -> Unit,
    onSave: (name: String, colorHex: String, targetMins: Int) -> Unit
) {
    var name by remember { mutableStateOf(subjectToEdit?.name ?: "") }
    var selectedColorHex by remember { mutableStateOf(subjectToEdit?.colorHex ?: "#2196F3") }
    var targetHoursText by remember { mutableStateOf((subjectToEdit?.targetMinutesPerWeek?.div(60) ?: 5).toString()) }

    val currentColor = try {
        val hex = selectedColorHex.removePrefix("#")
        val colorInt = if (hex.length == 6) ("FF$hex").toLong(16).toInt() else hex.toLong(16).toInt()
        Color(colorInt)
    } catch (e: Exception) {
        Color(0xFF2196F3)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (subjectToEdit == null) "Add New Subject" else "Edit Subject Color & Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Color Selection Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Assign Subject Color:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )

                        // Live Color Preview Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = currentColor,
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = selectedColorHex.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Preset Color Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(Subject.DEFAULT_PALETTE) { (hex, colorLabel) ->
                            val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                            val swatchColor = try {
                                val cleanHex = hex.removePrefix("#")
                                Color(("FF$cleanHex").toLong(16).toInt())
                            } catch (e: Exception) { Color.Gray }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(swatchColor)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorHex = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = colorLabel,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Custom Hex Input
                    OutlinedTextField(
                        value = selectedColorHex,
                        onValueChange = { selectedColorHex = it },
                        label = { Text("Custom Color Hex Code (e.g. #FF5722)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(currentColor)
                            )
                        }
                    )
                }

                OutlinedTextField(
                    value = targetHoursText,
                    onValueChange = { targetHoursText = it.filter { char -> char.isDigit() } },
                    label = { Text("Weekly Goal (Hours)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hours = targetHoursText.toIntOrNull() ?: 5
                    if (name.isNotBlank()) {
                        onSave(name.trim(), selectedColorHex.trim(), hours * 60)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(if (subjectToEdit == null) "Add Subject" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
