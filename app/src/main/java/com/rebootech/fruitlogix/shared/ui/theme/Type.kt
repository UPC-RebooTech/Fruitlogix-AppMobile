package com.rebootech.fruitlogix.shared.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * FruitLogix Typography Definitions.
 *
 * Headings: Poppins (FontWeight 600, 700)
 * Body & Data: Roboto (FontWeight 400, 500)
 *
 * NOTE: When TTF font files are added to `res/font/`, update these definitions to use:
 * ```
 * val PoppinsFontFamily = FontFamily(
 *     Font(R.font.poppins_semibold, FontWeight.SemiBold),
 *     Font(R.font.poppins_bold, FontWeight.Bold)
 * )
 * val RobotoFontFamily = FontFamily(
 *     Font(R.font.roboto_regular, FontWeight.Normal),
 *     Font(R.font.roboto_medium, FontWeight.Medium)
 * )
 * ```
 *
 * Files to place in `app/src/main/res/font/`:
 * - `poppins_semibold.ttf` (Poppins SemiBold 600)
 * - `poppins_bold.ttf` (Poppins Bold 700)
 * - `roboto_regular.ttf` (Roboto Regular 400)
 * - `roboto_medium.ttf` (Roboto Medium 500)
 */

val PoppinsFontFamily: FontFamily = FontFamily.SansSerif
val RobotoFontFamily: FontFamily = FontFamily.SansSerif

val FruitLogixTypography = Typography(
    // H1 screen title: Poppins 28px / line-height 36px, weight 700
    displayLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    // H2 section title: Poppins 22px / line-height 30px, weight 600
    titleLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp
    ),
    // H3 card title: Poppins 18px / line-height 24px, weight 600
    titleMedium = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    // Body large: Roboto 16px / line-height 24px, weight 400
    bodyLarge = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    // Body medium: Roboto 14px / line-height 20px, weight 400
    bodyMedium = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    // Secondary text / Body small: Roboto 12px / line-height 16px, weight 400
    bodySmall = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    // Overline / KPI caption: Roboto 11-12px, UPPERCASE, letter-spacing 0.08em, weight 500
    labelSmall = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.08.em
    )
)

object FruitLogixExtraTypography {
    // Large KPI number: Poppins 32-40px, weight 700
    val kpiNumber = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp
    )
}