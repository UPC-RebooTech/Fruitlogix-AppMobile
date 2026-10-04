package com.rebootech.fruitlogix.shared.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Reusable Lime Accent Progress Bar component.
 *
 * @param progress Progress value from 0.0f to 1.0f
 * @param modifier Modifier to apply
 * @param height Dp height of the progress bar (default 8dp)
 * @param trackColor Track background color
 * @param progressColor Lime indicator fill color
 */
@Composable
fun LimeProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    trackColor: Color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark,
    progressColor: Color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "LimeProgressBarAnimation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Pill)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Pill)
                .background(progressColor)
        )
    }
}

@Preview
@Composable
private fun LimeProgressBarPreview() {
    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme {
        _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.LimeProgressBar(progress = 0.65f)
    }
}
