package com.rebootech.fruitlogix.producer.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockItem
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockStatus
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/** Inventory health card (light list card). */
@Composable
fun StockHealthSection(
    stockItems: List<StockItem>,
    onItemClick: (StockItem) -> Unit,
    onNavigateStockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FruitLogixCard(
        variant = CardVariant.White,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(FruitLogixTheme.spacing.sm)) {
            CardHeader(
                title = stringResource(R.string.producer_home_stock_title),
                subtitle = stringResource(R.string.producer_home_stock_subtitle),
                actionDescription = stringResource(R.string.producer_home_cd_view_stock),
                onAction = onNavigateStockClick
            )

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))

            stockItems.forEachIndexed { index, item ->
                StockLevelRow(
                    item = item,
                    onClick = { onItemClick(item) }
                )
                if (index < stockItems.lastIndex) {
                    Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
                }
            }
        }
    }
}

/**
 * One product: name + status tag, quantity vs. minimum, and a bar with the threshold mark.
 */
@Composable
fun StockLevelRow(
    item: StockItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors

    val barColor = when (item.status) {
        StockStatus.CRITICAL -> scheme.error
        StockStatus.LOW -> colors.attentionContent
        StockStatus.HEALTHY -> colors.successContent
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(FruitLogixTheme.shapes.Button)
            .clickable(onClick = onClick)
            .padding(vertical = FruitLogixTheme.spacing.xs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.productName,
                style = FruitLogixTheme.typography.titleSmall,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            when (item.status) {
                StockStatus.LOW -> {
                    Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                    StatusBadge(
                        text = stringResource(R.string.producer_home_stock_low),
                        type = StatusBadgeType.WARNING,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                StockStatus.CRITICAL -> {
                    Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                    StatusBadge(
                        text = stringResource(R.string.producer_home_stock_critical),
                        type = StatusBadgeType.DANGER,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                StockStatus.HEALTHY -> Unit
            }
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
            Text(
                text = stringResource(R.string.common_kg_value, item.availableKg),
                style = FruitLogixTheme.extraTypography.dataMono,
                color = if (item.status == StockStatus.CRITICAL) scheme.error else scheme.onSurface
            )
        }

        Text(
            text = stringResource(R.string.producer_home_stock_min, item.minThresholdKg),
            style = FruitLogixTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))

        // Bar with threshold mark at 25% (fillFraction = available / (min * 4))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(FruitLogixTheme.shapes.Pill)
                .background(scheme.surfaceContainerHigh)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(item.fillFraction)
                    .fillMaxHeight()
                    .clip(FruitLogixTheme.shapes.Pill)
                    .background(barColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.25f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(scheme.onSurfaceVariant)
                )
            }
        }
    }
}
