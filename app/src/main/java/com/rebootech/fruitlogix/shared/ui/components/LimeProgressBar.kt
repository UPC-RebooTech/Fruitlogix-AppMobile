package com.rebootech.fruitlogix.shared.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.isOnDarkSurface

/**
 * Progress bar that adapts to its surface:
 * - Dark surfaces: Lima indicator on a translucent white track.
 * - Light surfaces: Musgo indicator on a mint track (Lima is too light to read on light backgrounds).
 *
 * @param progress Progress value from 0f to 1f
 * @param trackColor Optional override for the track color
 * @param progressColor Optional override for the indicator color
 */
@Composable
fun LimeProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    trackColor: Color? = null,
    progressColor: Color? = null
) {
    val onDark = isOnDarkSurface()
    val resolvedTrack = trackColor ?: if (onDark) {
        FruitLogixTheme.colors.textOnDark.copy(alpha = 0.14f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val resolvedProgress = progressColor ?: if (onDark) {
        FruitLogixTheme.colors.primary
    } else {
        MaterialTheme.colorScheme.primary
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "LimeProgressBarAnimation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(FruitLogixTheme.shapes.Pill)
            .background(resolvedTrack)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(FruitLogixTheme.shapes.Pill)
                .background(resolvedProgress)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7FAF6)
@Composable
private fun LimeProgressBarPreview() {
    FruitLogixTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            LimeProgressBar(progress = 0.65f)
        }
    }
}
