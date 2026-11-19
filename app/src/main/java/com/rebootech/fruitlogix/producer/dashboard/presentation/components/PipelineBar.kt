package com.rebootech.fruitlogix.producer.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.dashboard.domain.model.PipelineCount
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

private data class PipelineSegment(
    val key: String,
    val label: String,
    val count: Int,
    val color: Color
)

/**
 * Order flow card (light list card): proportional bar + 2x2 legend.
 * Colors follow the status semantics: pending = Mango, in preparation = neutral,
 * in transit = Glaciar, delivered = Maduro.
 */
@Composable
fun PipelineBar(
    pipeline: PipelineCount,
    onSegmentClick: (String) -> Unit,
    onNavigateOrdersClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors

    val segments = listOf(
        PipelineSegment("PENDING", stringResource(R.string.producer_orders_chip_pending), pipeline.pending, colors.attentionContent),
        PipelineSegment("IN_PREPARATION", stringResource(R.string.producer_orders_chip_in_preparation), pipeline.inPrep, scheme.outline),
        PipelineSegment("IN_TRANSIT", stringResource(R.string.producer_orders_chip_in_transit), pipeline.inTransit, colors.coldContent),
        PipelineSegment("DELIVERED", stringResource(R.string.producer_orders_chip_delivered), pipeline.delivered, colors.successContent)
    )

    FruitLogixCard(
        variant = CardVariant.White,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(FruitLogixTheme.spacing.sm)) {
            CardHeader(
                title = stringResource(R.string.producer_home_pipeline_title),
                subtitle = pluralStringResource(
                    R.plurals.producer_home_pipeline_active,
                    pipeline.totalActive,
                    pipeline.totalActive
                ),
                actionDescription = stringResource(R.string.producer_home_cd_view_orders),
                onAction = onNavigateOrdersClick
            )

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.sm))

            val total = segments.sumOf { it.count }.coerceAtLeast(1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(FruitLogixTheme.shapes.Pill)
                    .background(scheme.surfaceContainerHigh),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                segments.filter { it.count > 0 }.forEach { segment ->
                    Box(
                        modifier = Modifier
                            .weight(segment.count.toFloat() / total)
                            .fillMaxHeight()
                            .background(segment.color)
                            .clickable { onSegmentClick(segment.key) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.sm))

            segments.chunked(2).forEachIndexed { index, row ->
                if (index > 0) Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)
                ) {
                    row.forEach { segment ->
                        PipelineLegendTile(
                            segment = segment,
                            onClick = { onSegmentClick(segment.key) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PipelineLegendTile(
    segment: PipelineSegment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = FruitLogixTheme.sizing.minTouch)
            .clip(FruitLogixTheme.shapes.Button)
            .background(scheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(horizontal = FruitLogixTheme.spacing.inner, vertical = FruitLogixTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(segment.color)
        )
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
        Text(
            text = segment.label,
            style = FruitLogixTheme.typography.labelMedium,
            color = scheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xxs))
        Text(
            text = segment.count.toString(),
            style = FruitLogixTheme.typography.titleSmall,
            color = scheme.onSurface
        )
    }
}

/**
 * Title + subtitle on the left, round arrow action on the right (48dp touch target).
 * Shared by the light list cards of the Home screen.
 */
@Composable
internal fun CardHeader(
    title: String,
    subtitle: String?,
    actionDescription: String?,
    onAction: (() -> Unit)?
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = FruitLogixTheme.typography.titleMedium,
                color = scheme.onSurface
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = FruitLogixTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }
        if (onAction != null) {
            Box(
                modifier = Modifier
                    .size(FruitLogixTheme.sizing.minTouch)
                    .clip(CircleShape)
                    .clickable(onClick = onAction),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    id = R.drawable.ic_arrow_forward,
                    contentDescription = actionDescription,
                    tint = scheme.primary,
                    modifier = Modifier.size(FruitLogixTheme.sizing.icon)
                )
            }
        }
    }
}
