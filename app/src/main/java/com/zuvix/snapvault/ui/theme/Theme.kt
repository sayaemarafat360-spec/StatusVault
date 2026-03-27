package com.snaphubpro.zuvixapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Color definitions
val Background = Color(0xFF0F0F0F)
val Surface = Color(0xFF1A1A1A)
val SurfaceVariant = Color(0xFF252525)
val Accent = Color(0xFF25D366)
val AccentDark = Color(0xFF1DA851)
val AccentLight = Color(0xFF4DE08A)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB0B0B0)
val TextTertiary = Color(0xFF707070)
val Success = Color(0xFF4CAF50)
val Error = Color(0xFFF44336)
val Warning = Color(0xFFFF9800)
val Info = Color(0xFF2196F3)
val NewBadge = Color(0xFF25D366)
val VideoOverlay = Color(0x80000000)
val Ripple = Color(0x20FFFFFF)
val Divider = Color(0x1AFFFFFF)
val PremiumStart = Color(0xFFFFD700)
val PremiumEnd = Color(0xFFFFA500)

private val DarkColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = Color.Black,
    primaryContainer = AccentDark,
    onPrimaryContainer = AccentLight,
    secondary = AccentLight,
    onSecondary = Color.Black,
    tertiary = PremiumStart,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = Error,
    onError = Color.White,
    outline = Divider,
    outlineVariant = SurfaceVariant
)

@Composable
fun SnapVaultTheme(
    darkTheme: Boolean = true, // Always dark theme for this app
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Background.toArgb()
            window.navigationBarColor = Background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
