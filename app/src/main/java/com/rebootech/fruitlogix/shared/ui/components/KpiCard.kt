package com.rebootech.fruitlogix.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * KPI tile on inverseSurface (dark in the light theme).
 *
 * Layout, top to bottom: icon box (+ optional status pill on the right), caption
 * (up to 2 lines, full width), value (full width, shrinks to fit instead of truncating),
 * optional progress and footer.
 */
@Composable
fun KpiCard(
    caption: String,
    value: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    deltaPill: (@Composable () -> Unit)? = null,
    progress: Float? = null,
    footer: (@Composable () -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = scheme.inverseSurface,
            contentColor = scheme.inverseOnSurface
        )
    ) {
        Column(modifier = Modifier.padding(FruitLogixTheme.spacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(FruitLogixTheme.shapes.IconBox)
                        .background(scheme.inversePrimary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
                deltaPill?.invoke()
            }

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))

            Text(
                text = caption.uppercase(),
                style = FruitLogixTheme.typography.labelSmall,
                color = colors.onInverseSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))

            val numberStyle = FruitLogixTheme.extraTypography.kpiNumber
            Text(
                text = value,
                style = numberStyle,
                color = scheme.inverseOnSurface,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 18.sp,
                    maxFontSize = numberStyle.fontSize,
                    stepSize = 1.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )

            progress?.let {
                Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
                LimeProgressBar(progress = it)
            }

            footer?.let {
                Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
                it()
            }
        }
    }
}

@Preview
@Composable
private fun KpiCardPreview() {
    FruitLogixTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                caption = "Aprobación de calidad",
                value = "98%",
                icon = {
                    AppIcon(
                        id = R.drawable.ic_shield_check,
                        contentDescription = null,
                        tint = FruitLogixTheme.colors.primary
                    )
                },
                deltaPill = { StatusBadge(text = "Óptimo", type = StatusBadgeType.SUCCESS) },
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                caption = "Pagos pendientes",
                value = "S/ 18,450",
                icon = {
                    AppIcon(
                        id = R.drawable.ic_invoices,
                        contentDescription = null,
                        tint = FruitLogixTheme.colors.primary
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
