package com.example.app_a_void.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColorScheme(
    primary = Color(0xFF121212),    // Primary color
    secondary = Color(0xFF1E88E5), // Secondary color
    tertiary = Color(0xFF00BCD4),  // Tertiary color
    background = Color(0xFF121212), // Background color
    surface = Color(0xFF1C1C1C),   // Surface color
    onPrimary = Color(0xFFE0E0E0), // Text on primary
    onSecondary = Color.White,     // Text on secondary
    onBackground = Color(0xFFE0E0E0), // Text on background
    onSurface = Color(0xFFE0E0E0)  // Text on surface
)

@Composable
fun AppAVoidTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorPalette,
        typography = AppTypography, // Apply your typography
        content = content
    )
}
