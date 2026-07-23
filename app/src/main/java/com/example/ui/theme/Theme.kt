package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = ForestOnPrimary,
    primaryContainer = SagePrimaryContainer,
    onPrimaryContainer = OnSagePrimaryContainer,
    secondary = SlateSecondary,
    onSecondary = SlateOnSecondary,
    secondaryContainer = LightSageSecondaryContainer,
    onSecondaryContainer = OnLightSageSecondaryContainer,
    tertiary = MintTertiary,
    tertiaryContainer = MintTertiaryContainer,
    background = GeoBackground,
    onBackground = GeoOnBackground,
    surface = GeoSurface,
    onSurface = GeoOnSurface,
    surfaceVariant = GeoSurfaceVariant,
    onSurfaceVariant = GeoOnSurfaceVariant,
    outline = GeoOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkMintPrimary,
    onPrimary = DarkForestOnPrimary,
    primaryContainer = DarkForestPrimaryContainer,
    onPrimaryContainer = DarkOnForestPrimaryContainer,
    secondary = DarkSlateSecondary,
    onSecondary = Color(0xFF191C19),
    secondaryContainer = DarkSageSecondaryContainer,
    onSecondaryContainer = DarkGeoOnSurface,
    tertiary = Color(0xFFB2F5B6),
    background = DarkGeoBackground,
    onBackground = DarkGeoOnBackground,
    surface = DarkGeoSurface,
    onSurface = DarkGeoOnSurface,
    surfaceVariant = DarkGeoSurfaceVariant,
    onSurfaceVariant = DarkGeoOnSurfaceVariant,
    outline = DarkGeoOutline
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

