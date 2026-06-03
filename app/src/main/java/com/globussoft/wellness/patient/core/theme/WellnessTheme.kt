package com.globussoft.wellness.patient.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

private fun buildColorScheme(primaryColor: Color) = lightColorScheme(
    primary = primaryColor,
    onPrimary = WellnessOnPrimary,
    primaryContainer = WellnessPrimaryContainer,
    onPrimaryContainer = WellnessOnPrimaryContainer,
    secondary = WellnessSecondary,
    onSecondary = WellnessOnSecondary,
    secondaryContainer = WellnessSecondaryContainer,
    onSecondaryContainer = WellnessOnSecondaryContainer,
    background = WellnessBackground,
    onBackground = WellnessOnSurface,
    surface = WellnessSurface,
    onSurface = WellnessOnSurface,
    surfaceVariant = WellnessSurfaceVariant,
    onSurfaceVariant = WellnessOnSurfaceVariant,
    outline = WellnessOutline,
    error = WellnessError,
    onError = WellnessOnError,
    errorContainer = WellnessErrorContainer,
    onErrorContainer = WellnessOnErrorContainer,
)

fun parseBrandColor(hex: String?): Color {
    if (hex.isNullOrBlank()) return WellnessPrimary
    return runCatching {
        Color(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"))
    }.getOrDefault(WellnessPrimary)
}

@Composable
fun WellnessTheme(
    brandColor: Color = WellnessPrimary,
    content: @Composable () -> Unit,
) {
    val colorScheme = remember(brandColor) { buildColorScheme(brandColor) }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = WellnessTypography,
        shapes = WellnessShapes,
        content = content,
    )
}
