package com.championslogger.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF42A5F5),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF1A237E),
    onPrimaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFFF44336),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFF0D1B2A),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1B2838),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF263238),
    onSurfaceVariant = Color(0xFFB0BEC5),
    outline = Color(0xFF37474F)
)

@Composable
fun ChampionsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
