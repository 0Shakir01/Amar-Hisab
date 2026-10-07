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
    primary = FinancialGreenLight,
    onPrimary = Color(0xFF003822),
    primaryContainer = FinancialGreenDark,
    onPrimaryContainer = Color(0xFFA7F3D0),

    secondary = Color(0xFF90CAF9),
    onSecondary = Color(0xFF0D253F),
    secondaryContainer = FinancialNavySecondary,
    onSecondaryContainer = Color(0xFFD6E4FF),

    tertiary = Color(0xFF6EE7B7),
    onTertiary = Color(0xFF003822),

    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
    error = ExpenseRed
)

private val LightColorScheme = lightColorScheme(
    primary = FinancialGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = FinancialGreenContainer,
    onPrimaryContainer = FinancialOnGreenContainer,

    secondary = FinancialNavyPrimary,
    onSecondary = Color.White,
    secondaryContainer = FinancialNavyContainer,
    onSecondaryContainer = FinancialOnNavyContainer,

    tertiary = Color(0xFF0284C7),
    onTertiary = Color.White,

    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = ExpenseRed
)

@Composable
fun AmarHisabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We enforce our branded Green, White, and Dark-Blue theme for financial clarity
    dynamicColor: Boolean = false,
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
