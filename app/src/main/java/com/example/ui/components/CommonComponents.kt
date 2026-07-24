package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SubjectColors

fun Modifier.iosClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "iosClickScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            detectTapGestures(
                onPress = {
                    isPressed = true
                    try {
                        awaitRelease()
                    } finally {
                        isPressed = false
                    }
                },
                onTap = {
                    onClick()
                }
            )
        }
}

@Composable
fun SubjectIcon(
    iconName: String,
    tint: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val vector: ImageVector = when (iconName.lowercase()) {
        "calculator", "math" -> Icons.Default.Calculate
        "science", "physics", "chemistry" -> Icons.Default.Science
        "computer", "coding", "dev" -> Icons.Default.Computer
        "language", "english", "spanish" -> Icons.Default.Translate
        "brush", "art" -> Icons.Default.Brush
        "history" -> Icons.Default.HistoryEdu
        "music" -> Icons.Default.MusicNote
        "biology" -> Icons.Default.Biotech
        else -> Icons.Default.Book
    }

    Icon(
        imageVector = vector,
        contentDescription = contentDescription ?: iconName,
        tint = tint,
        modifier = modifier
    )
}

@Composable
fun CircularProgressGoalCard(
    currentMinutes: Int,
    targetMinutes: Int,
    title: String = "Daily Study Goal",
    modifier: Modifier = Modifier
) {
    val progressFraction = if (targetMinutes > 0) {
        (currentMinutes.toFloat() / targetMinutes.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "GoalProgress"
    )

    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("circular_goal_card")
    ) {
        Row(
            modifier = Modifier
                .padding(22.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                val hours = currentMinutes / 60
                val mins = currentMinutes % 60
                val targetHours = targetMinutes / 60
                val targetMins = targetMinutes % 60

                Text(
                    text = "${hours}h ${mins}m / ${targetHours}h ${targetMins}m",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                val percentage = (animatedProgress * 100).toInt()
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (percentage >= 100) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (percentage >= 100) "🎉 Goal Achieved!" else "$percentage% Completed Today",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (percentage >= 100) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Circular progress ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(88.dp)
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 10.dp.toPx()
                    drawCircle(
                        color = outlineColor,
                        style = Stroke(width = strokeWidth)
                    )
                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun PriorityChip(priority: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (priority.lowercase()) {
        "high" -> Pair(Color(0xFFFEE2E2), Color(0xFFDC2626))
        "medium" -> Pair(Color(0xFFFEF3C7), Color(0xFFD97706))
        else -> Pair(Color(0xFFDCE5D8), Color(0xFF006D32))
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = priority.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun StudyBarChart(
    daysData: List<Pair<String, Float>>, // Pair(DayLabel, Hours)
    maxHours: Float = 5.0f,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("study_bar_chart")
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(
                text = "Weekly Study Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                daysData.forEach { (label, hours) ->
                    val fraction = (hours / maxHours.coerceAtLeast(1.0f)).coerceIn(0f, 1f)
                    val animatedFraction by animateFloatAsState(
                        targetValue = fraction,
                        animationSpec = tween(durationMillis = 600),
                        label = "BarHeight"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (hours > 0) String.format("%.1fh", hours) else "",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .weight(1f),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // Track
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(20.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(trackColor)
                            )
                            // Filled Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(animatedFraction.coerceAtLeast(0.04f))
                                    .width(20.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(primaryColor)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SubjectColorPickerRow(
    selectedColorHex: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Subject Theme Color",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SubjectColors.forEach { colorLong ->
                val isSelected = colorLong == selectedColorHex
                val color = Color(colorLong)

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(colorLong) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IconPickerRow(
    selectedIconName: String,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val icons = listOf(
        "Book", "Calculator", "Science", "Computer",
        "Language", "Brush", "History", "Music"
    )

    Column(modifier = modifier) {
        Text(
            text = "Subject Icon",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            icons.forEach { iconName ->
                val isSelected = iconName.lowercase() == selectedIconName.lowercase()
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(42.dp)
                        .clickable { onIconSelected(iconName) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        SubjectIcon(
                            iconName = iconName,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
