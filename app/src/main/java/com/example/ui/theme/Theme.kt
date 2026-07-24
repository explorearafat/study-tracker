package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = IndigoOnPrimary,
    primaryContainer = IndigoPrimaryContainer,
    onPrimaryContainer = OnIndigoPrimaryContainer,
    secondary = SlateSecondary,
    onSecondary = SlateOnSecondary,
    secondaryContainer = SlateSecondaryContainer,
    onSecondaryContainer = OnSlateSecondaryContainer,
    tertiary = MintTertiary,
    tertiaryContainer = MintTertiaryContainer,
    background = SoftBackground,
    onBackground = SoftOnBackground,
    surface = SoftSurface,
    onSurface = SoftOnSurface,
    surfaceVariant = SoftSurfaceVariant,
    onSurfaceVariant = SoftOnSurfaceVariant,
    outline = SoftOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkIndigoPrimary,
    onPrimary = DarkIndigoOnPrimary,
    primaryContainer = DarkIndigoPrimaryContainer,
    onPrimaryContainer = DarkOnIndigoPrimaryContainer,
    secondary = DarkSlateSecondary,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = DarkSlateSecondaryContainer,
    onSecondaryContainer = DarkSoftOnSurface,
    tertiary = MintTertiary,
    background = DarkSoftBackground,
    onBackground = DarkSoftOnBackground,
    surface = DarkSoftSurface,
    onSurface = DarkSoftOnSurface,
    surfaceVariant = DarkSoftSurfaceVariant,
    onSurfaceVariant = DarkSoftOnSurfaceVariant,
    outline = DarkSoftOutline
)

@Composable
fun StudyTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

