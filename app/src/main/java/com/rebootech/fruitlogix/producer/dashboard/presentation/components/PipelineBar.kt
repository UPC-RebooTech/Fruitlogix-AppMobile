package com.rebootech.fruitlogix.producer.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.dashboard.domain.model.PipelineCount
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.theme.ColorPrimary
import com.rebootech.fruitlogix.shared.ui.theme.ColorSuccess
import com.rebootech.fruitlogix.shared.ui.theme.ColorWarning
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

private val ColorInPrepSage = Color(0xFFA5C3A5)

@Composable
fun PipelineBar(
    pipeline: PipelineCount,
    onSegmentClick: (String) -> Unit,
    onNavigateOrdersClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FruitLogixCard(
        variant = CardVariant.White,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Flujo de pedidos",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        style = FruitLogixTheme.typography.titleMedium,
                        color = FruitLogixTheme.colors.textOnLight
                    )
                    Text(
                        text = "${pipeline.totalActive} pedidos activos",
                        style = FruitLogixTheme.typography.bodySmall,
                        color = FruitLogixTheme.colors.textMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onNavigateOrdersClick),
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(
                        id = R.drawable.ic_arrow_forward,
                        contentDescription = "Ver pedidos",
                        tint = FruitLogixTheme.colors.textOnLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Proportional Segmented Bar
            val total = (pipeline.pending + pipeline.inPrep + pipeline.inTransit + pipeline.delivered).coerceAtLeast(1)
            val wPending = pipeline.pending.toFloat() / total
            val wInPrep = pipeline.inPrep.toFloat() / total
            val wInTransit = pipeline.inTransit.toFloat() / total
            val wDelivered = pipeline.delivered.toFloat() / total

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(FruitLogixTheme.colors.bg),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (wPending > 0) {
                    Box(
                        modifier = Modifier
                            .weight(wPending)
                            .fillMaxHeight()
                            .background(ColorWarning)
                            .clickable { onSegmentClick("PENDING") }
                    )
                }
                if (wInPrep > 0) {
                    Box(
                        modifier = Modifier
                            .weight(wInPrep)
                            .fillMaxHeight()
                            .background(ColorInPrepSage)
                            .clickable { onSegmentClick("IN_PREPARATION") }
                    )
                }
                if (wInTransit > 0) {
                    Box(
                        modifier = Modifier
                            .weight(wInTransit)
                            .fillMaxHeight()
                            .background(ColorPrimary)
                            .clickable { onSegmentClick("IN_TRANSIT") }
                    )
                }
                if (wDelivered > 0) {
                    Box(
                        modifier = Modifier
                            .weight(wDelivered)
                            .fillMaxHeight()
                            .background(ColorSuccess)
                            .clickable { onSegmentClick("DELIVERED") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2x2 Legend with Tint tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PipelineLegendTile(
                    label = "Pendientes",
                    count = pipeline.pending,
                    dotColor = ColorWarning,
                    onClick = { onSegmentClick("PENDING") },
                    modifier = Modifier.weight(1f)
                )
                PipelineLegendTile(
                    label = "En Preparación",
                    count = pipeline.inPrep,
                    dotColor = ColorInPrepSage,
                    onClick = { onSegmentClick("IN_PREPARATION") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PipelineLegendTile(
                    label = "En Tránsito",
                    count = pipeline.inTransit,
                    dotColor = ColorPrimary,
                    onClick = { onSegmentClick("IN_TRANSIT") },
                    modifier = Modifier.weight(1f)
                )
                PipelineLegendTile(
                    label = "Entregados",
                    count = pipeline.delivered,
                    dotColor = ColorSuccess,
                    onClick = { onSegmentClick("DELIVERED") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PipelineLegendTile(
    label: String,
    count: Int,
    dotColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(FruitLogixTheme.colors.surfaceTint)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = FruitLogixTheme.typography.bodySmall,
                color = FruitLogixTheme.colors.textOnLight,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = count.toString(),
            style = FruitLogixTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = FruitLogixTheme.colors.textOnLight
        )
    }
}

