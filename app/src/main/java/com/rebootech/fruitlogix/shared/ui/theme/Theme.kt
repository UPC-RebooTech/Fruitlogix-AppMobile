package com.rebootech.fruitlogix.shared.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

object FruitLogixTheme {
    val colors: FruitLogixColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFruitLogixColors.current

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val spacing: Spacing
        get() = Spacing

    val shapes: FruitLogixShapeTokens
        get() = FruitLogixShapeTokens

    val sizing: Sizing
        get() = Sizing

    val extraTypography: FruitLogixExtraTypography
        get() = FruitLogixExtraTypography
}

/**
 * FruitLogix theme. Light only for now ("Cosecha" design system).
 * System bars use Bosque profundo with light icons, like the landing page header.
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