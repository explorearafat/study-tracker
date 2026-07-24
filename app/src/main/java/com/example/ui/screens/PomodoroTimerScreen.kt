package com.example.ui.screens

import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subject
import com.example.ui.PomodoroMode
import com.example.ui.TimerUiState
import com.example.ui.components.SubjectIcon
import com.example.util.AmbientSoundPlayer
import com.example.util.AmbientSoundType
import com.example.util.FocusShieldManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroTimerScreen(
    timerState: TimerUiState,
    subjects: List<Subject>,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onSetMode: (PomodoroMode) -> Unit,
    onSelectSubject: (Int) -> Unit,
    onAddMinutes: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    var prevRemainingSeconds by remember { mutableIntStateOf(timerState.remainingSeconds) }
    LaunchedEffect(timerState.remainingSeconds) {
        if (timerState.remainingSeconds == 0 && prevRemainingSeconds > 0) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        prevRemainingSeconds = timerState.remainingSeconds
    }

    val progressFraction = if (timerState.totalSeconds > 0) {
        (timerState.remainingSeconds.toFloat() / timerState.totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "TimerProgress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "TimerPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (timerState.isRunning) 1.03f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val minutes = timerState.remainingSeconds / 60
    val seconds = timerState.remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val currentSubject = subjects.find { it.id == timerState.selectedSubjectId }

    val themePrimary = MaterialTheme.colorScheme.primary
    val subjectPrimaryColor = remember(currentSubject?.colorHex, themePrimary) {
        currentSubject?.colorHex?.let {
            try { Color(it.toULong()) } catch (e: Exception) { null }
        } ?: themePrimary
    }

    val subjectSecondaryColor = remember(subjectPrimaryColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(subjectPrimaryColor.toArgb(), hsv)
        hsv[0] = (hsv[0] + 35f) % 360f
        hsv[1] = (hsv[1] * 0.8f).coerceIn(0.3f, 0.95f)
        hsv[2] = (hsv[2] * 1.05f).coerceIn(0.6f, 1.0f)
        Color(android.graphics.Color.HSVToColor(hsv))
    }

    val progressGradientBrush = remember(subjectPrimaryColor, subjectSecondaryColor) {
        Brush.sweepGradient(
            colors = listOf(
                subjectPrimaryColor,
                subjectSecondaryColor,
                subjectPrimaryColor
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("pomodoro_timer_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "FOCUS TIMER",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Deep Work Session",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            PomodoroMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = timerState.mode == mode,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onSetMode(mode)
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = PomodoroMode.entries.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    val modeIcon = when (mode) {
                        PomodoroMode.WORK -> Icons.Default.CenterFocusStrong
                        PomodoroMode.SHORT_BREAK -> Icons.Default.Coffee
                        PomodoroMode.LONG_BREAK -> Icons.Default.SelfImprovement
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = modeIcon, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (timerState.mode == mode) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Select Focus Subject",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (subjects.isEmpty()) {
                Text(
                    text = "No subjects found. Add a subject in the Subjects tab.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(subjects, key = { it.id }) { subject ->
                        val isSelected = subject.id == timerState.selectedSubjectId
                        val color = try {
                            Color(subject.colorHex.toULong())
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onSelectSubject(subject.id)
                            },
                            shape = RoundedCornerShape(16.dp),
                            label = {
                                Text(
                                    text = subject.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                SubjectIcon(
                                    iconName = subject.iconName,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else color,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(240.dp)
                        .scale(pulseScale)
                ) {
                    val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        subjectPrimaryColor.copy(alpha = if (timerState.isRunning) 0.22f else 0.10f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 16.dp.toPx()

                        drawCircle(
                            color = trackColor,
                            style = Stroke(width = strokeWidth)
                        )

                        drawArc(
                            brush = progressGradientBrush,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (timerState.isRunning) subjectPrimaryColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (timerState.isRunning) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                )
                                Text(
                                    text = if (timerState.isRunning) "FOCUSING NOW" else "READY TO FOCUS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (timerState.isRunning) subjectPrimaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = timeFormatted,
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 52.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = subjectPrimaryColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, subjectPrimaryColor.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    tint = subjectPrimaryColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = currentSubject?.name ?: "General Focus",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = subjectPrimaryColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onAddMinutes(5)
                        },
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "+5m", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = {
                            if (timerState.isRunning) {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            } else {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            onToggleTimer()
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = subjectPrimaryColor,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("toggle_timer_fab")
                    ) {
                        Icon(
                            imageVector = if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (timerState.isRunning) "Pause" else "Start",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (timerState.isRunning) "Pause" else "Start Focus",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            onResetTimer()
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            )
                            .testTag("reset_timer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Timer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        AmbientSoundPlayerCard()

        FocusShieldCard()

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Tip",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Pomodoro Mastery Tip",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Focus uninterrupted for 25 minutes, then recharge with a 5-minute break. Stay consistent daily!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusShieldCard(
    context: Context = LocalContext.current
) {
    var isShieldEnabled by remember { mutableStateOf(FocusShieldManager.isShieldEnabled(context)) }
    var isDndEnabled by remember { mutableStateOf(FocusShieldManager.isDndEnabled(context)) }
    var blockedAppIds by remember { mutableStateOf(FocusShieldManager.getBlockedAppIds(context)) }

    var hasUsagePermission by remember { mutableStateOf(FocusShieldManager.hasUsageStatsPermission(context)) }
    var hasDndPermission by remember { mutableStateOf(FocusShieldManager.hasDndPermission(context)) }

    LaunchedEffect(Unit) {
        hasUsagePermission = FocusShieldManager.hasUsageStatsPermission(context)
        hasDndPermission = FocusShieldManager.hasDndPermission(context)
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isShieldEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Focus App Blocker & DND Guard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isShieldEnabled) "Active during focus timer" else "App blocker paused",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = isShieldEnabled,
                    onCheckedChange = { enabled ->
                        isShieldEnabled = enabled
                        FocusShieldManager.setShieldEnabled(context, enabled)
                    }
                )
            }

            if (isShieldEnabled) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Auto Silence Notifications (DND)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Switch(
                        checked = isDndEnabled,
                        onCheckedChange = { enabled ->
                            isDndEnabled = enabled
                            FocusShieldManager.setDndEnabled(context, enabled)
                        }
                    )
                }

                if (!hasUsagePermission) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF3C7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Usage Access needed to block social apps",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF92400E),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            TextButton(
                                onClick = { FocusShieldManager.openUsageStatsSettings(context) }
                            ) {
                                Text("Grant", fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                            }
                        }
                    }
                }

                if (isDndEnabled && !hasDndPermission) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFDBEAFE),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsOff,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "DND Permission needed to mute notifications",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF1E40AF),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            TextButton(
                                onClick = { FocusShieldManager.openDndSettings(context) }
                            ) {
                                Text("Grant", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Social Media Apps to Block During Focus:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FocusShieldManager.AVAILABLE_SOCIAL_APPS.forEach { app ->
                            val isSelected = blockedAppIds.contains(app.id)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    FocusShieldManager.toggleAppBlocked(context, app.id)
                                    blockedAppIds = FocusShieldManager.getBlockedAppIds(context)
                                },
                                label = {
                                    Text(
                                        text = app.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                )
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Timer pause/finish হলে সব app automatic unblock হবে!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AmbientSoundPlayerCard() {
    var selectedSoundType by remember { mutableStateOf(AmbientSoundPlayer.getCurrentType()) }
    var currentVolume by remember { mutableFloatStateOf(AmbientSoundPlayer.getVolume()) }
    val isPlaying = selectedSoundType != AmbientSoundType.OFF

    DisposableEffect(Unit) {
        onDispose {
            // Option: Keep playing or let user retain control
        }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isPlaying) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.VolumeOff,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Ambient Focus Sound Player",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isPlaying) "Playing ${selectedSoundType.displayName}" else "Background sound paused",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = {
                        if (isPlaying) {
                            selectedSoundType = AmbientSoundType.OFF
                            AmbientSoundPlayer.playSound(AmbientSoundType.OFF)
                        } else {
                            selectedSoundType = AmbientSoundType.RAIN
                            AmbientSoundPlayer.playSound(AmbientSoundType.RAIN)
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (isPlaying) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause Sound" else "Play Sound",
                        tint = if (isPlaying) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select Sound Atmosphere:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AmbientSoundType.values().forEach { soundType ->
                        val isSelected = selectedSoundType == soundType
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedSoundType = soundType
                                AmbientSoundPlayer.playSound(soundType)
                            },
                            label = {
                                Text(
                                    text = soundType.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                    }
                }
            }

            if (isPlaying) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = {
                            val newVol = if (currentVolume > 0f) 0f else 0.5f
                            currentVolume = newVol
                            AmbientSoundPlayer.setVolume(newVol)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (currentVolume == 0f) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = "Mute or Unmute",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Slider(
                        value = currentVolume,
                        onValueChange = { vol ->
                            currentVolume = vol
                            AmbientSoundPlayer.setVolume(vol)
                        },
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.secondary,
                            activeTrackColor = MaterialTheme.colorScheme.secondary
                        )
                    )

                    Text(
                        text = "${(currentVolume * 100f).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.width(42.dp)
                    )
                }
            }
        }
    }
}
