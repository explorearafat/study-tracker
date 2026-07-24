package com.example.ui.screens

import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.Subject
import com.example.data.model.UserProfile
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    userProfile: UserProfile?,
    subjects: List<Subject>,
    onCompleteOnboarding: (
        name: String,
        academicLevel: String,
        motto: String,
        targetDailyHours: Float,
        avatarUri: String,
        initialTasks: List<Triple<String, Int, String>>,
        reminderEnabled: Boolean,
        reminderHour: Int,
        reminderMinute: Int
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current

    var currentStep by remember { mutableIntStateOf(1) }

    var name by remember(userProfile) { mutableStateOf(userProfile?.name ?: "Alex Scholar") }
    var academicLevel by remember(userProfile) { mutableStateOf(userProfile?.academicLevel ?: "Computer Science Student") }
    var motto by remember(userProfile) { mutableStateOf(userProfile?.motto ?: "Building consistent habits every single day.") }
    var avatarUri by remember(userProfile) { mutableStateOf(userProfile?.avatarUri ?: "preset:scholar") }
    var targetDailyHours by remember(userProfile) { mutableFloatStateOf(userProfile?.targetDailyHours ?: 3.5f) }

    val setupTasks = remember {
        mutableStateListOf(
            Triple("Review daily lecture notes", subjects.firstOrNull()?.id ?: 1, "High"),
            Triple("Solve 3 practice problems", subjects.getOrNull(1)?.id ?: subjects.firstOrNull()?.id ?: 1, "Medium")
        )
    }

    var taskTitleInput by remember { mutableStateOf("") }
    var selectedSubjectId by remember(subjects) { mutableIntStateOf(subjects.firstOrNull()?.id ?: 1) }
    var selectedPriority by remember { mutableStateOf("Medium") }

    var reminderEnabled by remember { mutableStateOf(true) }
    var reminderHour by remember { mutableIntStateOf(20) }
    var reminderMinute by remember { mutableIntStateOf(0) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            reminderEnabled = true
        } else {
            reminderEnabled = false
        }
    }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            reminderHour = hourOfDay
            reminderMinute = minute
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        },
        reminderHour,
        reminderMinute,
        false
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            avatarUri = it.toString()
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Study Tracker Setup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Step $currentStep of 2",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { if (currentStep == 1) 0.5f else 1.0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn() togetherWith
                                slideOutHorizontally(animationSpec = tween(300)) { -it } + fadeOut()
                    } else {
                        slideInHorizontally(animationSpec = tween(300)) { -it } + fadeIn() togetherWith
                                slideOutHorizontally(animationSpec = tween(300)) { it } + fadeOut()
                    }
                },
                modifier = Modifier.weight(1f)
            ) { step ->
                if (step == 1) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Column {
                            Text(
                                text = "Setup Your Profile 👋",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Customize your identity and set your daily study goal.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                UserProfileAvatar(
                                    avatarUri = avatarUri,
                                    name = name.ifBlank { "S" },
                                    size = 80.dp,
                                    showEditBadge = true,
                                    onClick = { imagePickerLauncher.launch("image/*") }
                                )

                                Text(
                                    text = "Choose Profile Picture or Preset",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                val presets = listOf(
                                    "scholar" to Icons.Default.School,
                                    "science" to Icons.Default.Science,
                                    "tech" to Icons.Default.Computer,
                                    "art" to Icons.Default.Palette,
                                    "nature" to Icons.Default.Spa,
                                    "star" to Icons.Default.Star
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    presets.forEach { (key, icon) ->
                                        val presetKey = "preset:$key"
                                        val isSelected = avatarUri == presetKey
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clickable {
                                                    avatarUri = presetKey
                                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = key,
                                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Your Full Name") },
                            placeholder = { Text("e.g. Alex Scholar") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = academicLevel,
                            onValueChange = { academicLevel = it },
                            label = { Text("Academic Level / Major / Grade") },
                            placeholder = { Text("e.g. Computer Science Student") },
                            leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = motto,
                            onValueChange = { motto = it },
                            label = { Text("Personal Motto / Motivation") },
                            placeholder = { Text("e.g. Continuous learning yields mastery.") },
                            leadingIcon = { Icon(Icons.Default.FormatQuote, contentDescription = null) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Text("Daily Target Study Hours", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    }
                                    Text(
                                        text = "${(targetDailyHours * 10).roundToInt() / 10.0} hrs/day",
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Slider(
                                    value = targetDailyHours,
                                    onValueChange = { targetDailyHours = it },
                                    valueRange = 1.0f..10.0f,
                                    steps = 17,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Column {
                            Text(
                                text = "Setup Daily Tasks 🎯",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Set your initial goals & schedule daily study reminders.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Add Initial Task / Study Goal",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                OutlinedTextField(
                                    value = taskTitleInput,
                                    onValueChange = { taskTitleInput = it },
                                    label = { Text("Task Title") },
                                    placeholder = { Text("e.g. Complete Math Chapter 4 exercises") },
                                    shape = RoundedCornerShape(14.dp),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    var subjectExpanded by remember { mutableStateOf(false) }
                                    val currentSubject = subjects.find { it.id == selectedSubjectId } ?: subjects.firstOrNull()

                                    Box(modifier = Modifier.weight(1f)) {
                                        OutlinedButton(
                                            onClick = { subjectExpanded = true },
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = currentSubject?.name ?: "Subject",
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = subjectExpanded,
                                            onDismissRequest = { subjectExpanded = false }
                                        ) {
                                            subjects.forEach { sub ->
                                                DropdownMenuItem(
                                                    text = { Text(sub.name) },
                                                    onClick = {
                                                        selectedSubjectId = sub.id
                                                        subjectExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    val priorities = listOf("High", "Medium", "Low")
                                    var priorityExpanded by remember { mutableStateOf(false) }

                                    Box(modifier = Modifier.weight(1f)) {
                                        OutlinedButton(
                                            onClick = { priorityExpanded = true },
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "$selectedPriority Priority",
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = priorityExpanded,
                                            onDismissRequest = { priorityExpanded = false }
                                        ) {
                                            priorities.forEach { prio ->
                                                DropdownMenuItem(
                                                    text = { Text(prio) },
                                                    onClick = {
                                                        selectedPriority = prio
                                                        priorityExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (taskTitleInput.isNotBlank()) {
                                            setupTasks.add(Triple(taskTitleInput.trim(), selectedSubjectId, selectedPriority))
                                            taskTitleInput = ""
                                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        }
                                    },
                                    enabled = taskTitleInput.isNotBlank(),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add to Daily Tasks")
                                }
                            }
                        }

                        Text(
                            text = "Initial Tasks List (${setupTasks.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (setupTasks.isEmpty()) {
                            Text(
                                text = "No tasks added yet. Add at least one task to get started!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                setupTasks.forEachIndexed { index, (title, subId, priority) ->
                                    val subName = subjects.find { it.id == subId }?.name ?: "Subject"
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(imageVector = Icons.Default.CheckCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                Column {
                                                    Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                    Text(text = "$subName • $priority Priority", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }

                                            IconButton(onClick = { setupTasks.removeAt(index) }) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Column {
                                            Text("Daily Study Reminder", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            Text("Notify me daily to stay on track", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Switch(
                                        checked = reminderEnabled,
                                        onCheckedChange = { isChecked ->
                                            if (isChecked) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                    val hasPermission = ContextCompat.checkSelfPermission(
                                                        context,
                                                        android.Manifest.permission.POST_NOTIFICATIONS
                                                    ) == PackageManager.PERMISSION_GRANTED

                                                    if (hasPermission) {
                                                        reminderEnabled = true
                                                    } else {
                                                        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                                    }
                                                } else {
                                                    reminderEnabled = true
                                                }
                                            } else {
                                                reminderEnabled = false
                                            }
                                        }
                                    )
                                }

                                if (reminderEnabled) {
                                    val formattedTime = remember(reminderHour, reminderMinute) {
                                        val h = if (reminderHour % 12 == 0) 12 else reminderHour % 12
                                        val amPm = if (reminderHour >= 12) "PM" else "AM"
                                        String.format("%02d:%02d %s", h, reminderMinute, amPm)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Reminder Time: $formattedTime",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        OutlinedButton(
                                            onClick = { timePickerDialog.show() },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Change")
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentStep == 2) {
                    OutlinedButton(
                        onClick = {
                            currentStep = 1
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        },
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Back", fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        if (currentStep == 1) {
                            currentStep = 2
                        } else {
                            onCompleteOnboarding(
                                name.ifBlank { "Alex Scholar" },
                                academicLevel.ifBlank { "Student Scholar" },
                                motto.ifBlank { "Building consistent habits every single day." },
                                targetDailyHours,
                                avatarUri,
                                setupTasks.toList(),
                                reminderEnabled,
                                reminderHour,
                                reminderMinute
                            )
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                ) {
                    Text(
                        text = if (currentStep == 1) "Next: Daily Tasks ->" else "Complete Setup 🚀",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
