package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.example.data.model.StudySession
import com.example.data.model.Subject
import com.example.ui.components.StudyBarChart
import com.example.ui.components.SubjectIcon
import java.util.*

@Composable
fun AnalyticsScreen(
    sessions: List<StudySession>,
    subjects: List<Subject>,
    modifier: Modifier = Modifier
) {
    val totalSeconds = remember(sessions) { sessions.sumOf { it.durationSeconds } }
    val totalHours = totalSeconds / 3600f

    val calendar = remember { Calendar.getInstance() }
    val todayMs = remember {
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val todaySeconds = remember(sessions, todayMs) {
        sessions.filter { it.timestamp >= todayMs }.sumOf { it.durationSeconds }
    }
    val todayHours = todaySeconds / 3600f

    val weekAgoMs = remember { todayMs - (6 * 86400000L) }
    val weeklySessions = remember(sessions, weekAgoMs) {
        sessions.filter { it.timestamp >= weekAgoMs }
    }
    val weeklyHours = weeklySessions.sumOf { it.durationSeconds } / 3600f

    // Prepare 7-day data for chart
    val weeklyDaysData = remember(sessions, todayMs) {
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val dayDataList = mutableListOf<Pair<String, Float>>()

        val tempCal = Calendar.getInstance()
        for (i in 6 downTo 0) {
            tempCal.timeInMillis = todayMs - (i * 86400000L)
            val dayStart = tempCal.timeInMillis
            val dayEnd = dayStart + 86400000L - 1

            val dayOfWeekIndex = (tempCal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0
            val dayLabel = days.getOrElse(dayOfWeekIndex) { "Day" }

            val secs = sessions
                .filter { it.timestamp in dayStart..dayEnd }
                .sumOf { it.durationSeconds }

            dayDataList.add(Pair(dayLabel, secs / 3600f))
        }
        dayDataList
    }

    // Subject Breakdown Data
    val subjectBreakdown = remember(sessions, subjects) {
        subjects.map { subject ->
            val subSecs = sessions.filter { it.subjectId == subject.id }.sumOf { it.durationSeconds }
            val fraction = if (totalSeconds > 0) subSecs.toFloat() / totalSeconds.toFloat() else 0f
            Triple(subject, subSecs / 60, fraction)
        }.sortedByDescending { it.second }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "LEARNING METRICS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Performance & Visuals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Summary Cards Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Today",
                    value = String.format("%.1fh", todayHours),
                    icon = Icons.Default.Today,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "7 Days",
                    value = String.format("%.1fh", weeklyHours),
                    icon = Icons.Default.DateRange,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Total",
                    value = String.format("%.1fh", totalHours),
                    icon = Icons.Default.MilitaryTech,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Weekly Bar Chart
        item {
            StudyBarChart(
                daysData = weeklyDaysData,
                maxHours = (weeklyDaysData.maxOfOrNull { it.second } ?: 3.0f).coerceAtLeast(3.0f)
            )
        }

        // Subject Breakdown
        item {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = "Subject Learning Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (subjectBreakdown.isEmpty()) {
                        Text(
                            text = "No study data available yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            subjectBreakdown.forEach { (subject, totalMins, fraction) ->
                                val color = Color(subject.colorHex)
                                val hours = totalMins / 60
                                val mins = totalMins % 60

                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = CircleShape,
                                                color = color.copy(alpha = 0.15f),
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    SubjectIcon(
                                                        iconName = subject.iconName,
                                                        tint = color,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = subject.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Text(
                                            text = "${hours}h ${mins}m (${(fraction * 100).toInt()}%)",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    LinearProgressIndicator(
                                        progress = { fraction },
                                        color = color,
                                        trackColor = color.copy(alpha = 0.15f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

