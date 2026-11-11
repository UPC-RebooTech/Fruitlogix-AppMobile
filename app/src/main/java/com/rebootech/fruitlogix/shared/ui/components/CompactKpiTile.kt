package com.rebootech.fruitlogix.shared.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Small KPI tile (inverseSurface) for rows of 3 summary numbers at the top of a screen.
 * Caption up to 2 lines; the value shrinks to fit instead of truncating.
 *
 * @param valueColor Optional accent for the value (e.g. a critical count). Defaults to inverseOnSurface.
 */
@Composable
fun CompactKpiTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color? = null
) {
    val scheme = MaterialTheme.colorScheme
    FruitLogixCard(
        variant = CardVariant.Dark,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FruitLogixTheme.spacing.inner)
        ) {
            Text(
                text = label.uppercase(),
                style = FruitLogixTheme.typography.labelSmall,
                color = FruitLogixTheme.colors.onInverseSurfaceVariant,
                minLines = 2,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
            val style = FruitLogixTheme.typography.titleMedium
            Text(
                text = value,
                style = style,
                color = valueColor ?: scheme.inverseOnSurface,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 13.sp,
                    maxFontSize = style.fontSize,
                    stepSize = 0.5.sp
                )
            )
        }
    }
}
