package com.rebootech.fruitlogix.shared.ui.theme

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
    val bg: Color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorBg,
    val surfaceDark: Color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorSurfaceDark,
    val appbar: Color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorAppbar,
    val primary: Color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorPrimary,
    val onPrimary: Color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorOnPrimary,
    val textOnLight: Color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorTextOnLight,
    val textOnDark: Color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorTextOnDark,
    val textMuted: Color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorTextMuted,
    val danger: Color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorDanger,
    val dangerStrong: Color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorDangerStrong,
    val success: Color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorSuccess,
    val warning: Color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorWarning,
    val info: Color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorInfo
)

val LocalFruitLogixColors = staticCompositionLocalOf { _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixColors() }

// ==========================================
// 3. Material 3 ColorScheme Mapping
// ==========================================

val FruitLogixColorScheme: ColorScheme = darkColorScheme(
    primary = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorPrimary,
    onPrimary = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorOnPrimary,
    primaryContainer = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorAppbar,
    onPrimaryContainer = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorPrimary,
    secondary = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorPrimary,
    onSecondary = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorOnPrimary,
    background = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorBg,
    onBackground = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorTextOnLight,
    surface = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorSurfaceDark,
    onSurface = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorTextOnDark,
    surfaceVariant = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorAppbar,
    onSurfaceVariant = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorTextMuted,
    error = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorDanger,
    onError = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorTextOnDark,
    outline = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.ColorTextMuted
)