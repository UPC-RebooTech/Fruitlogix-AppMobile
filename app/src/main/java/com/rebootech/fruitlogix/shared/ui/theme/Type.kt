@file:OptIn(ExperimentalTextApi::class)

package com.rebootech.fruitlogix.shared.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.rebootech.fruitlogix.R

/*
 * FruitLogix "Cosecha" typography (option A, "Continuidad").
 *
 * - Poppins (600/700): display, headline and title styles. Same as the landing page.
 * - Atkinson Hyperlegible Next (variable): body and labels. Designed for low vision;
 *   clear I/l/1, O/0 and rn/m, good for outdoor use and older users.
 * - Atkinson Hyperlegible Mono (variable): lot codes, plates, IDs, temperatures and amounts.
 *
 * Fonts are bundled in res/font (the app works offline). Licenses: /licenses/fonts.
 * Minimum size for useful text is 13sp; 12sp only for uppercase overlines.
 */

val PoppinsFontFamily: FontFamily = FontFamily(
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

private fun atkinson(weight: FontWeight) = Font(
    resId = R.font.atkinson_hyperlegible_next,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
)

private fun atkinsonMono(weight: FontWeight) = Font(
    resId = R.font.atkinson_hyperlegible_mono,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
)

val AtkinsonFontFamily: FontFamily = FontFamily(
    atkinson(FontWeight.Normal),
    atkinson(FontWeight.Medium),
    atkinson(FontWeight.SemiBold),
    atkinson(FontWeight.Bold)
)

val AtkinsonMonoFontFamily: FontFamily = FontFamily(
    atkinsonMono(FontWeight.Normal),
    atkinsonMono(FontWeight.Medium),
    atkinsonMono(FontWeight.SemiBold)
)

/** Legacy alias: body font used across existing screens. Now Atkinson Hyperlegible Next. */
val RobotoFontFamily: FontFamily = AtkinsonFontFamily

val FruitLogixTypography = Typography(
    // Hero numbers and big screen titles
    displayLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.01).em
    ),
    displayMedium = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.01).em
    ),
    displaySmall = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.01).em
    ),
    // Screen titles
    headlineLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    // Section titles
    headlineSmall = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp
    ),
    // Top app bar
    titleLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    // Card titles
    titleMedium = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    // List rows, subtitles
    titleSmall = TextStyle(
        fontFamily = AtkinsonFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    // Main reading text
    bodyLarge = TextStyle(
        fontFamily = AtkinsonFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 26.sp
    ),
    // Default text
    bodyMedium = TextStyle(
        fontFamily = AtkinsonFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    // Supporting text (minimum for useful content)
    bodySmall = TextStyle(
        fontFamily = AtkinsonFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.01.em
    ),
    // Buttons
    labelLarge = TextStyle(
        fontFamily = AtkinsonFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.01.em
    ),
    // Chips, status tags
    labelMedium = TextStyle(
        fontFamily = AtkinsonFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.02.em
    ),
    // Uppercase overlines (apply uppercase in the component)
    labelSmall = TextStyle(
        fontFamily = AtkinsonFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.08.em
    )
)

object FruitLogixExtraTypography {
    /** Large KPI number on tiles */
    val kpiNumber = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 42.sp
    )

    /** Lot codes, plates, order IDs, temperatures, amounts */
    val dataMono = TextStyle(
        fontFamily = AtkinsonMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp
    )

    /** Timestamps and IDs inside list rows */
    val dataMonoSmall = TextStyle(
        fontFamily = AtkinsonMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )

    /** Live readings (temperature, humidity) shown large */
    val readingMono = TextStyle(
        fontFamily = AtkinsonMonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 38.sp
    )
}
