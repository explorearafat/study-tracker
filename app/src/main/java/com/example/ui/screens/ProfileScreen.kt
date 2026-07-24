package com.example.ui.screens

import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.StudySession
import com.example.data.model.Subject
import com.example.data.model.Task
import com.example.data.model.UserProfile
import com.example.notifications.NotificationHelper
import java.util.*

data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isUnlocked: Boolean,
    val progressText: String,
    val badgeColor: Color
)

@Composable
fun UserProfileAvatar(
    avatarUri: String,
    name: String,
    size: Dp = 64.dp,
    onClick: (() -> Unit)? = null,
    showEditBadge: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (onClick != null) {
                    Modifier.clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onClick()
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            avatarUri.startsWith("preset:") -> {
                val presetName = avatarUri.removePrefix("preset:")
                val (bgColor, icon) = when (presetName) {
                    "scholar" -> Color(0xFF4F46E5) to Icons.Default.School
                    "science" -> Color(0xFFEC4899) to Icons.Default.Science
                    "tech" -> Color(0xFF10B981) to Icons.Default.Computer
                    "art" -> Color(0xFFF59E0B) to Icons.Default.Palette
                    "nature" -> Color(0xFF06B6D4) to Icons.Default.Spa
                    else -> Color(0xFF8B5CF6) to Icons.Default.Star
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Avatar Preset",
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.5f)
                    )
                }
            }
            avatarUri.isNotEmpty() -> {
                AsyncImage(
                    model = avatarUri,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF4F46E5),
                                    Color(0xFFEC4899)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (name.take(1)).uppercase(),
                        style = if (size > 60.dp) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        if (showEditBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.32f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Edit Profile Picture",
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize(0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile?,
    sessions: List<StudySession> = emptyList(),
    tasks: List<Task> = emptyList(),
    subjects: List<Subject> = emptyList(),
    onUpdateProfile: (name: String, academicLevel: String, motto: String, targetDailyHours: Float, avatarUri: String, workMins: Int, breakMins: Int, longBreakMins: Int) -> Unit,
    onUpdateReminderSettings: (enabled: Boolean, hour: Int, minute: Int) -> Unit = { _, _, _ -> },
    onResetOnboarding: () -> Unit = {},
    onToggleDarkMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val context = LocalContext.current

    var name by remember(userProfile) { mutableStateOf(userProfile?.name ?: "Alex Scholar") }
    var academicLevel by remember(userProfile) { mutableStateOf(userProfile?.academicLevel ?: "Computer Science Student") }
    var motto by remember(userProfile) { mutableStateOf(userProfile?.motto ?: "Building consistent habits every single day.") }
    var avatarUri by remember(userProfile) { mutableStateOf(userProfile?.avatarUri ?: "") }
    var targetDailyHours by remember(userProfile) { mutableFloatStateOf(userProfile?.targetDailyHours ?: 3.0f) }

    var workMinsText by remember(userProfile) { mutableStateOf((userProfile?.pomodoroWorkMinutes ?: 25).toString()) }
    var breakMinsText by remember(userProfile) { mutableStateOf((userProfile?.pomodoroBreakMinutes ?: 5).toString()) }
    var longBreakMinsText by remember(userProfile) { mutableStateOf((userProfile?.pomodoroLongBreakMinutes ?: 15).toString()) }

    var reminderEnabled by remember(userProfile) { mutableStateOf(userProfile?.reminderEnabled ?: false) }
    var reminderHour by remember(userProfile) { mutableIntStateOf(userProfile?.reminderHour ?: 20) }
    var reminderMinute by remember(userProfile) { mutableIntStateOf(userProfile?.reminderMinute ?: 0) }

    var savedFeedback by remember { mutableStateOf(false) }
    var showAvatarBottomSheet by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            reminderEnabled = true
            onUpdateReminderSettings(true, reminderHour, reminderMinute)
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            reminderEnabled = false
            onUpdateReminderSettings(false, reminderHour, reminderMinute)
        }
    }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            reminderHour = hourOfDay
            reminderMinute = minute
            if (reminderEnabled) {
                onUpdateReminderSettings(true, hourOfDay, minute)
            }
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
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onUpdateProfile(
                name, academicLevel, motto, targetDailyHours, it.toString(),
                workMinsText.toIntOrNull() ?: 25,
                breakMinsText.toIntOrNull() ?: 5,
                longBreakMinsText.toIntOrNull() ?: 15
            )
            savedFeedback = true
        }
    }

    val totalSeconds = remember(sessions) { sessions.sumOf { it.durationSeconds } }
    val totalHours = totalSeconds / 3600f
    val displayHours = totalSeconds / 3600
    val displayMins = (totalSeconds % 3600) / 60

    val completedTasksCount = remember(tasks) { tasks.count { it.isCompleted } }

    val todaySeconds = remember(sessions) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        sessions.filter {
            val sessionCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            sessionCal.get(Calendar.YEAR) == year && sessionCal.get(Calendar.DAY_OF_YEAR) == dayOfYear
        }.sumOf { it.durationSeconds }
    }
    val todayHours = todaySeconds / 3600f

    val achievements = remember(sessions, tasks, subjects, todayHours, targetDailyHours) {
        listOf(
            AchievementBadge(
                id = "first_step",
                title = "First Step",
                description = "Log your first study session",
                icon = Icons.Default.PlayArrow,
                isUnlocked = sessions.isNotEmpty(),
                progressText = if (sessions.isNotEmpty()) "Completed" else "0 / 1 session",
                badgeColor = Color(0xFF3B82F6)
            ),
            AchievementBadge(
                id = "focus_master",
                title = "Focus Master",
                description = "Log over 5 total focus hours",
                icon = Icons.Default.Timer,
                isUnlocked = totalHours >= 5.0f,
                progressText = "${String.format("%.1f", totalHours)} / 5.0 hrs",
                badgeColor = Color(0xFFEC4899)
            ),
            AchievementBadge(
                id = "task_conqueror",
                title = "Task Conqueror",
                description = "Complete 5 study tasks",
                icon = Icons.Default.CheckCircle,
                isUnlocked = completedTasksCount >= 5,
                progressText = "$completedTasksCount / 5 tasks",
                badgeColor = Color(0xFF10B981)
            ),
            AchievementBadge(
                id = "subject_scholar",
                title = "Subject Scholar",
                description = "Create 3 study subjects",
                icon = Icons.Default.School,
                isUnlocked = subjects.size >= 3,
                progressText = "${subjects.size} / 3 subjects",
                badgeColor = Color(0xFFF59E0B)
            ),
            AchievementBadge(
                id = "deep_diver",
                title = "Deep Worker",
                description = "Complete a 50+ min study block",
                icon = Icons.Default.Psychology,
                isUnlocked = sessions.any { it.durationSeconds >= 3000 },
                progressText = if (sessions.any { it.durationSeconds >= 3000 }) "Unlocked" else "In Progress",
                badgeColor = Color(0xFF8B5CF6)
            ),
            AchievementBadge(
                id = "goal_crusher",
                title = "Goal Crusher",
                description = "Hit your daily study target",
                icon = Icons.Default.EmojiEvents,
                isUnlocked = todayHours >= targetDailyHours && targetDailyHours > 0,
                progressText = "${String.format("%.1f", todayHours)} / ${String.format("%.1f", targetDailyHours)} hrs",
                badgeColor = Color(0xFF06B6D4)
            )
        )
    }

    val unlockedCount = achievements.count { it.isUnlocked }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("profile_screen"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "PROFILE & GOALS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Account & Achievements",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                UserProfileAvatar(
                    avatarUri = avatarUri,
                    name = name,
                    size = 68.dp,
                    onClick = { showAvatarBottomSheet = true },
                    showEditBadge = true
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = academicLevel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "“$motto”",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Study Time Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
                    border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TOTAL FOCUS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4338CA)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (displayHours > 0) "${displayHours}h ${displayMins}m" else "${displayMins}m",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E1B4B)
                        )
                        Text(
                            text = "${sessions.size} study sessions",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4338CA)
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5)),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TaskAlt,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TASKS DONE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$completedTasksCount",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF064E3B)
                        )
                        Text(
                            text = "Out of ${tasks.size} total tasks",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF065F46)
                        )
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Daily Study Goal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Set your target focus hours",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "${String.format("%.1f", targetDailyHours)} hrs/day",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Slider(
                    value = targetDailyHours,
                    onValueChange = { targetDailyHours = (it * 2).toInt() / 2f },
                    valueRange = 0.5f..10f,
                    steps = 18,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(1.5f, 2.5f, 4.0f, 6.0f)
                    presets.forEach { preset ->
                        val isSelected = targetDailyHours == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = { targetDailyHours = preset },
                            label = { Text("${preset}h", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Achievements ($unlockedCount/${achievements.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                achievements.chunked(2).forEach { rowBadges ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowBadges.forEach { badge ->
                            Card(
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (badge.isUnlocked)
                                        badge.badgeColor.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (badge.isUnlocked) badge.badgeColor.copy(alpha = 0.4f)
                                    else MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (badge.isUnlocked) badge.badgeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = badge.icon,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        Icon(
                                            imageVector = if (badge.isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = if (badge.isUnlocked) badge.badgeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Text(
                                        text = badge.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Text(
                                        text = badge.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )

                                    Text(
                                        text = badge.progressText,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (badge.isUnlocked) badge.badgeColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (userProfile?.isDarkMode == true) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Dark Mode",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Dark Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Comfortable theme for late-night study sessions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = userProfile?.isDarkMode ?: false,
                    onCheckedChange = { onToggleDarkMode() },
                    modifier = Modifier.testTag("dark_mode_switch")
                )
            }
        }

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Personal Study Info",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TextButton(onClick = { showAvatarBottomSheet = true }) {
                        Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Change Photo", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = academicLevel,
                    onValueChange = { academicLevel = it },
                    label = { Text("Academic Major / Level") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = motto,
                    onValueChange = { motto = it },
                    label = { Text("Learning Motto") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Pomodoro Defaults",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = workMinsText,
                        onValueChange = { workMinsText = it.filter { c -> c.isDigit() } },
                        label = { Text("Focus (m)") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = breakMinsText,
                        onValueChange = { breakMinsText = it.filter { c -> c.isDigit() } },
                        label = { Text("Break (m)") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = longBreakMinsText,
                        onValueChange = { longBreakMinsText = it.filter { c -> c.isDigit() } },
                        label = { Text("Long (m)") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Daily Study Reminders",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Stay consistent with daily goals",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { isChecked ->
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            if (isChecked) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        android.Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        reminderEnabled = true
                                        onUpdateReminderSettings(true, reminderHour, reminderMinute)
                                    } else {
                                        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    reminderEnabled = true
                                    onUpdateReminderSettings(true, reminderHour, reminderMinute)
                                }
                            } else {
                                reminderEnabled = false
                                onUpdateReminderSettings(false, reminderHour, reminderMinute)
                            }
                        },
                        modifier = Modifier.testTag("reminder_switch")
                    )
                }

                AnimatedVisibility(visible = reminderEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

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
                            Column {
                                Text(
                                    text = "Scheduled Reminder Time",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formattedTime,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    timePickerDialog.show()
                                },
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Set Time")
                            }
                        }

                        FilledTonalButton(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                NotificationHelper.showStudyNotification(
                                    context,
                                    "Daily Study Reminder 📚",
                                    "It's time for your daily study session! Keep building consistent habits."
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Test Notification Now", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        FocusShieldCard()

        Button(
            onClick = {
                val wMins = workMinsText.toIntOrNull() ?: 25
                val bMins = breakMinsText.toIntOrNull() ?: 5
                val lbMins = longBreakMinsText.toIntOrNull() ?: 15

                onUpdateProfile(
                    name, academicLevel, motto, targetDailyHours, avatarUri, wMins, bMins, lbMins
                )
                savedFeedback = true
            },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("save_profile_button")
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = "Save")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Profile & Goal Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onResetOnboarding()
            },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Re-run Initial Setup Wizard", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }

        AnimatedVisibility(visible = savedFeedback) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Profile & Daily Target updated successfully!",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }

    if (showAvatarBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAvatarBottomSheet = false },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Profile Picture",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = { showAvatarBottomSheet = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Button(
                    onClick = {
                        showAvatarBottomSheet = false
                        imagePickerLauncher.launch("image/*")
                    },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Choose Photo from Gallery", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                Text(
                    text = "OR CHOOSE A SCHOLAR PRESET",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val presets = listOf(
                    "scholar" to ("Scholar" to Icons.Default.School),
                    "science" to ("Scientist" to Icons.Default.Science),
                    "tech" to ("Developer" to Icons.Default.Computer),
                    "art" to ("Creative" to Icons.Default.Palette),
                    "nature" to ("Growth" to Icons.Default.Spa),
                    "star" to ("Star" to Icons.Default.Star)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    presets.forEach { (key, pair) ->
                        val (label, icon) = pair
                        val presetString = "preset:$key"
                        val isSelected = avatarUri == presetString

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                avatarUri = presetString
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onUpdateProfile(
                                    name, academicLevel, motto, targetDailyHours, presetString,
                                    workMinsText.toIntOrNull() ?: 25,
                                    breakMinsText.toIntOrNull() ?: 5,
                                    longBreakMinsText.toIntOrNull() ?: 15
                                )
                                savedFeedback = true
                                showAvatarBottomSheet = false
                            }
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (avatarUri.isNotEmpty()) {
                    OutlinedButton(
                        onClick = {
                            avatarUri = ""
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onUpdateProfile(
                                name, academicLevel, motto, targetDailyHours, "",
                                workMinsText.toIntOrNull() ?: 25,
                                breakMinsText.toIntOrNull() ?: 5,
                                longBreakMinsText.toIntOrNull() ?: 15
                            )
                            savedFeedback = true
                            showAvatarBottomSheet = false
                        },
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Remove Profile Picture", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
