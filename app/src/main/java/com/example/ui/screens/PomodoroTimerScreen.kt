package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

import com.example.data.Subject
import com.example.ui.MainViewModel
import com.example.util.AmbientSoundPlayer
import com.example.util.AmbientSoundType
import com.example.util.FocusShieldManager

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PomodoroTimerScreen(
    viewModel: MainViewModel,
    initialSubject: Subject? = null
) {
    val subjects by viewModel.subjects.collectAsState()
    val context = LocalContext.current

    var selectedSubject by remember { mutableStateOf(initialSubject ?: subjects.firstOrNull()) }
    LaunchedEffect(subjects) {
        if (selectedSubject == null && subjects.isNotEmpty()) {
            selectedSubject = subjects.first()
        }
    }

    var isTimerRunning by remember { mutableStateOf(false) }
    var totalTimerSeconds by remember { mutableIntStateOf(25 * 60) }
    var remainingSeconds by remember { mutableIntStateOf(25 * 60) }

    val activeColor = selectedSubject?.toColor() ?: MaterialTheme.colorScheme.primary

    LaunchedEffect(isTimerRunning, remainingSeconds) {
        if (isTimerRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
        } else if (isTimerRunning && remainingSeconds == 0) {
            isTimerRunning = false
            selectedSubject?.let { sub ->
                val elapsedMins = (totalTimerSeconds / 60).coerceAtLeast(1)
                viewModel.logFocusSession(sub, elapsedMins)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Focus Session & Sound Player",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (selectedSubject != null) "Focusing on ${selectedSubject?.name}" else "Select subject to focus",
                            style = MaterialTheme.typography.bodySmall,
                            color = activeColor
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Subject Selection Bar with custom subject colors
            if (subjects.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Focus Subject Target:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(subjects, key = { it.id }) { subject ->
                            val isSel = selectedSubject?.id == subject.id
                            val subColor = subject.toColor()

                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    selectedSubject = subject
                                },
                                label = {
                                    Text(
                                        text = subject.name,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
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
                }
            }

            // Central Circular Timer Display styled in assigned Subject Color
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(2.dp, activeColor.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val minutes = remainingSeconds / 60
                    val seconds = remainingSeconds % 60
                    val timeFormatted = "%02d:%02d".format(minutes, seconds)
                    val progressFraction = (remainingSeconds.toFloat() / totalTimerSeconds.coerceAtLeast(1)).coerceIn(0f, 1f)

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(200.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier.fillMaxSize(),
                            color = activeColor,
                            strokeWidth = 12.dp,
                            trackColor = activeColor.copy(alpha = 0.15f)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = timeFormatted,
                                fontSize = 44.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = activeColor
                            )
                            Text(
                                text = selectedSubject?.name ?: "Focus Timer",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Timer Duration Preset Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(15, 25, 45, 60).forEach { mins ->
                            OutlinedButton(
                                onClick = {
                                    isTimerRunning = false
                                    totalTimerSeconds = mins * 60
                                    remainingSeconds = mins * 60
                                },
                                border = BorderStroke(
                                    1.dp,
                                    if (totalTimerSeconds == mins * 60) activeColor else MaterialTheme.colorScheme.outline
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (totalTimerSeconds == mins * 60) activeColor.copy(alpha = 0.15f) else Color.Transparent
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${mins}m",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalTimerSeconds == mins * 60) activeColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Play/Pause & Reset Controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledIconButton(
                            onClick = {
                                isTimerRunning = false
                                remainingSeconds = totalTimerSeconds
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset Timer")
                        }

                        Button(
                            onClick = { isTimerRunning = !isTimerRunning },
                            colors = ButtonDefaults.buttonColors(containerColor = activeColor),
                            modifier = Modifier
                                .height(52.dp)
                                .weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTimerRunning) "Pause Session" else "Start Focus",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // In-App Ambient Sound Player Component
            AmbientSoundPlayerCard()

            // Focus Shield DND Card
            FocusShieldCard(context = context, subjectName = selectedSubject?.name ?: "Study Session")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AmbientSoundPlayerCard() {
    var selectedSoundType by remember { mutableStateOf(AmbientSoundPlayer.getCurrentType()) }
    var currentVolume by remember { mutableFloatStateOf(AmbientSoundPlayer.getVolume()) }
    val isPlaying = selectedSoundType != AmbientSoundType.OFF

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                            text = "Ambient Sound Player",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
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
                    text = "Sound Atmosphere:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
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
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = Color.White
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
                            contentDescription = "Mute/Unmute",
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

@Composable
fun FocusShieldCard(context: android.content.Context, subjectName: String) {
    val isShieldOn = FocusShieldManager.isShieldActive

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isShieldOn)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(
            1.dp,
            if (isShieldOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = if (isShieldOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(32.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Focus Shield DND Guard",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isShieldOn)
                        "Active: Distraction apps blocked & foreground guard running"
                    else
                        "Enable to block social notifications during sessions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = isShieldOn,
                onCheckedChange = { checked ->
                    FocusShieldManager.toggleShield(context, checked, subjectName)
                }
            )
        }
    }
}
