package com.rebootech.fruitlogix.producer.qualityControl.stock.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.qualityControl.harvests.data.FakeHarvestRepository
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.repository.StockItemModel
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.repository.StockRepository
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.CompactKpiTile
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.LimeProgressBar
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.text.NumberFormat


/**
 * Producer Stock (Inventory tab).
 * Card rule: summary numbers are dark KPI tiles; inventory lots are light list cards.
 */
@Composable
fun ProducerStockScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToRegisterHarvest: () -> Unit,
    onNavigateToHarvestsTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stockList = StockRepository.stockItems
    val harvests by FakeHarvestRepository.instance.recentHarvests.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val locale = LocalConfiguration.current.locales[0]
    val numberFormat = remember(locale) { NumberFormat.getNumberInstance(locale) }

    val totalAvailable = stockList.sumOf { it.availableKg }
    val criticalCount = stockList.count { it.isCritical }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = spacing.sm, end = spacing.sm, top = spacing.sm, bottom = spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.inner)
        ) {
            item {
                Column {
                    Text(
                        text = stringResource(R.string.producer_stock_title),
                        style = FruitLogixTheme.typography.headlineMedium,
                        color = scheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.producer_stock_subtitle),
                        style = FruitLogixTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant
                    )
                }
            }

            // Summary KPIs (dark)
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    CompactKpiTile(
                        label = stringResource(R.string.producer_stock_kpi_available),
                        value = stringResource(R.string.producer_orders_tile_volume_format, numberFormat.format(totalAvailable)),
                        modifier = Modifier.weight(1f)
                    )
                    CompactKpiTile(
                        label = stringResource(R.string.producer_stock_kpi_active_lots),
                        value = stockList.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    CompactKpiTile(
                        label = stringResource(R.string.producer_stock_kpi_critical),
                        value = criticalCount.toString(),
                        valueColor = if (criticalCount > 0) FruitLogixTheme.colors.dangerStrong else null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Inventory / Harvests segmented tabs
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(FruitLogixTheme.shapes.Button)
                        .background(scheme.surfaceContainerHigh)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StockTabButton(
                        text = stringResource(R.string.producer_stock_tab_inventory, stockList.size),
                        selected = true,
                        modifier = Modifier.weight(1f)
                    )
                    StockTabButton(
                        text = stringResource(R.string.producer_stock_tab_harvests, harvests.size),
                        selected = false,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHarvestsTab
                    )
                }
            }

            // Inventory lots (light)
            items(stockList, key = { it.id }) { stock ->
                InventoryCardItem(
                    item = stock,
                    numberFormat = numberFormat,
                    onIncrement = { StockRepository.updateStockKg(stock.id, stock.availableKg + 10) },
                    onDecrement = { StockRepository.updateStockKg(stock.id, stock.availableKg - 10) },
                    onClick = { onNavigateToDetail(stock.id) }
                )
            }
        }

        // Fixed main action
        PrimaryButton(
            text = stringResource(R.string.producer_stock_register_harvest),
            onClick = onNavigateToRegisterHarvest,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.sm, vertical = spacing.xs),
            leadingIcon = {
                AppIcon(
                    id = R.drawable.ic_leaf,
                    contentDescription = null,
                    modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                )
            }
        )
    }
}

/** Segment of the Inventory / Harvests switch. 48dp touch target. */
@Composable
fun StockTabButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = FruitLogixTheme.sizing.minTouch)
            .clip(FruitLogixTheme.shapes.IconBox)
            .background(if (selected) scheme.secondary else scheme.surfaceContainerHigh)
            .clickable(role = Role.Tab, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) scheme.onSecondary else scheme.onSurface,
            style = FruitLogixTheme.typography.labelLarge,
            maxLines = 1
        )
    }
}

/** Inventory lot (light list card) with a ±10 kg stepper. */
@Composable
fun InventoryCardItem(
    item: StockItemModel,
    numberFormat: NumberFormat,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val fillFraction = (item.availableKg.toFloat() / item.totalCapacityKg.toFloat()).coerceIn(0f, 1f)
    val isCritical = item.isCritical

    FruitLogixCard(
        variant = CardVariant.White,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(FruitLogixTheme.spacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.productName,
                        style = FruitLogixTheme.typography.titleMedium,
                        color = scheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
                    StatusBadge(
                        text = stringResource(
                            if (isCritical) R.string.producer_stock_status_critical else R.string.producer_stock_status_healthy
                        ),
                        type = if (isCritical) StatusBadgeType.DANGER else StatusBadgeType.SUCCESS,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                KgStepper(
                    productName = item.productName,
                    onDecrement = onDecrement,
                    onIncrement = onIncrement
                )
            }

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = stringResource(R.string.producer_orders_tile_volume_format, numberFormat.format(item.availableKg)),
                    style = FruitLogixTheme.typography.titleLarge,
                    color = if (isCritical) scheme.error else scheme.onSurface
                )
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                Text(
                    text = stringResource(R.string.producer_stock_capacity, numberFormat.format(item.totalCapacityKg)),
                    style = FruitLogixTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))

            LimeProgressBar(
                progress = fillFraction,
                progressColor = if (isCritical) scheme.error else colors.successContent
            )

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))

            Text(
                text = stringResource(R.string.producer_stock_location, item.chamberLocation),
                style = FruitLogixTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.producer_stock_variety, item.variety),
                style = FruitLogixTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )
        }
    }
}

/** −/+ 10 kg buttons, 48dp each. */
@Composable
private fun KgStepper(
    productName: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val decreaseDescription = stringResource(R.string.producer_stock_cd_decrease, productName)
    val increaseDescription = stringResource(R.string.producer_stock_cd_increase, productName)

    Row(
        modifier = Modifier.clip(FruitLogixTheme.shapes.Button),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        StepperButton(
            symbol = "−",
            description = decreaseDescription,
            container = scheme.secondaryContainer,
            content = scheme.onSecondaryContainer,
            onClick = onDecrement
        )
        StepperButton(
            symbol = "+",
            description = increaseDescription,
            container = scheme.primaryContainer,
            content = scheme.onPrimaryContainer,
            onClick = onIncrement
        )
    }
}

@Composable
private fun StepperButton(
    symbol: String,
    description: String,
    container: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(FruitLogixTheme.sizing.minTouch)
            .background(container)
            .clickable(role = Role.Button, onClick = onClick)
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            style = FruitLogixTheme.typography.titleLarge,
            color = content
        )
    }
}
