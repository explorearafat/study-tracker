package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudySession
import com.example.data.model.Subject
import com.example.data.model.Task
import com.example.data.model.UserProfile
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile?,
    sessions: List<StudySession> = emptyList(),
    tasks: List<Task> = emptyList(),
    subjects: List<Subject> = emptyList(),
    onUpdateProfile: (name: String, academicLevel: String, motto: String, targetDailyHours: Float, avatarUri: String, workMins: Int, breakMins: Int, longBreakMins: Int) -> Unit,
    onToggleDarkMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var name by remember(userProfile) { mutableStateOf(userProfile?.name ?: "Alex Scholar") }
    var academicLevel by remember(userProfile) { mutableStateOf(userProfile?.academicLevel ?: "Computer Science Student") }
    var motto by remember(userProfile) { mutableStateOf(userProfile?.motto ?: "Building consistent habits every single day.") }
    var targetDailyHours by remember(userProfile) { mutableFloatStateOf(userProfile?.targetDailyHours ?: 3.0f) }

    var workMinsText by remember(userProfile) { mutableStateOf((userProfile?.pomodoroWorkMinutes ?: 25).toString()) }
    var breakMinsText by remember(userProfile) { mutableStateOf((userProfile?.pomodoroBreakMinutes ?: 5).toString()) }
    var longBreakMinsText by remember(userProfile) { mutableStateOf((userProfile?.pomodoroLongBreakMinutes ?: 15).toString()) }

    var savedFeedback by remember { mutableStateOf(false) }

    // Analytics calculations
    val totalSeconds = remember(sessions) { sessions.sumOf { it.durationSeconds } }
    val totalHours = totalSeconds / 3600f
    val displayHours = totalSeconds / 3600
    val displayMins = (totalSeconds % 3600) / 60

    val completedTasksCount = remember(tasks) { tasks.count { it.isCompleted } }

    // Calculate today's study hours
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

    // Achievements list
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
        // Top Subtitle & Header
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

        // Profile Header Card
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
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
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
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

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

        // Total Study Time & Summary Stats Cards
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
                // Total Focus Time Card
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

                // Completed Tasks Card
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

        // Daily Study Goal Settings Card
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

                // Slider Control
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

                // Quick Preset Pills
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

        // Achievements & Badges Grid
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

        // Dark Mode Toggle Card
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

        // Personal Information Form
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
                    text = "Personal Study Info",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

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

        // Pomodoro Timer Defaults
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

        // Save Profile Button
        Button(
            onClick = {
                val wMins = workMinsText.toIntOrNull() ?: 25
                val bMins = breakMinsText.toIntOrNull() ?: 5
                val lbMins = longBreakMinsText.toIntOrNull() ?: 15

                onUpdateProfile(
                    name, academicLevel, motto, targetDailyHours, "", wMins, bMins, lbMins
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
}
