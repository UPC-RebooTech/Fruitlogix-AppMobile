package com.rebootech.fruitlogix.shared.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * FruitLogix spacing scale on an 8dp grid.
 * [xxs] (4dp) and [inner] (12dp) are only for spacing inside components
 * (icon to text, chip padding); layouts use the 8dp steps.
 */
object Spacing {
    val xxs: Dp = 4.dp
    val xs: Dp = 8.dp
    val inner: Dp = 12.dp
    val sm: Dp = 16.dp
    val md: Dp = 24.dp
    val lg: Dp = 32.dp
    val xl: Dp = 48.dp
}

/**
 * Touch and control sizes. Field actions (scan, save inspection, accept order)
 * use [fieldTouch] because they are often used outdoors or with gloves.
 */
object Sizing {
    val minTouch: Dp = 48.dp
    val fieldTouch: Dp = 56.dp
    val buttonHeight: Dp = 56.dp
    val iconSm: Dp = 20.dp
    val icon: Dp = 24.dp
    val iconBox: Dp = 40.dp
}
