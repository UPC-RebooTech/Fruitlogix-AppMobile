package com.rebootech.fruitlogix.shared.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/*
 * FruitLogix "Cosecha" design system: color tokens (light theme).
 *
 * Brand values are shared with the landing page (Fruitlogix-website-v2):
 * Lima #D4E952, Bosque #192E21, Bosque profundo #13241A, Musgo #406C47, Menta #DFEBE0.
 *
 * Rules:
 * - Lima is the fill of the main call to action (primaryContainer), never text on light surfaces.
 * - Musgo (primary) is the readable accent for links, focus, selection and progress.
 * - Glaciar (tertiary) is reserved for cold chain, sensors and informational content.
 * - Status always combines container color + icon + text; never color alone.
 */

// ==========================================
// 1. Brand palette (raw values)
// ==========================================

val Lima = Color(0xFFD4E952)
val LimaSoft = Color(0xFFE9F6AE)
val Bosque = Color(0xFF192E21)
val BosqueProfundo = Color(0xFF13241A)
val Musgo = Color(0xFF406C47)
val Menta = Color(0xFFDFEBE0)
val Glaciar = Color(0xFF00676B)
val GlaciarSoft = Color(0xFFC8EEF0)
val OnGlaciarSoft = Color(0xFF002022)

// Neutrals (leaf-tinted)
val Neutral0 = Color(0xFFFFFFFF)
val Neutral10 = Color(0xFFF7FAF6)
val Neutral20 = Color(0xFFF0F5EF)
val Neutral30 = Color(0xFFE8F1E6)
val Neutral40 = Color(0xFFDFEBE0)
val Neutral50 = Color(0xFFD5E3D6)
val NeutralVariant = Color(0xFF4A5A4F)
val NeutralOutline = Color(0xFF74857A)
val NeutralOutlineVariant = Color(0xFFC9D6CB)

// Semantic: Maduro (success), Mango (attention), Granada (error)
val Maduro = Color(0xFF2D7A3F)
val MaduroContainer = Color(0xFFCFF3D3)
val OnMaduroContainer = Color(0xFF0B3A17)
val Mango = Color(0xFF8A5300)
val MangoContainer = Color(0xFFFFE2B8)
val OnMangoContainer = Color(0xFF3D2400)
val Granada = Color(0xFFC2302F)
val GranadaContainer = Color(0xFFFFDAD5)
val OnGranadaContainer = Color(0xFF5C0B0A)

// ==========================================
// 2. Legacy token constants (kept so existing screens compile;
//    values updated to the Cosecha palette. Prefer MaterialTheme roles in new code.)
// ==========================================

/** App background */
val ColorBg = Neutral10

/** Dark cards, KPI tiles, bottom sheets on dark */
val ColorSurfaceDark = Bosque

/** Top app bar, bottom navigation */
val ColorAppbar = BosqueProfundo

/** Lima: main CTA fill, active tab, highlights, progress on dark */
val ColorPrimary = Lima

/** Text / icons on Lima */
val ColorOnPrimary = BosqueProfundo

/** Main text on light background */
val ColorTextOnLight = BosqueProfundo

/** Main text on dark background */
val ColorTextOnDark = Color(0xFFFFFFFF)

/** Secondary text on dark surfaces */
val ColorTextMuted = Color(0xFFA9C2AC)

/** Secondary text on light surfaces */
val ColorTextMutedLight = NeutralVariant

/** Critical alert card background (dark contexts, white text) */
val ColorDanger = Color(0xFF8E2422)

/** Critical numbers and badges on dark surfaces */
val ColorDangerStrong = Color(0xFFFF6B5E)

/** Success accent on dark surfaces */
val ColorSuccess = Color(0xFF6FD98A)

/** Attention accent on dark surfaces */
val ColorWarning = Color(0xFFF2B544)

/** Info / cold chain accent on dark surfaces */
val ColorInfo = Color(0xFF7DD5DA)

/** Light surface for cards and containers */
val ColorSurfaceLight = Neutral20

/** Mint tint for chips, quick actions, secondary light cards */
val ColorSurfaceTint = Menta

/** Pure white */
val ColorWhite = Neutral0

// ==========================================
// 3. Extended FruitLogix colors (CompositionLocal)
// ==========================================

@Immutable
data class FruitLogixColors(
    // Legacy fields (existing screens)
    val bg: Color = ColorBg,
    val surfaceDark: Color = ColorSurfaceDark,
    val appbar: Color = ColorAppbar,
    val primary: Color = ColorPrimary,
    val onPrimary: Color = ColorOnPrimary,
    val textOnLight: Color = ColorTextOnLight,
    val textOnDark: Color = ColorTextOnDark,
    val textMuted: Color = ColorTextMuted,
    val textMutedLight: Color = ColorTextMutedLight,
    val danger: Color = ColorDanger,
    val dangerStrong: Color = ColorDangerStrong,
    val success: Color = ColorSuccess,
    val warning: Color = ColorWarning,
    val info: Color = ColorInfo,
    val surfaceLight: Color = ColorSurfaceLight,
    val surfaceTint: Color = ColorSurfaceTint,
    val white: Color = ColorWhite,

    /** Secondary text on inverseSurface (dark highlighted cards, KPI tiles) */
    val onInverseSurfaceVariant: Color = ColorTextMuted,

    // Semantic status on light surfaces (container + content + text on container)
    val successContent: Color = Maduro,
    val successContainer: Color = MaduroContainer,
    val onSuccessContainer: Color = OnMaduroContainer,
    val attentionContent: Color = Mango,
    val attentionContainer: Color = MangoContainer,
    val onAttentionContainer: Color = OnMangoContainer,
    val coldContent: Color = Glaciar,
    val coldContainer: Color = GlaciarSoft,
    val onColdContainer: Color = OnGlaciarSoft
)

val LocalFruitLogixColors = staticCompositionLocalOf { FruitLogixColors() }

// ==========================================
// 4. Material 3 ColorScheme (light)
// ==========================================

val FruitLogixColorScheme: ColorScheme = lightColorScheme(
    primary = Musgo,
    onPrimary = Neutral0,
    primaryContainer = Lima,
    onPrimaryContainer = BosqueProfundo,
    inversePrimary = Lima,

    secondary = Bosque,
    onSecondary = Neutral0,
    secondaryContainer = Menta,
    onSecondaryContainer = BosqueProfundo,

    tertiary = Glaciar,
    onTertiary = Neutral0,
    tertiaryContainer = GlaciarSoft,
    onTertiaryContainer = OnGlaciarSoft,

    error = Granada,
    onError = Neutral0,
    errorContainer = GranadaContainer,
    onErrorContainer = OnGranadaContainer,

    background = Neutral10,
    onBackground = BosqueProfundo,
    surface = Neutral10,
    onSurface = BosqueProfundo,
    surfaceVariant = Neutral40,
    onSurfaceVariant = NeutralVariant,
    surfaceTint = Musgo,
    surfaceBright = Neutral0,
    surfaceDim = Neutral50,
    surfaceContainerLowest = Neutral0,
    surfaceContainerLow = Neutral20,
    surfaceContainer = Neutral30,
    surfaceContainerHigh = Neutral40,
    surfaceContainerHighest = Neutral50,

    inverseSurface = Bosque,
    inverseOnSurface = Color(0xFFEEF5EC),

    outline = NeutralOutline,
    outlineVariant = NeutralOutlineVariant,
    scrim = Color(0xFF000000)
)
