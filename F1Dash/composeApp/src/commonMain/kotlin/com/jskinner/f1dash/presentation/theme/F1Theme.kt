package com.jskinner.f1dash.presentation.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// F1 Dark Theme Colors
private val F1Red = Color(0xFFFF1E1E)
private val F1White = Color(0xFFFFFFFF)
private val F1Black = Color(0xFF000000)
private val F1DarkGray = Color(0xFF1A1A1A)
private val F1MediumGray = Color(0xFF2D2D2D)
private val F1LightGray = Color(0xFF4A4A4A)
private val F1Silver = Color(0xFFC0C0C0)

// Custom dark color scheme for F1 app
private val F1DarkColorScheme = darkColorScheme(
    primary = F1Red,
    onPrimary = F1White,
    primaryContainer = F1MediumGray,
    onPrimaryContainer = F1White,
    
    secondary = F1Silver,
    onSecondary = F1Black,
    secondaryContainer = F1LightGray,
    onSecondaryContainer = F1White,
    
    tertiary = F1Red,
    onTertiary = F1White,
    tertiaryContainer = F1DarkGray,
    onTertiaryContainer = F1White,
    
    error = Color(0xFFFF6B6B),
    onError = F1White,
    errorContainer = Color(0xFF8B0000),
    onErrorContainer = F1White,
    
    background = F1Black,
    onBackground = F1White,
    
    surface = F1DarkGray,
    onSurface = F1White,
    surfaceVariant = F1MediumGray,
    onSurfaceVariant = F1Silver,
    
    outline = F1LightGray,
    outlineVariant = F1MediumGray,
    
    scrim = F1Black,
    
    inverseSurface = F1White,
    inverseOnSurface = F1Black,
    inversePrimary = F1Red,
    
    surfaceDim = F1DarkGray,
    surfaceBright = F1MediumGray,
    surfaceContainerLowest = F1Black,
    surfaceContainerLow = F1DarkGray,
    surfaceContainer = F1MediumGray,
    surfaceContainerHigh = F1LightGray,
    surfaceContainerHighest = Color(0xFF5A5A5A)
)

@Composable
fun F1Theme(
    content: @Composable () -> Unit
) {
    val colorScheme = F1DarkColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}