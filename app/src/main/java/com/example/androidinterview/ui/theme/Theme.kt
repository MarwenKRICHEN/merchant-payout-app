package com.example.androidinterview.ui.theme

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
    primary = Blue,
    primaryContainer = Color(0xFF1B3A4B),
    onPrimaryContainer = Color(0xFF90CAF9),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outlineVariant = Color(0xFF3A3A3A),
)

private val LightColorScheme = lightColorScheme(
    primary = Blue,
    primaryContainer = LightBlue,
    onPrimaryContainer = Blue,
    background = Grey100,
    onBackground = Ink,
    surface = Grey100,
    onSurface = Ink,
    onSurfaceVariant = Grey600,
    outlineVariant = Grey300,
)

@Composable
fun AndroidInterviewTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}