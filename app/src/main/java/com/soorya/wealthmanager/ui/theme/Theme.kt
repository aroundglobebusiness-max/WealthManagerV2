package com.soorya.wealthmanager.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.core.view.WindowCompat

val WealthTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 48.sp, letterSpacing = (-1.5).sp, color = InkBlack),
    displayMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 36.sp, letterSpacing = (-1).sp, color = InkBlack),
    headlineLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 28.sp, color = InkBlack),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = InkBlack),
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = InkBlack),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = InkBlack),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, color = InkBlack),
    titleSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, color = InkDark),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, color = InkDark),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, color = InkLight),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, color = InkMuted),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp, color = InkBlack),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, color = InkLight),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 9.sp, letterSpacing = 0.8.sp, color = InkMuted),
)

private val WealthColorScheme = lightColorScheme(
    primary = InkBlack,
    onPrimary = PearlLight,
    primaryContainer = InkDark,
    onPrimaryContainer = PearlLight,
    secondary = InkMedium,
    onSecondary = PearlLight,
    background = Pearl,
    onBackground = InkBlack,
    surface = PearlLight,
    onSurface = InkBlack,
    surfaceVariant = PearlSurface,
    onSurfaceVariant = InkLight,
    outline = PearlBorder,
    outlineVariant = InkFaint,
    error = MonoRed,
    onError = PearlLight,
)

@Composable
fun WealthManagerTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color(0xFFFAFAF8).toArgb()
            window.navigationBarColor = Color(0xFFFAFAF8).toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }
    }
    MaterialTheme(
        colorScheme = WealthColorScheme,
        typography = WealthTypography,
        content = content
    )
}
