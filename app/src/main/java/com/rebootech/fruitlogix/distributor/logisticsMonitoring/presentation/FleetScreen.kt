package com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules.ArrivalRules
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.components.AlertCard
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.components.ArrivalCard
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.components.DispatchCard
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.components.GeofenceNotifier
import com.rebootech.fruitlogix.shared.ui.components.SegmentedTabs
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.EmptyState
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.format.formatDecimal
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import kotlinx.coroutines.launch

/**
 * Fleet center (tab 3): trucks on route, arrivals at the hub and alerts in one place,
 * switched with three tabs. Sensors and limits live in their own screen (button at the top).
 */
@Composable
fun FleetScreen(
    onOpenDispatch: (unitId: String) -> Unit,
    onStartReception: () -> Unit,
    onOpenSensors: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FleetViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val resolvedMessage = stringResource(R.string.fleet_alert_resolved)

    Box(modifier = modifier.fillMaxSize().background(scheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = spacing.sm, end = spacing.sm, top = spacing.sm, bottom = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.inner)
        ) {
            item {
                Column {
                    Text(stringResource(R.string.fleet_title), style = FruitLogixTheme.typography.headlineMedium, color = scheme.onBackground)
                    Text(
                        text = stringResource(R.string.fleet_summary, state.onRoute.size, state.arrivals.size, state.alerts.size),
                        style = FruitLogixTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(spacing.inner))
                    SecondaryButton(
                        text = stringResource(R.string.fleet_sensors_button),
                        onClick = onOpenSensors,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { AppIcon(id = R.drawable.ic_sensor_waves, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)) }
                    )
                }
            }

            item {
                SegmentedTabs(
                    options = listOf(
                        FleetTab.ON_ROUTE to stringResource(R.string.fleet_tab_on_route, state.onRoute.size),
                        FleetTab.ARRIVALS to stringResource(R.string.fleet_tab_arrivals, state.arrivals.size),
                        FleetTab.ALERTS to stringResource(R.string.fleet_tab_alerts, state.alerts.size)
                    ),
                    selected = state.tab,
                    onSelect = viewModel::onTabSelected
                )
            }

            when (state.tab) {
                FleetTab.ON_ROUTE -> {
                    if (state.onRoute.isEmpty()) {
                        item { EmptyState(title = stringResource(R.string.fleet_empty_on_route), message = "") }
                    } else {
                        items(state.onRoute, key = { it.dispatch.unitId }) { item ->
                            DispatchCard(item = item, onClick = { onOpenDispatch(item.dispatch.unitId) })
                        }
                    }
                }

                FleetTab.ARRIVALS -> {
                    item {
                        Text(
                            text = stringResource(R.string.fleet_arrivals_hint, ArrivalRules.ARRIVALS_RADIUS_KM.toInt(), state.hubName),
                            style = FruitLogixTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant
                        )
                    }
                    if (state.arrivals.isEmpty()) {
                        item { EmptyState(title = stringResource(R.string.fleet_arrivals_empty), message = "") }
                    } else {
                        items(state.arrivals, key = { "arrival-" + it.dispatch.unitId }) { item ->
                            ArrivalCard(
                                item = item,
                                onStartReception = onStartReception,
                                onViewTrip = { onOpenDispatch(item.dispatch.unitId) }
                            )
                        }
                    }
                    item { GeofenceDemo(onSimulate = viewModel::simulateGeofenceEntry) }
                }

                FleetTab.ALERTS -> {
                    if (state.alerts.isEmpty()) {
                        item {
                            EmptyState(
                                title = stringResource(R.string.fleet_alerts_empty),
                                message = "",
                                icon = { AppIcon(id = R.drawable.ic_verified, contentDescription = null, tint = FruitLogixTheme.colors.successContent, modifier = Modifier.size(FruitLogixTheme.sizing.iconBox)) }
                            )
                        }
                    } else {
                        items(state.alerts, key = { it.id }) { alert ->
                            AlertCard(
                                alert = alert,
                                onOpen = { alert.unitId?.let(onOpenDispatch) ?: onOpenSensors() },
                                onResolve = {
                                    viewModel.resolveAlert(alert.id)
                                    scope.launch { snackbar.showSnackbar(resolvedMessage) }
                                }
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(spacing.sm)
        )
    }

    state.geofenceEntry?.let { entry ->
        GeofenceSheet(
            entry = entry,
            hubName = state.hubName,
            onStartReception = {
                viewModel.dismissGeofenceEntry()
                onStartReception()
            },
            onDismiss = viewModel::dismissGeofenceEntry
        )
    }
}

@Composable
private fun GeofenceDemo(onSimulate: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FruitLogixTheme.shapes.Button)
            .border(1.dp, scheme.outlineVariant, FruitLogixTheme.shapes.Button)
            .padding(FruitLogixTheme.spacing.inner)
    ) {
        Text(stringResource(R.string.fleet_geofence_demo), style = FruitLogixTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
        TextActionButton(text = stringResource(R.string.fleet_geofence_simulate), onClick = onSimulate)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GeofenceSheet(
    entry: GeofenceEntry,
    hubName: String,
    onStartReception: () -> Unit,
    onDismiss: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val context = LocalContext.current
    val d = entry.item.dispatch
    val title = stringResource(R.string.fleet_geofence_title, d.unitId)
    val message = stringResource(
        R.string.fleet_geofence_message,
        formatDecimal(entry.event.distanceKm.toDouble()),
        hubName,
        entry.minutesToDock,
        d.dock
    )

    // Same text as a system notification, so it is seen even outside the app
    LaunchedEffect(entry.event.timestampMs) { GeofenceNotifier.notify(context, title, message) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = scheme.surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FruitLogixTheme.spacing.md)
                .padding(bottom = FruitLogixTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(colors.coldContainer),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(id = R.drawable.ic_logistics_arrive, contentDescription = null, tint = colors.coldContent, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
            Text(title, style = FruitLogixTheme.typography.titleLarge, color = scheme.onSurface, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
            Text(message, style = FruitLogixTheme.typography.bodyLarge, color = scheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Text(
                text = "${stringResource(R.string.fleet_cargo, d.productName, d.pallets)} · ${d.driver.name}",
                style = FruitLogixTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.md))
            PrimaryButton(text = stringResource(R.string.fleet_arrival_start), onClick = onStartReception, modifier = Modifier.fillMaxWidth())
            TextActionButton(text = stringResource(R.string.dispatch_close), onClick = onDismiss)
            Spacer(modifier = Modifier.width(1.dp))
        }
    }
}
