package com.example.dndinventorymanager.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DnDColorScheme = darkColorScheme(
    primary = DnDGold,
    onPrimary = DnDDarkBg,
    secondary = DnDBurgundy,
    onSecondary = Color.White,
    tertiary = DnDGold,
    onTertiary = DnDDarkBg,
    background = DnDDarkBg,
    onBackground = DnDLightText,
    surface = DnDCardBg,
    onSurface = DnDLightText,
    surfaceVariant = Color(0xFF3a3a3a),
    onSurfaceVariant = DnDMutedText,
    error = DnDWarningRed,
    onError = Color.White
)

@Composable
fun DnDInventoryManagerTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DnDColorScheme,
        typography = Typography,
        content = content
    )
}