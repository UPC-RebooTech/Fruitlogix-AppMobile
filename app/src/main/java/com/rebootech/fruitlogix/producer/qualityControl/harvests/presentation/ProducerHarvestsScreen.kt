package com.rebootech.fruitlogix.producer.qualityControl.harvests.presentation

import androidx.annotation.StringRes
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.qualityControl.harvests.data.FakeHarvestRepository
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.HarvestStatus
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.PlannedHarvest
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.ScheduledHarvest
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.repository.StockRepository
import com.rebootech.fruitlogix.producer.qualityControl.stock.presentation.StockTabButton
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.LimeProgressBar
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.format.appLocale
import com.rebootech.fruitlogix.shared.ui.format.formatRelativeDateTime
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.IsoFields

/** Localized label + badge type for a harvest status. Also used by the Home "Next harvest" card. */
@StringRes
fun harvestStatusLabel(status: HarvestStatus): Int = when (status) {
    HarvestStatus.READY -> R.string.producer_harvests_status_ready
    HarvestStatus.RIPENING -> R.string.producer_harvests_status_ripening
    HarvestStatus.SCHEDULED -> R.string.producer_harvests_status_scheduled
    HarvestStatus.REGISTERED -> R.string.producer_harvests_status_registered
}

fun harvestStatusBadge(status: HarvestStatus): StatusBadgeType = when (status) {
    HarvestStatus.READY -> StatusBadgeType.SUCCESS
    HarvestStatus.RIPENING -> StatusBadgeType.WARNING
    HarvestStatus.SCHEDULED -> StatusBadgeType.INFO
    HarvestStatus.REGISTERED -> StatusBadgeType.NEUTRAL
}

/**
 * Harvest control (Harvests tab of Stock).
 * Card rule: the weekly forecast is the highlighted (dark) block; schedule and harvests are
 * light list cards. The Inventory / Harvests switch at the top mirrors the Stock screen.
 */
