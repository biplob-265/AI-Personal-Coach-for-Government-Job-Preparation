package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = HighDensityDarkPrimary,
    secondary = HighDensityDarkSecondary,
    tertiary = HighDensityDarkTertiary,
    background = HighDensityDarkBackground,
    surface = HighDensityDarkSurface,
    onPrimary = HighDensityDarkTertiary,
    onSecondary = HighDensityDarkTertiary,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    outlineVariant = HighDensityDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = HighDensityPrimary,
    secondary = HighDensitySecondary,
    tertiary = HighDensityTertiary,
    background = HighDensityBackground,
    surface = HighDensitySurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = HighDensityOnBackground,
    onSurface = HighDensityOnSurface,
    outlineVariant = HighDensityBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Force false to keep light theme beautifully active
    dynamicColor: Boolean = false, // Set to false to force our beautiful custom branding
    content: @Composable () -> Unit,
) {
    // Always use the highly legible, high-contrast Sweet Sapphire LightColorScheme
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
