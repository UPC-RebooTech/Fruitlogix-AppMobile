package com.rebootech.fruitlogix.distributor.fleetManagement.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Driver
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.DriverStatus
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Vehicle
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleStatus
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleType
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.rules.AssignmentRules
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.service.LicenseValidity
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.service.LicenseValidityState
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.EmptyState
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.SegmentedTabs
import com.rebootech.fruitlogix.shared.ui.components.SelectableChip
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.components.whiteCard
import com.rebootech.fruitlogix.shared.ui.format.appLocale
import com.rebootech.fruitlogix.shared.ui.format.formatDecimal
import com.rebootech.fruitlogix.shared.ui.format.formatRelativeDateTime
import com.rebootech.fruitlogix.shared.ui.format.initialsOf
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun DriversVehiclesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DriversVehiclesViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it.asString(context))
            viewModel.onMessageShown()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(scheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = spacing.sm, end = spacing.sm, top = spacing.sm, bottom = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.inner)
        ) {
            item { BackHeader(title = stringResource(R.string.drv_title), subtitle = stringResource(R.string.drv_subtitle), onBack = onBack) }
            item {
                Column {
                    PrimaryButton(
                        text = stringResource(R.string.drv_assign),
                        onClick = viewModel::openAssign,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { AppIcon(id = R.drawable.ic_assign_producer, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)) }
                    )
                    if (state.pending.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.drv_assign_pending, state.pending.size),
                            style = FruitLogixTheme.typography.bodyMedium,
                            color = FruitLogixTheme.colors.attentionContent,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
            item {
                SegmentedTabs(
                    options = listOf(
                        ResourcesTab.DRIVERS to stringResource(R.string.drv_tab_drivers, state.drivers.size),
                        ResourcesTab.VEHICLES to stringResource(R.string.drv_tab_vehicles, state.vehicles.size)
                    ),
                    selected = state.tab,
                    onSelect = viewModel::onTabChange
                )
            }
            when (state.tab) {
                ResourcesTab.DRIVERS -> {
                    item {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                        ) {
                            SelectableChip(
                                label = stringResource(R.string.dist_orders_filter_count, stringResource(R.string.drv_filter_all), state.drivers.size),
                                selected = state.status == null,
                                onClick = { viewModel.onStatusChange(null) }
                            )
                            DriverStatus.entries.forEach { status ->
                                SelectableChip(
                                    label = stringResource(R.string.dist_orders_filter_count, stringResource(driverStatusLabel(status)), state.count(status)),
                                    selected = state.status == status,
                                    onClick = { viewModel.onStatusChange(status) }
                                )
                            }
                        }
                    }
                    if (state.visibleDrivers.isEmpty()) {
                        item { EmptyState(title = stringResource(R.string.drv_empty), message = "") }
                    } else {
                        items(state.visibleDrivers, key = { it.id }) { driver ->
                            DriverCard(driver) {
                                val number = driver.phone.filter { it.isDigit() || it == '+' }
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
                            }
                        }
                    }
                }
                ResourcesTab.VEHICLES -> items(state.vehicles.sortedBy { it.status.ordinal }, key = { it.id }) { VehicleCard(it) }
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

    if (state.assigning) AssignSheet(state, viewModel)
}

private fun driverStatusLabel(status: DriverStatus) = when (status) {
    DriverStatus.AVAILABLE -> R.string.drv_status_available
    DriverStatus.ON_ROUTE -> R.string.drv_status_on_route
    DriverStatus.OFF_DUTY -> R.string.drv_status_off_duty
}

private fun vehicleTypeLabel(type: VehicleType) = when (type) {
    VehicleType.HEAVY_REEFER -> R.string.veh_type_heavy
    VehicleType.REEFER_TRUCK -> R.string.veh_type_truck
    VehicleType.MEDIUM_REEFER_VAN -> R.string.veh_type_van
    VehicleType.LIGHT_REEFER -> R.string.veh_type_light
}

@Composable
private fun DriverCard(driver: Driver, onCall: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val license = LicenseValidity.checkValidity(driver.licenseExpiry)
    val expired = license is LicenseValidityState.Expired
    val (badgeType, badgeRes) = when (driver.status) {
        DriverStatus.AVAILABLE -> StatusBadgeType.SUCCESS to R.string.drv_status_available
        DriverStatus.ON_ROUTE -> StatusBadgeType.INFO to R.string.drv_status_on_route
        DriverStatus.OFF_DUTY -> StatusBadgeType.NEUTRAL to R.string.drv_status_off_duty
    }
    val dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(appLocale())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .whiteCard()
            .padding(FruitLogixTheme.spacing.inner)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(FruitLogixTheme.sizing.iconBox)
                    .clip(CircleShape)
                    .background(scheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(initialsOf(driver.name), style = FruitLogixTheme.typography.titleSmall, color = scheme.onSecondaryContainer)
            }
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
            Column(modifier = Modifier.weight(1f)) {
                Text(driver.name, style = FruitLogixTheme.typography.titleMedium, color = scheme.onSurface)
                Text(stringResource(R.string.drv_license, driver.licenseClass), style = FruitLogixTheme.extraTypography.dataMonoSmall, color = scheme.onSurfaceVariant)
            }
            StatusBadge(text = stringResource(badgeRes), type = badgeType, maxLines = 1, softWrap = false)
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        when {
            expired -> Text(
                text = stringResource(R.string.drv_license_expired, driver.licenseExpiry.format(dateFormat)),
                style = FruitLogixTheme.typography.bodyMedium,
                color = scheme.error
            )
            license is LicenseValidityState.ExpiringSoon -> Text(
                text = stringResource(R.string.drv_license_expires, license.daysRemaining),
                style = FruitLogixTheme.typography.bodyMedium,
                color = colors.attentionContent
            )
        }
        driver.assignment?.let {
            Text(stringResource(R.string.drv_assignment, it.unitCode, it.plate, it.orderId), style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurface)
            Text(it.route, style = FruitLogixTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
        }
        driver.restUntil?.let {
            Text(stringResource(R.string.drv_rest_until, formatRelativeDateTime(it)), style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (driver.coldChainCertified) {
                AppIcon(id = R.drawable.ic_logistics_snowflake, contentDescription = null, tint = colors.coldContent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.drv_cold_chain), style = FruitLogixTheme.typography.labelMedium, color = colors.coldContent)
            }
            Spacer(modifier = Modifier.weight(1f))
            TextActionButton(text = stringResource(R.string.drv_call), onClick = onCall)
        }
    }
}

@Composable
private fun VehicleCard(vehicle: Vehicle) {
    val scheme = MaterialTheme.colorScheme
    val (badgeType, badgeRes) = when (vehicle.status) {
        VehicleStatus.AVAILABLE -> StatusBadgeType.SUCCESS to R.string.drv_status_available
        VehicleStatus.ON_ROUTE -> StatusBadgeType.INFO to R.string.drv_status_on_route
        VehicleStatus.MAINTENANCE -> StatusBadgeType.WARNING to R.string.drv_status_maintenance
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .whiteCard()
            .padding(FruitLogixTheme.spacing.inner)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(id = R.drawable.ic_fleet_truck, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
            Text(vehicle.unitCode, style = FruitLogixTheme.extraTypography.dataMono, color = scheme.onSurface)
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
            Text(vehicle.plate, style = FruitLogixTheme.extraTypography.dataMonoSmall, color = scheme.onSurfaceVariant)
            Spacer(modifier = Modifier.weight(1f))
            StatusBadge(text = stringResource(badgeRes), type = badgeType, maxLines = 1, softWrap = false)
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        Text(stringResource(vehicleTypeLabel(vehicle.type)), style = FruitLogixTheme.typography.titleMedium, color = scheme.onSurface)
        Text(
            text = "${stringResource(R.string.veh_capacity, formatDecimal(vehicle.capacityTons))} · " +
                stringResource(R.string.veh_range, "${formatDecimal(vehicle.minCelsius)} °C", "${formatDecimal(vehicle.maxCelsius)} °C"),
            style = FruitLogixTheme.extraTypography.dataMonoSmall,
            color = scheme.onSurfaceVariant
        )
        Text(
            text = when (vehicle.status) {
                VehicleStatus.ON_ROUTE -> stringResource(R.string.veh_driven_by, vehicle.driverName.orEmpty())
                VehicleStatus.AVAILABLE -> stringResource(R.string.veh_at_hub)
                VehicleStatus.MAINTENANCE -> stringResource(R.string.veh_in_workshop)
            },
            style = FruitLogixTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )
    }
}

// US79 -------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssignSheet(state: DriversVehiclesUiState, viewModel: DriversVehiclesViewModel) {
    val scheme = MaterialTheme.colorScheme
    var orderId by rememberSaveable { mutableStateOf(state.pending.singleOrNull()?.orderId) }
    var driverId by rememberSaveable { mutableStateOf<String?>(null) }
    var vehicleId by rememberSaveable { mutableStateOf<String?>(null) }
    val error = state.assignError?.let { res ->
        if (state.assignErrorArgs.isEmpty()) stringResource(res) else stringResource(res, *state.assignErrorArgs.toTypedArray())
    }
    // Only people and trucks that can actually leave are offered
    val drivers = state.drivers.filter { it.status == DriverStatus.AVAILABLE }
    val vehicles = state.vehicles.filter { it.status == VehicleStatus.AVAILABLE }

    ModalBottomSheet(
        onDismissRequest = viewModel::closeAssign,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = scheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = FruitLogixTheme.spacing.md)
                .padding(bottom = FruitLogixTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)
        ) {
            Text(stringResource(R.string.drv_assign), style = FruitLogixTheme.typography.titleLarge, color = scheme.onSurface)
            Text(stringResource(R.string.asg_message), style = FruitLogixTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
            error?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(FruitLogixTheme.shapes.Button)
                        .background(scheme.errorContainer)
                        .padding(FruitLogixTheme.spacing.inner),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIcon(id = R.drawable.ic_warning, contentDescription = null, tint = scheme.onErrorContainer, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
                    Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                    Text(it, style = FruitLogixTheme.typography.bodyMedium, color = scheme.onErrorContainer)
                }
            }

            SheetLabel(stringResource(R.string.asg_order))
            if (state.pending.isEmpty()) {
                Text(stringResource(R.string.asg_no_orders), style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
            }
            state.pending.forEach { order ->
                ChoiceRow(
                    title = "${order.orderId} · ${order.clientName}",
                    detail = stringResource(R.string.asg_order_line, order.productName, order.clientName, formatDecimal(order.quantityTons)),
                    selected = orderId == order.orderId,
                    enabled = true,
                    onSelect = { orderId = order.orderId }
                )
            }

            SheetLabel(stringResource(R.string.asg_driver))
            drivers.forEach { driver ->
                val canDrive = AssignmentRules.canDrive(driver)
                ChoiceRow(
                    title = driver.name,
                    detail = if (canDrive) stringResource(R.string.drv_license, driver.licenseClass) else stringResource(R.string.asg_error_license),
                    selected = driverId == driver.id,
                    enabled = canDrive,
                    onSelect = { driverId = driver.id }
                )
            }

            SheetLabel(stringResource(R.string.asg_vehicle))
            vehicles.forEach { vehicle ->
                ChoiceRow(
                    title = "${vehicle.unitCode} · ${vehicle.plate}",
                    detail = stringResource(R.string.asg_vehicle_line, stringResource(vehicleTypeLabel(vehicle.type)), "${formatDecimal(vehicle.minCelsius)}–${formatDecimal(vehicle.maxCelsius)} °C", formatDecimal(vehicle.capacityTons)),
                    selected = vehicleId == vehicle.id,
                    enabled = true,
                    onSelect = { vehicleId = vehicle.id }
                )
            }

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
            PrimaryButton(
                text = stringResource(R.string.asg_confirm),
                onClick = { viewModel.assign(orderId, driverId, vehicleId) },
                enabled = state.pending.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SheetLabel(text: String) {
    Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
    Text(text, style = FruitLogixTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
}

@Composable
private fun ChoiceRow(title: String, detail: String, selected: Boolean, enabled: Boolean, onSelect: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = FruitLogixTheme.sizing.fieldTouch)
            .clip(FruitLogixTheme.shapes.Button)
            .background(if (selected) scheme.secondaryContainer else scheme.surfaceContainerLowest)
            .border(if (selected) 2.dp else 1.dp, if (selected) scheme.primary else scheme.outlineVariant, FruitLogixTheme.shapes.Button)
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onSelect)
            .padding(horizontal = FruitLogixTheme.spacing.xs, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null, enabled = enabled)
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = FruitLogixTheme.typography.titleSmall, color = if (enabled) scheme.onSurface else scheme.onSurfaceVariant)
            Text(detail, style = FruitLogixTheme.typography.bodySmall, color = if (enabled) scheme.onSurfaceVariant else scheme.error)
        }
    }
}
