package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PremiumWhite,
    secondary = PremiumSilver,
    tertiary = PremiumAccentRed,
    background = AmoledBlack,
    surface = DarkGreySurface,
    onPrimary = AmoledBlack,
    onSecondary = PremiumWhite,
    onTertiary = PremiumWhite,
    onBackground = PremiumWhite,
    onSurface = PremiumWhite,
    outline = PremiumBorder,
    surfaceVariant = DarkCardBg
)

private val LightColorScheme = darkColorScheme(
    primary = PremiumWhite,
    secondary = PremiumSilver,
    tertiary = PremiumAccentBlue,
    background = AmoledBlack,
    surface = DarkGreySurface,
    onPrimary = AmoledBlack,
    onSecondary = PremiumWhite,
    onTertiary = PremiumWhite,
    onBackground = PremiumWhite,
    onSurface = PremiumWhite,
    outline = PremiumBorder,
    surfaceVariant = DarkCardBg
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark theme by default for premium AMOLED look
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
