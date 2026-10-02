package com.rebootech.fruitlogix.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource

/**
 * FruitLogix AppIcon composable wrapper around Material 3 Icon and painterResource.
 *
 * @param id Drawable resource ID (@DrawableRes)
 * @param contentDescription Accessibility description for the icon
 * @param modifier Modifier to apply to the Icon
 * @param tint Tint color for the icon vector, defaults to LocalContentColor.current
 */
@Composable
fun AppIcon(
    @DrawableRes id: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    Icon(
        painter = painterResource(id = id),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}
