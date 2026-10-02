package com.rebootech.fruitlogix.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Access object for FruitLogix Design System Tokens within Composable hierarchy.
 */
object FruitLogixTheme {
    val colors: FruitLogixColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFruitLogixColors.current

    val typography: androidx.compose.material3.Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val spacing: Spacing
        get() = Spacing

    val shapes: FruitLogixShapeTokens
        get() = FruitLogixShapeTokens
}

/**
 * FruitLogix Main Design System Theme.
 *
 * Light mint background (#E8F3E3) is used for the app background.
 * Dark surfaces (#1F2D23) are used by cards, KPI tiles, and bottom sheets.
 */
@Composable
fun FruitLogixTheme(
    content: @Composable () -> Unit
) {
    val fruitLogixColors = FruitLogixColors()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let { win ->
                win.statusBarColor = ColorAppbar.toArgb()
                win.navigationBarColor = ColorAppbar.toArgb()
                WindowCompat.getInsetsController(win, view).isAppearanceLightStatusBars = false
            }
        }
    }

    CompositionLocalProvider(
        LocalFruitLogixColors provides fruitLogixColors
    ) {
        MaterialTheme(
            colorScheme = FruitLogixColorScheme,
            typography = FruitLogixTypography,
            shapes = FruitLogixShapes,
            content = content
        )
    }
}