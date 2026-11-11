package com.rebootech.fruitlogix.shared.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/*
 * FruitLogix "Cosecha" shapes (radii aligned with the landing page: 12 / 18 / 24).
 *
 * Material 3 slots:
 * - extraSmall  8dp  status tags, badges, snackbar
 * - small      12dp  text fields, chips, menus, secondary buttons
 * - medium     18dp  list cards, KPI tiles
 * - large      24dp  hero cards, FAB
 * - extraLarge 28dp  dialogs, bottom sheets
 *
 * Only the main call to action uses the full pill.
 */
val FruitLogixShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

object FruitLogixShapeTokens {
    /** Standard cards and KPI tiles */
    val Card = RoundedCornerShape(18.dp)

    /** Hero / featured card */
    val HeroCard = RoundedCornerShape(24.dp)

    /** Main CTA, nav indicator, avatar-like chips */
    val Pill = RoundedCornerShape(percent = 50)

    /** Secondary buttons */
    val Button = RoundedCornerShape(12.dp)

    /** Text fields */
    val Input = RoundedCornerShape(12.dp)

    /** Status tags and badges */
    val Tag = RoundedCornerShape(8.dp)

    /** Icon container inside cards */
    val IconBox = RoundedCornerShape(12.dp)

    /** Dialogs */
    val Dialog = RoundedCornerShape(28.dp)

    /** Bottom sheets (top corners only) */
    val BottomSheet = RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 28.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
}
