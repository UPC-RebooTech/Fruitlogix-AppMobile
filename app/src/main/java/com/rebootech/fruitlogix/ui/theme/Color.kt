package com.rebootech.fruitlogix.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ==========================================
// 1. Color Token Constants (Single Source of Truth)
// ==========================================

/** App background, pale mint */
val ColorBg = Color(0xFFE8F3E3)

/** Cards, KPI tiles, bottom sheets */
val ColorSurfaceDark = Color(0xFF1F2D23)

/** Top app bar, bottom navigation */
val ColorAppbar = Color(0xFF2D3F33)

/** Primary lime: primary buttons, active tab, highlights, progress bars */
val ColorPrimary = Color(0xFFC8E060)

/** Text / icons on lime accent */
val ColorOnPrimary = Color(0xFF1F2D23)

/** Main text on light background */
val ColorTextOnLight = Color(0xFF1F2D23)

/** Main text on dark background */
val ColorTextOnDark = Color(0xFFFFFFFF)

/** Secondary text on dark surfaces */
val ColorTextMuted = Color(0xFFA9C2AE)

/** Critical alert card background */
val ColorDanger = Color(0xFF8B2A2A)

/** Critical numbers, badges, overdue */
val ColorDangerStrong = Color(0xFFFF5A5A)

/** On time, delivered, online */
val ColorSuccess = Color(0xFF4ADE80)

/** Customs delay, pending, caution */
val ColorWarning = Color(0xFFF5B800)

/** Informational notices */
val ColorInfo = Color(0xFF4C8DFF)

// ==========================================
// 2. Extended FruitLogix Color Scheme
// ==========================================

@Immutable
data class FruitLogixColors(
    val bg: Color = ColorBg,
    val surfaceDark: Color = ColorSurfaceDark,
    val appbar: Color = ColorAppbar,
    val primary: Color = ColorPrimary,
    val onPrimary: Color = ColorOnPrimary,
    val textOnLight: Color = ColorTextOnLight,
    val textOnDark: Color = ColorTextOnDark,
    val textMuted: Color = ColorTextMuted,
    val danger: Color = ColorDanger,
    val dangerStrong: Color = ColorDangerStrong,
    val success: Color = ColorSuccess,
    val warning: Color = ColorWarning,
    val info: Color = ColorInfo
)

val LocalFruitLogixColors = staticCompositionLocalOf { FruitLogixColors() }

// ==========================================
// 3. Material 3 ColorScheme Mapping
// ==========================================

val FruitLogixColorScheme: ColorScheme = darkColorScheme(
    primary = ColorPrimary,
    onPrimary = ColorOnPrimary,
    primaryContainer = ColorAppbar,
    onPrimaryContainer = ColorPrimary,
    secondary = ColorPrimary,
    onSecondary = ColorOnPrimary,
    background = ColorBg,
    onBackground = ColorTextOnLight,
    surface = ColorSurfaceDark,
    onSurface = ColorTextOnDark,
    surfaceVariant = ColorAppbar,
    onSurfaceVariant = ColorTextMuted,
    error = ColorDanger,
    onError = ColorTextOnDark,
    outline = ColorTextMuted
)