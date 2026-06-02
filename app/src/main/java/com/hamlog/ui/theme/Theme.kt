package com.hamlog.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamlog.R

// ── Alexandria Design System ──────────────────────────────────────────────────
// Colours sourced 1-to-1 from the HTML Tailwind config

// Light surface tokens
val AlxSurface            = Color(0xFFFAF9FA)
val AlxBackground         = Color(0xFFFAF9FA)
val AlxSurfaceContainer   = Color(0xFFEFEDEE)
val AlxSurfaceContainerLow  = Color(0xFFF5F3F4)
val AlxSurfaceContainerHigh = Color(0xFFE9E8E9)
val AlxSurfaceContainerLowest = Color(0xFFFFFFFF)
val AlxSurfaceDim         = Color(0xFFDBDADB)
val AlxSurfaceVariant     = Color(0xFFE3E2E3)

// Primary
val AlxPrimary            = Color(0xFF094CB2)
val AlxOnPrimary          = Color(0xFFFFFFFF)
val AlxPrimaryContainer   = Color(0xFF3366CC)
val AlxOnPrimaryContainer = Color(0xFFE7EBFF)
val AlxPrimaryFixed       = Color(0xFFD9E2FF)
val AlxPrimaryFixedDim    = Color(0xFFB1C5FF)
val AlxInversePrimary     = Color(0xFFB1C5FF)

// Secondary
val AlxSecondary          = Color(0xFF5A5F63)
val AlxOnSecondary        = Color(0xFFFFFFFF)
val AlxSecondaryContainer = Color(0xFFDFE3E8)
val AlxOnSecondaryContainer = Color(0xFF606569)

// Tertiary (amber/gold)
val AlxTertiary           = Color(0xFF6D5E00)
val AlxOnTertiary         = Color(0xFFFFFFFF)
val AlxTertiaryContainer  = Color(0xFFBFAB49)
val AlxOnTertiaryContainer = Color(0xFF4A3F00)

// Outline
val AlxOutline            = Color(0xFF737784)
val AlxOutlineVariant     = Color(0xFFC3C6D5)

// Text
val AlxOnSurface          = Color(0xFF1B1C1D)
val AlxOnSurfaceVariant   = Color(0xFF434653)

// Error
val AlxError              = Color(0xFFBA1A1A)
val AlxOnError            = Color(0xFFFFFFFF)
val AlxErrorContainer     = Color(0xFFFFDAD6)
val AlxOnErrorContainer   = Color(0xFF93000A)

// Font families
val NotoSerif = FontFamily(
    Font(R.font.noto_serif, FontWeight.Normal),
    Font(R.font.noto_serif, FontWeight.Bold)
)
val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal),
    Font(R.font.space_grotesk, FontWeight.Medium),
    Font(R.font.space_grotesk, FontWeight.Bold)
)

// Dark surface tokens
val SurfaceDark  = Color(0xFF0B0D0F)
val SurfaceDark3 = Color(0xFF1C1E22)
val TextPrimary  = Color(0xFFE8EAF0)
val TextSecondary = Color(0xFFA0A4B0)
val BorderSubtle = Color(0xFF3A3E47)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB1C5FF), onPrimary = Color(0xFF001946),
    primaryContainer = Color(0xFF00419D), onPrimaryContainer = Color(0xFFD9E2FF),
    secondary = Color(0xFFC2C7CC), onSecondary = Color(0xFF2C3135),
    secondaryContainer = Color(0xFF42474B), onSecondaryContainer = Color(0xFFDFE3E8),
    tertiary = Color(0xFFDCC661), onTertiary = Color(0xFF3A3000),
    tertiaryContainer = Color(0xFF524600), onTertiaryContainer = Color(0xFFF9E37A),
    surface = SurfaceDark, onSurface = TextPrimary,
    surfaceVariant = SurfaceDark3, onSurfaceVariant = TextSecondary,
    background = SurfaceDark, onBackground = TextPrimary,
    outline = BorderSubtle, outlineVariant = BorderSubtle,
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = AlxPrimary,
    onPrimary = AlxOnPrimary,
    primaryContainer = AlxPrimaryContainer,
    onPrimaryContainer = AlxOnPrimaryContainer,
    inversePrimary = AlxInversePrimary,
    secondary = AlxSecondary,
    onSecondary = AlxOnSecondary,
    secondaryContainer = AlxSecondaryContainer,
    onSecondaryContainer = AlxOnSecondaryContainer,
    tertiary = AlxTertiary,
    onTertiary = AlxOnTertiary,
    tertiaryContainer = AlxTertiaryContainer,
    onTertiaryContainer = AlxOnTertiaryContainer,
    surface = AlxSurface,
    onSurface = AlxOnSurface,
    surfaceVariant = AlxSurfaceVariant,
    onSurfaceVariant = AlxOnSurfaceVariant,
    background = AlxBackground,
    onBackground = AlxOnSurface,
    outline = AlxOutline,
    outlineVariant = AlxOutlineVariant,
    error = AlxError,
    onError = AlxOnError,
    errorContainer = AlxErrorContainer,
    onErrorContainer = AlxOnErrorContainer
)