@Composable
fun ProducerHarvestsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToRegisterHarvest: () -> Unit = {},
    onNavigateToInventory: () -> Unit = onNavigateBack,
    modifier: Modifier = Modifier
) {
    val repository = FakeHarvestRepository.instance
    val harvestList by repository.recentHarvests.collectAsState()
    val scheduled by repository.scheduledHarvests.collectAsState()
    val forecast = repository.weeklyForecast

    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val locale = appLocale()
    val numberFormat = remember(locale) { NumberFormat.getNumberInstance(locale) }

    // Next 7 days; null = show all upcoming harvests
    val days = remember { (0L..6L).map { LocalDate.now().plusDays(it) } }
    var selectedDay by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    val visibleScheduled = scheduled.filter { selectedDay == null || it.scheduledFor.toLocalDate() == selectedDay }

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
                        text = stringResource(R.string.producer_harvests_title),
                        style = FruitLogixTheme.typography.headlineMedium,
                        color = scheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.producer_harvests_subtitle),
                        style = FruitLogixTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant
                    )
                }
            }

            // Inventory / Harvests switch (same as Stock)
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
                        text = stringResource(R.string.producer_stock_tab_inventory, StockRepository.stockItems.size),
                        selected = false,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToInventory
                    )
                    StockTabButton(
                        text = stringResource(R.string.producer_stock_tab_harvests, harvestList.size),
                        selected = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Weekly forecast (highlighted, dark)
            item {
                FruitLogixCard(
                    variant = CardVariant.Dark,
                    shape = FruitLogixTheme.shapes.HeroCard,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(spacing.sm)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.producer_harvests_forecast_week,
                                    forecast.weekStart.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
                                ).uppercase(),
                                style = FruitLogixTheme.typography.labelSmall,
                                color = scheme.inversePrimary,
                                modifier = Modifier.weight(1f)
                            )
                            StatusBadge(
                                text = stringResource(R.string.producer_harvests_export_grade),
                                type = StatusBadgeType.NEUTRAL,
                                showDot = false,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text(
                            text = stringResource(
                                R.string.producer_harvests_forecast_requested,
                                forecast.productName,
                                numberFormat.format(forecast.requestedKg)
                            ),
                            style = FruitLogixTheme.typography.titleMedium,
                            color = scheme.inverseOnSurface
                        )
                        Text(
                            text = stringResource(
                                R.string.producer_harvests_forecast_committed,
                                forecast.clients.joinToString(" · ")
                            ),
                            style = FruitLogixTheme.typography.bodySmall,
                            color = FruitLogixTheme.colors.onInverseSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(spacing.inner))
                        LimeProgressBar(progress = forecast.fulfilledPct / 100f)
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.producer_harvests_forecast_fulfilled),
                                style = FruitLogixTheme.typography.bodySmall,
                                color = FruitLogixTheme.colors.onInverseSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = stringResource(
                                    R.string.producer_harvests_forecast_progress,
                                    numberFormat.format(forecast.fulfilledKg),
                                    numberFormat.format(forecast.requestedKg),
                                    forecast.fulfilledPct
                                ),
                                style = FruitLogixTheme.extraTypography.dataMonoSmall,
                                color = scheme.inversePrimary
                            )
                        }
                    }
                }
            }

            // Schedule (next 7 days)
            item {
                Column {
                    Text(
                        text = stringResource(R.string.producer_harvests_schedule_title),
                        style = FruitLogixTheme.typography.titleMedium,
                        color = scheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(spacing.xs))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                        items(days) { day ->
                            DayChip(
                                day = day,
                                selected = selectedDay == day,
                                hasHarvest = scheduled.any { it.scheduledFor.toLocalDate() == day },
                                onClick = { selectedDay = if (selectedDay == day) null else day }
                            )
                        }
                    }
                }
            }

            // Upcoming harvests
            item {
                SectionTitle(
                    title = stringResource(R.string.producer_harvests_upcoming_title),
                    trailing = pluralStringResource(
                        R.plurals.producer_harvests_queued,
                        visibleScheduled.size,
                        visibleScheduled.size
                    )
                )
            }
            if (visibleScheduled.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.producer_harvests_none_scheduled),
                        style = FruitLogixTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant
                    )
                }
            } else {
                items(visibleScheduled, key = { it.id }) { harvest ->
                    ScheduledHarvestCard(harvest = harvest, numberFormat = numberFormat)
                }
            }

            // Recent harvests in stock
            item {
                SectionTitle(
                    title = stringResource(R.string.producer_harvests_recent_title),
                    trailing = stringResource(R.string.producer_harvests_live),
                    trailingIsLive = true
                )
            }
            items(harvestList, key = { it.id }) { harvest ->
                RecentHarvestCard(harvest = harvest, numberFormat = numberFormat)
            }
        }

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

@Composable
private fun SectionTitle(title: String, trailing: String, trailingIsLive: Boolean = false) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = FruitLogixTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = FruitLogixTheme.typography.titleMedium,
            color = scheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (trailingIsLive) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(FruitLogixTheme.colors.successContent)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = trailing,
            style = FruitLogixTheme.typography.labelMedium,
            color = scheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DayChip(
    day: LocalDate,
    selected: Boolean,
    hasHarvest: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val locale = appLocale()
    val weekday = day.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).replaceFirstChar { it.titlecase(locale) }
    val description = stringResource(
        if (selected) R.string.producer_orders_cd_chip_selected else R.string.producer_orders_cd_chip_not_selected,
        "$weekday ${day.dayOfMonth}"
    )
    val fg = if (selected) scheme.onPrimaryContainer else scheme.onSurface

    Column(
        modifier = Modifier
            .defaultMinSize(minWidth = 52.dp, minHeight = 64.dp)
            .clip(FruitLogixTheme.shapes.Button)
            .background(if (selected) scheme.primaryContainer else scheme.surfaceContainerLow)
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics { contentDescription = description }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = weekday,
            style = FruitLogixTheme.typography.labelMedium,
            color = if (selected) fg else scheme.onSurfaceVariant
        )
        Text(
            text = day.dayOfMonth.toString(),
            style = FruitLogixTheme.typography.titleMedium,
            color = fg
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (hasHarvest) (if (selected) fg else scheme.primary) else androidx.compose.ui.graphics.Color.Transparent)
        )
    }
}

