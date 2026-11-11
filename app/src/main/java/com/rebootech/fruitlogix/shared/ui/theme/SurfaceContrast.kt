package com.rebootech.fruitlogix.shared.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.luminance

/**
 * True when the current content color is light, which means the composable is drawn
 * on a dark surface (dark cards, KPI tiles, dark dialogs, app bar).
 *
 * Material 3 containers (Card, Surface, AlertDialog) set [LocalContentColor] for their
 * content, so shared components can adapt their colors without extra parameters.
 * Outside any container the default content color is dark, so the light variant is used.
 */
@Composable
@ReadOnlyComposable
fun isOnDarkSurface(): Boolean = LocalContentColor.current.luminance() > 0.5f