// ── Alexandria extra surface tokens (not in M3 ColorScheme) ──────────────────
val LocalSurfaceContainerLow    = staticCompositionLocalOf { AlxSurfaceContainerLow }
val LocalSurfaceContainer       = staticCompositionLocalOf { AlxSurfaceContainer }
val LocalSurfaceContainerHigh   = staticCompositionLocalOf { AlxSurfaceContainerHigh }
val LocalSurfaceContainerLowest = staticCompositionLocalOf { AlxSurfaceContainerLowest }

// ── Typography ────────────────────────────────────────────────────────────────
// headline → Noto Serif feel via Serif; body → default (Inter-like system sans);
// label → slightly tighter tracking (Public Sans feel via system sans)
private val AppTypography = Typography(
    headlineLarge  = TextStyle(fontFamily = NotoSerif,   fontWeight = FontWeight.Bold,     fontSize = 20.sp, lineHeight = 26.sp,  letterSpacing = (-0.5).sp),
    headlineSmall  = TextStyle(fontFamily = NotoSerif,   fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    titleLarge     = TextStyle(fontFamily = NotoSerif,   fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleMedium    = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp,  letterSpacing = 0.1.sp),
    titleSmall     = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp,  letterSpacing = 0.1.sp),
    bodyLarge      = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Normal,   fontSize = 13.sp, lineHeight = 19.sp,  letterSpacing = 0.25.sp),
    bodyMedium     = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 16.sp,  letterSpacing = 0.25.sp),
    bodySmall      = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Normal,   fontSize = 11.sp, lineHeight = 14.sp,  letterSpacing = 0.4.sp),
    labelLarge     = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium,   fontSize = 11.sp, lineHeight = 15.sp,  letterSpacing = 0.1.sp),
    labelMedium    = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium,   fontSize = 10.sp, lineHeight = 13.sp,  letterSpacing = 0.5.sp),
    labelSmall     = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium,   fontSize = 9.sp,  lineHeight = 11.sp,  letterSpacing = 0.8.sp)
)

// ── Shapes ─────────────────────────────────────────────────────────────────────
// Alexandria uses very subtle rounding: default≈2dp, lg≈4dp, xl≈8dp, full≈24dp
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(12.dp),
    large      = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

// ── Theme entry point ─────────────────────────────────────────────────────────
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
        WindowWidthSizeClass.Medium  -> 1.05f
        else                          -> 1.1f
    }

    val scaledTypography = AppTypography.copy(
        headlineLarge = AppTypography.headlineLarge.copy(fontSize = (20 * scale).sp, lineHeight = (26 * scale).sp),
        headlineSmall = AppTypography.headlineSmall.copy(fontSize = (18 * scale).sp, lineHeight = (24 * scale).sp),
        titleLarge    = AppTypography.titleLarge   .copy(fontSize = (16 * scale).sp, lineHeight = (22 * scale).sp),
        titleMedium   = AppTypography.titleMedium  .copy(fontSize = (14 * scale).sp, lineHeight = (20 * scale).sp),
        titleSmall    = AppTypography.titleSmall   .copy(fontSize = (12 * scale).sp, lineHeight = (16 * scale).sp),
        bodyLarge     = AppTypography.bodyLarge    .copy(fontSize = (13 * scale).sp, lineHeight = (19 * scale).sp),
        bodyMedium    = AppTypography.bodyMedium   .copy(fontSize = (12 * scale).sp, lineHeight = (16 * scale).sp),
        bodySmall     = AppTypography.bodySmall    .copy(fontSize = (11 * scale).sp, lineHeight = (14 * scale).sp),
        labelLarge    = AppTypography.labelLarge   .copy(fontSize = (11 * scale).sp, lineHeight = (15 * scale).sp),
        labelMedium   = AppTypography.labelMedium  .copy(fontSize = (10 * scale).sp, lineHeight = (13 * scale).sp),
        labelSmall    = AppTypography.labelSmall   .copy(fontSize = (9  * scale).sp, lineHeight = (11 * scale).sp)
    )

    CompositionLocalProvider(
        LocalWindowSizeClass provides widthClass,
        LocalSurfaceContainerLow    provides if (darkTheme) SurfaceDark3 else AlxSurfaceContainerLow,
        LocalSurfaceContainer       provides if (darkTheme) SurfaceDark3 else AlxSurfaceContainer,
        LocalSurfaceContainerHigh   provides if (darkTheme) Color(0xFF2A2C30) else AlxSurfaceContainerHigh,
        LocalSurfaceContainerLowest provides if (darkTheme) SurfaceDark else AlxSurfaceContainerLowest
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography  = scaledTypography,
            shapes      = AppShapes,
            content     = content
        )
    }
}

val LocalWindowSizeClass = staticCompositionLocalOf<WindowWidthSizeClass> {
    WindowWidthSizeClass.Compact
}