/** Planned harvest (light list card). */
@Composable
private fun ScheduledHarvestCard(harvest: ScheduledHarvest, numberFormat: NumberFormat) {
    val scheme = MaterialTheme.colorScheme
    FruitLogixCard(variant = CardVariant.White, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(FruitLogixTheme.spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = harvest.productName,
                        style = FruitLogixTheme.typography.titleMedium,
                        color = scheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatRelativeDateTime(harvest.scheduledFor),
                        style = FruitLogixTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                StatusBadge(
                    text = stringResource(R.string.producer_harvests_status_planned),
                    type = StatusBadgeType.INFO,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))

            Row(horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
                InfoTile(
                    label = stringResource(R.string.producer_harvests_est_yield),
                    value = "~${stringResource(R.string.producer_orders_tile_volume_format, numberFormat.format(harvest.estimatedYieldKg))}",
                    modifier = Modifier.weight(1f)
                )
                InfoTile(
                    label = stringResource(R.string.producer_harvests_zone),
                    value = harvest.zone,
                    mono = false,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))

            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(
                    id = R.drawable.ic_groups,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = pluralStringResource(
                        R.plurals.producer_harvests_assigned_crew,
                        harvest.pickers,
                        harvest.crewName,
                        harvest.pickers
                    ),
                    style = FruitLogixTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Harvest already logged in stock (light list card). */
@Composable
private fun RecentHarvestCard(harvest: PlannedHarvest, numberFormat: NumberFormat) {
    val scheme = MaterialTheme.colorScheme
    FruitLogixCard(variant = CardVariant.White, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(FruitLogixTheme.spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = harvest.fruitName,
                        style = FruitLogixTheme.typography.titleMedium,
                        color = scheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatRelativeDateTime(harvest.harvestedAt),
                        style = FruitLogixTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                StatusBadge(
                    text = stringResource(harvestStatusLabel(harvest.status)),
                    type = harvestStatusBadge(harvest.status),
                    maxLines = 1,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))

            Row(horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
                InfoTile(
                    label = stringResource(R.string.producer_harvests_logged_qty),
                    value = stringResource(R.string.producer_orders_tile_volume_format, numberFormat.format(harvest.weightKg)),
                    modifier = Modifier.weight(1f)
                )
                InfoTile(
                    label = stringResource(R.string.producer_harvests_batch_lot),
                    value = harvest.lotCode,
                    modifier = Modifier.weight(1f)
                )
            }

            if (harvest.crewOrTransport.isNotBlank() || harvest.sectorOrVault.isNotBlank()) {
                Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
                HorizontalDivider(color = scheme.outlineVariant)
                Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (harvest.crewOrTransport.isNotBlank()) {
                        AppIcon(
                            id = R.drawable.ic_id,
                            contentDescription = null,
                            tint = scheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = harvest.crewOrTransport,
                            style = FruitLogixTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    Text(
                        text = harvest.sectorOrVault,
                        style = FruitLogixTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    mono: Boolean = true
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .clip(FruitLogixTheme.shapes.Button)
            .background(scheme.surfaceContainerLow)
            .padding(horizontal = FruitLogixTheme.spacing.inner, vertical = FruitLogixTheme.spacing.xs)
    ) {
        Text(
            text = label,
            style = FruitLogixTheme.typography.labelMedium,
            color = scheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (mono) FruitLogixTheme.extraTypography.dataMono else FruitLogixTheme.typography.titleSmall,
            color = scheme.onSurface
        )
    }
}
