package com.tironflap.frontend.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// These will later be driven by the JSON theme engine
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7C4DFF),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFFCF6679),
    background = Color(0xFF0D0D0F),
    surface = Color(0xFF16161A),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFFE8E8E8),
    onSurface = Color(0xFFE8E8E8)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFF018786),
    background = Color(0xFFF8F8FA),
    surface = Color.White
)

@Composable
fun FrontendTheme(
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
