package com.hamlog.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Cyan400 = Color(0xFF00E5FF)
val Cyan900 = Color(0xFF00363D)
val Amber400 = Color(0xFFFFB300)
val Amber950 = Color(0xFF3D2000)
val SurfaceDark = Color(0xFF0B0D0F)
val SurfaceDark3 = Color(0xFF1C1E22)
val TextPrimary = Color(0xFFE8EAF0)
val TextSecondary = Color(0xFFA0A4B0)
val BorderSubtle = Color(0xFF2A2D33)

private val DarkColorScheme = darkColorScheme(
    primary = Cyan400, onPrimary = Color(0xFF001A1F), primaryContainer = Cyan900, onPrimaryContainer = Color(0xFFB2EBF2),
    secondary = Amber400, onSecondary = Color(0xFF1A0E00), secondaryContainer = Amber950, onSecondaryContainer = Color(0xFFFFECB3),
    tertiary = Color(0xFF22C55E), onTertiary = Color(0xFF052E16), tertiaryContainer = Color(0xFF14532D), onTertiaryContainer = Color(0xFFBBF7D0),
    surface = SurfaceDark, onSurface = TextPrimary, surfaceVariant = SurfaceDark3, onSurfaceVariant = TextSecondary,
    background = SurfaceDark, onBackground = TextPrimary, outline = BorderSubtle, outlineVariant = BorderSubtle,
    error = Color(0xFFFF5252), onError = Color.Black, errorContainer = Color(0xFF3B1C1A), onErrorContainer = Color(0xFFFECACA)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00838F), onPrimary = Color.White, primaryContainer = Color(0xFFB2EBF2), onPrimaryContainer = Color(0xFF001F24),
    secondary = Color(0xFFC56000), onSecondary = Color.White, secondaryContainer = Color(0xFFFFDDB3), onSecondaryContainer = Color(0xFF301400),
    tertiary = Color(0xFF16A34A), onTertiary = Color.White, tertiaryContainer = Color(0xFFDCFCE7), onTertiaryContainer = Color(0xFF052E16),
    surface = Color(0xFFF8F9FB), onSurface = Color(0xFF1A1C20), surfaceVariant = Color(0xFFEDEFF3), onSurfaceVariant = Color(0xFF5C5F68),
    background = Color(0xFFF8F9FB), onBackground = Color(0xFF1A1C20), outline = Color(0xFFD0D3DB), outlineVariant = Color(0xFFD0D3DB),
    error = Color(0xFFD32F2F), onError = Color.White, errorContainer = Color(0xFFFEE2E2), onErrorContainer = Color(0xFF450A0A)
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = (-0.5).sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.15.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 15.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 10.sp, lineHeight = 13.sp, letterSpacing = 0.4.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 13.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 9.sp, lineHeight = 11.sp, letterSpacing = 0.5.sp)
)

private val AppShapes = Shapes(extraSmall = RoundedCornerShape(6.dp), small = RoundedCornerShape(10.dp), medium = RoundedCornerShape(12.dp), large = RoundedCornerShape(16.dp), extraLarge = RoundedCornerShape(20.dp))

@Composable
fun HamLogTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val config = LocalConfiguration.current
    val widthClass = when {
        config.screenWidthDp < 600 -> WindowWidthSizeClass.Compact
        config.screenWidthDp < 840 -> WindowWidthSizeClass.Medium
        else -> WindowWidthSizeClass.Expanded
    }
    val scale = when (widthClass) {
        WindowWidthSizeClass.Compact -> 1.0f
        WindowWidthSizeClass.Medium -> 1.05f
        else -> 1.1f
    }

    val scaledTypography = AppTypography.copy(
        headlineLarge = AppTypography.headlineLarge.copy(fontSize = (20 * scale).sp, lineHeight = (26 * scale).sp),
        headlineSmall = AppTypography.headlineSmall.copy(fontSize = (18 * scale).sp, lineHeight = (24 * scale).sp),
        titleLarge = AppTypography.titleLarge.copy(fontSize = (16 * scale).sp, lineHeight = (22 * scale).sp),
        titleMedium = AppTypography.titleMedium.copy(fontSize = (13 * scale).sp, lineHeight = (18 * scale).sp),
        titleSmall = AppTypography.titleSmall.copy(fontSize = (11 * scale).sp, lineHeight = (16 * scale).sp),
        bodyLarge = AppTypography.bodyLarge.copy(fontSize = (12 * scale).sp, lineHeight = (17 * scale).sp),
        bodyMedium = AppTypography.bodyMedium.copy(fontSize = (11 * scale).sp, lineHeight = (15 * scale).sp),
        bodySmall = AppTypography.bodySmall.copy(fontSize = (10 * scale).sp, lineHeight = (13 * scale).sp),
        labelLarge = AppTypography.labelLarge.copy(fontSize = (12 * scale).sp, lineHeight = (16 * scale).sp),
        labelMedium = AppTypography.labelMedium.copy(fontSize = (10 * scale).sp, lineHeight = (13 * scale).sp),
        labelSmall = AppTypography.labelSmall.copy(fontSize = (9 * scale).sp, lineHeight = (11 * scale).sp)
    )

    CompositionLocalProvider(LocalWindowSizeClass provides widthClass) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = scaledTypography,
            shapes = AppShapes,
            content = content
        )
    }
}

val LocalWindowSizeClass = staticCompositionLocalOf<WindowWidthSizeClass> {
    WindowWidthSizeClass.Compact
}
