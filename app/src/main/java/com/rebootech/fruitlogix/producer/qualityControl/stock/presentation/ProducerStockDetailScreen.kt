package com.rebootech.fruitlogix.producer.qualityControl.stock.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockMovement
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockMovementType
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.repository.StockItemModel
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.repository.StockRepository
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.DetailSection
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.LimeProgressBar
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.format.appLocale
import com.rebootech.fruitlogix.shared.ui.format.formatDecimal
import com.rebootech.fruitlogix.shared.ui.format.formatTimeAgo
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.text.NumberFormat

/**
 * Stock lot detail.
 * - Highlighted (dark) summary: status, chamber temperature, kg on hand, reserved and free.
 * - Light sections: adjust stock (±1/±10 kg, save), minimum stock alert, movements.
 */
@Composable
fun ProducerStockDetailScreen(
    lotId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val item = StockRepository.stockItems.find { it.id == lotId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {
        if (item == null) {
            Column(modifier = Modifier.padding(spacing.sm)) {
                BackHeader(title = "", onBack = onNavigateBack)
                Text(
                    text = stringResource(R.string.stock_detail_not_found),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
            return@Column
        }

        var target by rememberSaveable(item.id) { mutableIntStateOf(item.availableKg) }
        val savedMessage = stringResource(R.string.stock_detail_saved)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.inner)
        ) {
            BackHeader(
                title = item.productName,
                subtitle = stringResource(R.string.stock_detail_subtitle, item.variety, item.chamberLocation),
                onBack = onNavigateBack
            )

            SummaryCard(item)
            AdjustSection(current = item.availableKg, target = target, onTargetChange = { target = it })
            ThresholdSection(item)
            MovementsSection(StockRepository.movements[item.id].orEmpty())
        }

        PrimaryButton(
            text = stringResource(R.string.stock_detail_save),
            onClick = {
                StockRepository.updateStockKg(item.id, target)
                Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
                onNavigateBack()
            },
            enabled = target != item.availableKg,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.sm, vertical = spacing.xs)
        )
    }
}

