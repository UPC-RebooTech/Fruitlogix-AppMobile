package com.rebootech.fruitlogix.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * FruitLogix Design System Shapes
 * - Cards: 24dp
 * - Chips & Buttons: Pill (50% fully rounded)
 * - Inputs: 16dp
 * - Bottom Sheet: 28dp top corners
 */
val FruitLogixShapes = Shapes(
    extraSmall = RoundedCornerShape(percent = 50),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
    extraLarge = RoundedCornerShape(percent = 50)
)

object FruitLogixShapeTokens {
    val Card = RoundedCornerShape(24.dp)
    val Pill = RoundedCornerShape(percent = 50)
    val Input = RoundedCornerShape(16.dp)
    val BottomSheet = RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 28.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
}

