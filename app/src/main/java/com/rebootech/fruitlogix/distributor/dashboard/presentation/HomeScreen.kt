package com.rebootech.fruitlogix.distributor.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.dashboard.domain.DashboardData
import com.rebootech.fruitlogix.distributor.dashboard.domain.DashboardSummary
import com.rebootech.fruitlogix.distributor.dashboard.domain.DistributorTask
import com.rebootech.fruitlogix.distributor.dashboard.presentation.components.DistributorTaskRow
import com.rebootech.fruitlogix.distributor.dashboard.presentation.components.FleetUnitRow
import com.rebootech.fruitlogix.distributor.dashboard.presentation.components.TemperatureAlertCard
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.KpiCard
import com.rebootech.fruitlogix.shared.ui.components.QuickActionButton
import com.rebootech.fruitlogix.shared.ui.components.SectionHeader
import com.rebootech.fruitlogix.shared.ui.format.appLocale
import com.rebootech.fruitlogix.shared.ui.format.formatDecimal
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Distributor Home, ordered by urgency: greeting → reefer at risk → quick actions →
 * today's tasks → KPIs → fleet on the road.
 */
@Composable
fun HomeScreen(
    onScanReception: () -> Unit,
    onNewOrder: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenFleet: () -> Unit,
    onOpenSensors: () -> Unit,
    onOpenUnit: (unitId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val data = state.data

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (state.isLoading || data == null) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.Center))
            return@Box
        }
        HomeContent(
            state = state,
            data = data,
            onScanReception = onScanReception,
            onNewOrder = onNewOrder,
            onOpenOrders = onOpenOrders,
            onOpenFleet = onOpenFleet,
            onOpenSensors = onOpenSensors,
            onOpenUnit = onOpenUnit
        )
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    data: DashboardData,
    onScanReception: () -> Unit,
    onNewOrder: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenFleet: () -> Unit,
    onOpenSensors: () -> Unit,
    onOpenUnit: (String) -> Unit
) {
    val spacing = FruitLogixTheme.spacing

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = spacing.sm, end = spacing.sm, top = spacing.sm, bottom = spacing.xl),
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        item { Greeting(state.dayPart, state.userName, data.hubName, state.today) }

        data.temperatureAlert?.let { alert ->
            item { TemperatureAlertCard(alert = alert, onReviewUnit = { onOpenUnit(alert.unitId) }) }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                QuickActionButton(R.drawable.ic_radar, stringResource(R.string.dist_home_qa_scan), onScanReception, Modifier.weight(1f))
                QuickActionButton(R.drawable.ic_orders, stringResource(R.string.dist_home_qa_new_order), onNewOrder, Modifier.weight(1f))
                QuickActionButton(R.drawable.ic_fleet, stringResource(R.string.dist_home_qa_fleet), onOpenFleet, Modifier.weight(1f))
                QuickActionButton(R.drawable.ic_sensor_waves, stringResource(R.string.dist_home_qa_sensors), onOpenSensors, Modifier.weight(1f))
            }
        }

        if (data.tasks.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    SectionHeader(title = stringResource(R.string.dist_home_tasks_title))
                    data.tasks.forEach { task ->
                        DistributorTaskRow(
                            task = task,
                            onClick = {
                                when (task) {
                                    is DistributorTask.MissingDispatchGuide -> onOpenOrders()
                                    is DistributorTask.ReceiveAtDock -> onScanReception()
                                }
                            }
                        )
                    }
                }
            }
        }

        item { KpiGrid(data.summary) }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                SectionHeader(
                    title = stringResource(R.string.dist_home_fleet_title),
                    subtitle = stringResource(R.string.dist_home_fleet_subtitle, data.fleet.size),
                    actionText = stringResource(R.string.dist_home_fleet_see_all),
                    onActionClick = onOpenFleet
                )
                data.fleet.forEach { truck ->
                    FleetUnitRow(truck = truck, onClick = { onOpenUnit(truck.dispatch.unitId) })
                }
            }
        }
    }
}

@Composable
private fun Greeting(dayPart: DayPart, userName: String, hubName: String, today: LocalDate) {
    val scheme = MaterialTheme.colorScheme
    val locale = appLocale()
    val greeting = if (userName.isBlank()) {
        stringResource(
            when (dayPart) {
                DayPart.MORNING -> R.string.dist_home_greeting_morning_plain
                DayPart.AFTERNOON -> R.string.dist_home_greeting_afternoon_plain
                DayPart.EVENING -> R.string.dist_home_greeting_evening_plain
            }
        )
    } else {
        stringResource(
            when (dayPart) {
                DayPart.MORNING -> R.string.dist_home_greeting_morning
                DayPart.AFTERNOON -> R.string.dist_home_greeting_afternoon
                DayPart.EVENING -> R.string.dist_home_greeting_evening
            },
            userName
        )
    }
    val pattern = stringResource(R.string.dist_home_date_pattern)
    val date = remember(today, locale, pattern) {
        today.format(DateTimeFormatter.ofPattern(pattern, locale)).replaceFirstChar { it.titlecase(locale) }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(greeting, style = FruitLogixTheme.typography.headlineMedium, color = scheme.onBackground)
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(id = R.drawable.ic_warehouse, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xxs))
            Text("$hubName · $date", style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun KpiGrid(summary: DashboardSummary) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val spacing = FruitLogixTheme.spacing

    @Composable
    fun icon(id: Int): @Composable () -> Unit = {
        AppIcon(id = id, contentDescription = null, tint = scheme.inversePrimary, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
    }

    @Composable
    fun footer(text: String): @Composable () -> Unit = {
        Text(text, style = FruitLogixTheme.typography.bodySmall, color = colors.onInverseSurfaceVariant)
    }

    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
            KpiCard(
                caption = stringResource(R.string.dist_home_kpi_active_orders),
                value = summary.activeOrders.toString(),
                icon = icon(R.drawable.ic_orders),
                footer = footer(stringResource(R.string.dist_home_kpi_pending, summary.pendingOrders)),
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                caption = stringResource(R.string.dist_home_kpi_deliveries),
                value = summary.deliveriesToday.toString(),
                icon = icon(R.drawable.ic_dispatch_truck),
                footer = footer(stringResource(R.string.dist_home_kpi_avg_eta, summary.averageEtaMinutes)),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
            KpiCard(
                caption = stringResource(R.string.dist_home_kpi_quality),
                value = "${formatDecimal(summary.qualityApprovalPct)}%",
                icon = icon(R.drawable.ic_shield_check),
                footer = footer(stringResource(R.string.dist_home_kpi_lots, summary.lotsInspected)),
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                caption = stringResource(R.string.dist_home_kpi_alerts),
                value = (summary.criticalAlerts + summary.warningAlerts).toString(),
                icon = icon(R.drawable.ic_sensor_waves),
                footer = footer(stringResource(R.string.dist_home_kpi_alerts_detail, summary.criticalAlerts, summary.warningAlerts)),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