@Composable
private fun SummaryCard(item: StockItemModel) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val numberFormat = NumberFormat.getNumberInstance(appLocale())
    fun kg(value: Int) = numberFormat.format(value)

    FruitLogixCard(
        variant = CardVariant.Dark,
        shape = FruitLogixTheme.shapes.HeroCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(FruitLogixTheme.spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(
                    text = stringResource(
                        if (item.isCritical) R.string.producer_stock_status_critical else R.string.producer_stock_status_healthy
                    ),
                    type = if (item.isCritical) StatusBadgeType.DANGER else StatusBadgeType.SUCCESS,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.weight(1f))
                item.chamberTempC?.let { temp ->
                    StatusBadge(
                        text = stringResource(R.string.stock_detail_chamber_temp, formatDecimal(temp)),
                        type = StatusBadgeType.INFO,
                        maxLines = 1,
                        softWrap = false,
                        icon = {
                            AppIcon(
                                id = R.drawable.ic_logistics_snowflake,
                                contentDescription = null,
                                tint = colors.info,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
            Text(
                text = stringResource(R.string.stock_detail_on_hand).uppercase(),
                style = FruitLogixTheme.typography.labelSmall,
                color = colors.onInverseSurfaceVariant
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = stringResource(R.string.producer_orders_tile_volume_format, kg(item.availableKg)),
                    style = FruitLogixTheme.extraTypography.kpiNumber,
                    color = if (item.isCritical) colors.dangerStrong else scheme.inverseOnSurface
                )
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                Text(
                    text = stringResource(R.string.producer_stock_capacity, kg(item.totalCapacityKg)),
                    style = FruitLogixTheme.typography.bodySmall,
                    color = colors.onInverseSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            LimeProgressBar(
                progress = item.availableKg.toFloat() / item.totalCapacityKg.coerceAtLeast(1),
                progressColor = if (item.isCritical) colors.dangerStrong else null
            )
            if (item.isCritical) {
                Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
                Text(
                    text = stringResource(
                        R.string.stock_detail_below_min,
                        kg(item.minThresholdKg - item.availableKg),
                        kg(item.minThresholdKg)
                    ),
                    style = FruitLogixTheme.typography.bodySmall,
                    color = colors.dangerStrong
                )
            }

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
            HorizontalDivider(color = colors.onInverseSurfaceVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))

            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.stock_detail_reserved), style = FruitLogixTheme.typography.labelMedium, color = colors.onInverseSurfaceVariant)
                    Text(
                        stringResource(R.string.producer_orders_tile_volume_format, kg(item.reservedKg)),
                        style = FruitLogixTheme.extraTypography.dataMono,
                        color = scheme.inverseOnSurface
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.stock_detail_free), style = FruitLogixTheme.typography.labelMedium, color = colors.onInverseSurfaceVariant)
                    Text(
                        stringResource(R.string.producer_orders_tile_volume_format, kg(item.freeKg)),
                        style = FruitLogixTheme.extraTypography.dataMono,
                        color = scheme.inversePrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun AdjustSection(current: Int, target: Int, onTargetChange: (Int) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val numberFormat = NumberFormat.getNumberInstance(appLocale())
    val diff = target - current

    DetailSection(title = stringResource(R.string.stock_detail_section_adjust)) {
        Text(
            text = stringResource(R.string.stock_detail_adjust_hint),
            style = FruitLogixTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
        Text(
            text = stringResource(R.string.stock_detail_new_amount),
            style = FruitLogixTheme.typography.labelMedium,
            color = scheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.producer_orders_tile_volume_format, numberFormat.format(target)),
            style = FruitLogixTheme.extraTypography.readingMono,
            color = scheme.onSurface
        )
        if (diff != 0) {
            Text(
                text = stringResource(R.string.stock_detail_change, if (diff > 0) "+$diff" else "$diff"),
                style = FruitLogixTheme.typography.labelMedium,
                color = if (diff > 0) FruitLogixTheme.colors.successContent else scheme.error
            )
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
        Row(horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
            listOf(-10, -1, 1, 10).forEach { step ->
                StepButton(
                    label = if (step > 0) "+$step" else "$step",
                    description = stringResource(R.string.stock_detail_cd_adjust, if (step > 0) "+$step" else "$step"),
                    onClick = { onTargetChange((target + step).coerceAtLeast(0)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ThresholdSection(item: StockItemModel) {
    val scheme = MaterialTheme.colorScheme
    DetailSection(title = stringResource(R.string.stock_detail_section_threshold)) {
        Text(
            text = stringResource(R.string.stock_detail_threshold_hint),
            style = FruitLogixTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.common_kg_value, item.minThresholdKg),
                style = FruitLogixTheme.extraTypography.dataMono,
                color = scheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            StepButton(
                label = "−5",
                description = stringResource(R.string.stock_detail_cd_threshold_down),
                onClick = { StockRepository.updateThreshold(item.id, item.minThresholdKg - 5) },
                modifier = Modifier.width(64.dp)
            )
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
            StepButton(
                label = "+5",
                description = stringResource(R.string.stock_detail_cd_threshold_up),
                onClick = { StockRepository.updateThreshold(item.id, item.minThresholdKg + 5) },
                modifier = Modifier.width(64.dp)
            )
        }
    }
}

@Composable
private fun MovementsSection(movements: List<StockMovement>) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    DetailSection(title = stringResource(R.string.stock_detail_section_movements)) {
        if (movements.isEmpty()) {
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
            Text(
                text = stringResource(R.string.stock_detail_no_movements),
                style = FruitLogixTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )
        }
        movements.forEachIndexed { index, move ->
            if (index > 0) HorizontalDivider(color = scheme.outlineVariant)
            val typeLabel = stringResource(
                when (move.type) {
                    StockMovementType.HARVEST_INTAKE -> R.string.stock_detail_move_intake
                    StockMovementType.DISPATCH -> R.string.stock_detail_move_dispatch
                    StockMovementType.ADJUSTMENT -> R.string.stock_detail_move_adjustment
                }
            )
            val iconRes = when (move.type) {
                StockMovementType.HARVEST_INTAKE -> R.drawable.ic_leaf
                StockMovementType.DISPATCH -> R.drawable.ic_dispatch_truck
                StockMovementType.ADJUSTMENT -> R.drawable.ic_edit
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = FruitLogixTheme.spacing.inner),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(FruitLogixTheme.sizing.iconBox)
                        .clip(FruitLogixTheme.shapes.IconBox)
                        .background(scheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(id = iconRes, contentDescription = null, tint = scheme.onSecondaryContainer, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
                }
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = move.reference?.let { stringResource(R.string.stock_detail_move_with_ref, typeLabel, it) } ?: typeLabel,
                        style = FruitLogixTheme.typography.titleSmall,
                        color = scheme.onSurface
                    )
                    Text(
                        text = formatTimeAgo(move.at),
                        style = FruitLogixTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
                Text(
                    text = stringResource(R.string.common_kg_value, move.kg).let { if (move.kg > 0) "+$it" else it },
                    style = FruitLogixTheme.extraTypography.dataMono,
                    color = if (move.kg > 0) colors.successContent else scheme.error
                )
            }
        }
    }
}

/** 56dp tonal step button. */
@Composable
private fun StepButton(
    label: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .height(FruitLogixTheme.sizing.fieldTouch)
            .clip(FruitLogixTheme.shapes.Button)
            .background(scheme.secondaryContainer)
            .clickable(role = Role.Button, onClick = onClick)
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = FruitLogixTheme.extraTypography.dataMono,
            color = scheme.onSecondaryContainer
        )
    }
}

