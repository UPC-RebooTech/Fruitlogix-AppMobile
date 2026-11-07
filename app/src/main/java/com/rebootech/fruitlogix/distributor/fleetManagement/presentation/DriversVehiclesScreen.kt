package com.rebootech.fruitlogix.distributor.fleetManagement.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Driver
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.DriverAssignment
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.DriverStatus
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.PendingDispatchOption
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Vehicle
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleStatus
import com.rebootech.fruitlogix.distributor.fleetManagement.presentation.components.DriverCard
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.KpiCard
import com.rebootech.fruitlogix.shared.ui.theme.ColorAppbar
import com.rebootech.fruitlogix.shared.ui.theme.ColorDanger
import com.rebootech.fruitlogix.shared.ui.theme.ColorDangerStrong
import com.rebootech.fruitlogix.shared.ui.theme.ColorOnPrimary
import com.rebootech.fruitlogix.shared.ui.theme.ColorPrimary
import com.rebootech.fruitlogix.shared.ui.theme.ColorSuccess
import com.rebootech.fruitlogix.shared.ui.theme.ColorSurfaceDark
import com.rebootech.fruitlogix.shared.ui.theme.ColorTextMuted
import com.rebootech.fruitlogix.shared.ui.theme.ColorTextOnDark
import com.rebootech.fruitlogix.shared.ui.theme.ColorWarning
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DriversVehiclesScreen(
    onDriverClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: DriversVehiclesViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    DriversVehiclesContent(
        state = state,
        onTabSelected = viewModel::onTabSelected,
        onFilterSelected = viewModel::onFilterSelected,
        onDriverClick = { driverId ->
            viewModel.onDriverClick(driverId)
            onDriverClick(driverId)
        },
        onOpenAssignClick = viewModel::onOpenAssignDialog,
        onClearFeedback = viewModel::clearFeedback,
        modifier = modifier
    )

    state.selectedDriverDetail?.let { driver ->
        DriverDetailModal(
            driver = driver,
            onDismiss = viewModel::onDismissDriverDetail,
            onCallDriver = { phone ->
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                context.startActivity(intent)
            }
        )
    }

    if (state.showAssignDialog) {
        AssignDriverVehicleDialog(
            dispatches = state.pendingDispatches,
            drivers = state.drivers,
            vehicles = state.vehicles,
            errorMessage = state.assignmentError,
            onDismiss = viewModel::onDismissAssignDialog,
            onConfirmAssign = { dispatchId, driverId, vehicleId ->
                viewModel.assignResources(dispatchId, driverId, vehicleId)
            }
        )
    }
}

@Composable
fun DriversVehiclesContent(
    state: DriversVehiclesUiState,
    onTabSelected: (FleetTab) -> Unit,
    onFilterSelected: (DriverFilter) -> Unit,
    onDriverClick: (String) -> Unit,
    onOpenAssignClick: () -> Unit = {},
    onClearFeedback: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FruitLogixTheme.colors.bg),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            HeaderSection(onOpenAssignClick = onOpenAssignClick)
        }

        state.assignmentFeedback?.let { feedback ->
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = FruitLogixTheme.spacing.sm, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ColorSurfaceDark)
                        .border(1.dp, ColorPrimary, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = feedback,
                        color = ColorPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onClearFeedback) {
                        Text("OK", color = ColorTextOnDark, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = FruitLogixTheme.spacing.sm)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                SegmentedTabs(
                    selectedTab = state.selectedTab,
                    driversCount = state.totalDriversCount,
                    vehiclesCount = state.vehiclesCount,
                    onTabSelected = onTabSelected
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.selectedTab == FleetTab.DRIVERS) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KpiCard(
                            caption = stringResource(R.string.fleet_drivers_kpi_available),
                            value = state.availableCount.toString(),
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(FruitLogixTheme.shapes.Pill)
                                        .background(FruitLogixTheme.colors.success)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        KpiCard(
                            caption = stringResource(R.string.fleet_drivers_kpi_on_route),
                            value = state.onRouteCount.toString(),
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(FruitLogixTheme.shapes.Pill)
                                        .background(FruitLogixTheme.colors.primary)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        KpiCard(
                            caption = stringResource(R.string.fleet_drivers_kpi_off_duty),
                            value = state.offDutyCount.toString(),
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(FruitLogixTheme.shapes.Pill)
                                        .background(FruitLogixTheme.colors.textMuted)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    FilterChipsRow(
                        selectedFilter = state.selectedFilter,
                        onFilterSelected = onFilterSelected
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (state.selectedTab == FleetTab.DRIVERS) {
            when {
                state.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = FruitLogixTheme.colors.primary)
                        }
                    }
                }
                state.filteredDrivers.isEmpty() -> {
                    item {
                        EmptyDriversCard()
                    }
                }
                else -> {
                    items(
                        items = state.filteredDrivers,
                        key = { it.id }
                    ) { driver ->
                        Box(
                            modifier = Modifier.padding(
                                horizontal = FruitLogixTheme.spacing.sm,
                                vertical = 6.dp
                            )
                        ) {
                            DriverCard(
                                driver = driver,
                                onDetailClick = onDriverClick
                            )
                        }
                    }
                }
            }
        } else {
            if (state.vehicles.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = FruitLogixTheme.spacing.sm),
                        shape = FruitLogixTheme.shapes.Card,
                        colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark)
                    ) {
                        Text(
                            text = "No hay vehículos registrados en la flota.",
                            color = ColorTextOnDark,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            } else {
                items(
                    items = state.vehicles,
                    key = { v -> v.id }
                ) { vehicle ->
                    Box(
                        modifier = Modifier.padding(
                            horizontal = FruitLogixTheme.spacing.sm,
                            vertical = 6.dp
                        )
                    ) {
                        VehicleItemCard(vehicle = vehicle)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(onOpenAssignClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(FruitLogixTheme.colors.appbar)
            .padding(
                horizontal = FruitLogixTheme.spacing.sm,
                vertical = FruitLogixTheme.spacing.sm
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.screen_fleet_resources_title),
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                style = FruitLogixTheme.typography.titleLarge,
                color = FruitLogixTheme.colors.textOnDark
            )

            Button(
                onClick = onOpenAssignClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary,
                    contentColor = ColorOnPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "+ Asignar Flota",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(
                id = R.drawable.ic_fleet_info,
                contentDescription = null,
                tint = FruitLogixTheme.colors.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.fleet_web_portal_note),
                style = FruitLogixTheme.typography.bodySmall,
                color = FruitLogixTheme.colors.textMuted
            )
        }
    }
}

@Composable
private fun VehicleItemCard(vehicle: Vehicle) {
    val badgeBg = when (vehicle.status) {
        VehicleStatus.AVAILABLE -> ColorSuccess.copy(alpha = 0.18f)
        VehicleStatus.ON_ROUTE -> ColorPrimary.copy(alpha = 0.18f)
        VehicleStatus.MAINTENANCE -> ColorWarning.copy(alpha = 0.18f)
    }
    val badgeText = when (vehicle.status) {
        VehicleStatus.AVAILABLE -> ColorSuccess
        VehicleStatus.ON_ROUTE -> ColorPrimary
        VehicleStatus.MAINTENANCE -> ColorWarning
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ColorAppbar),
                        contentAlignment = Alignment.Center
                    ) {
                        AppIcon(
                            id = R.drawable.ic_fleet_truck,
                            contentDescription = null,
                            tint = ColorPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${vehicle.unitCode} • Placa ${vehicle.plate}",
                            color = ColorTextOnDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = vehicle.type,
                            color = ColorTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = vehicle.status.label,
                        color = badgeText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = ColorAppbar, thickness = 0.7.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Capacidad: ${vehicle.capacityTons} Tons",
                    color = ColorPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Rango Reefer: ${vehicle.tempRange}",
                    color = ColorTextMuted,
                    fontSize = 12.sp
                )
            }

            vehicle.currentRoute?.let { routeInfo ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Estado operativo: $routeInfo",
                    color = ColorTextOnDark,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun DriverDetailModal(
    driver: Driver,
    onDismiss: () -> Unit,
    onCallDriver: (String) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val isExpired = driver.status == DriverStatus.EXPIRED
    val extractedPhone = driver.specialty.substringAfter("•", "+51 987 654 321").trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorSurfaceDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = driver.name,
                    color = ColorTextOnDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (isExpired) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ColorDanger)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Licencia vencida",
                            color = ColorDangerStrong,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Categoría de Licencia: ${driver.licenseClass}",
                    color = ColorTextOnDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Fecha de Vigencia: ${driver.licenseExpiry.format(formatter)}",
                    color = if (isExpired) ColorDangerStrong else ColorPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Certificación y Contacto: ${driver.specialty}",
                    color = ColorTextMuted,
                    fontSize = 12.sp
                )

                HorizontalDivider(color = ColorAppbar, modifier = Modifier.padding(vertical = 4.dp))

                val assignment = driver.currentAssignment
                if (assignment?.unitCode != null) {
                    Text(
                        text = "Despacho Actual: Unidad ${assignment.unitCode} (${assignment.plateNumber})",
                        color = ColorPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    assignment.routeName?.let { route ->
                        Text(
                            text = "Ruta: $route",
                            color = ColorTextOnDark,
                            fontSize = 12.sp
                        )
                    }
                } else if (assignment?.cannotBeAssignedReason != null) {
                    Text(
                        text = assignment.cannotBeAssignedReason,
                        color = ColorDangerStrong,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = assignment?.restPeriodEnds ?: "Disponible para asignación inmediata",
                        color = ColorTextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCallDriver(extractedPhone) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary,
                    contentColor = ColorOnPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Llamar Conductor", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = ColorTextMuted)
            }
        }
    )
}

@Composable
private fun AssignDriverVehicleDialog(
    dispatches: List<PendingDispatchOption>,
    drivers: List<Driver>,
    vehicles: List<Vehicle>,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirmAssign: (dispatchId: String, driverId: String, vehicleId: String) -> Unit
) {
    var selectedDispatchId by remember { mutableStateOf(dispatches.firstOrNull()?.dispatchId ?: "") }
    var selectedDriverId by remember { mutableStateOf(drivers.firstOrNull { it.status == DriverStatus.AVAILABLE }?.id ?: "") }
    var selectedVehicleId by remember { mutableStateOf(vehicles.firstOrNull { it.status == VehicleStatus.AVAILABLE }?.id ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorSurfaceDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Asignar Conductor y Vehículo (US79)",
                color = ColorTextOnDark,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (dispatches.isEmpty()) {
                Text(
                    text = "Todos los despachos pendientes ya cuentan con conductor y vehículo asignados.",
                    color = ColorPrimary,
                    fontSize = 13.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = ColorDangerStrong,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text("1. Despacho pendiente:", color = ColorPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    dispatches.forEach { d ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDispatchId = d.dispatchId },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedDispatchId == d.dispatchId,
                                onClick = { selectedDispatchId = d.dispatchId },
                                colors = RadioButtonDefaults.colors(selectedColor = ColorPrimary)
                            )
                            Column {
                                Text("${d.orderCode} • ${d.productDescription}", color = ColorTextOnDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(d.destination, color = ColorTextMuted, fontSize = 11.sp)
                            }
                        }
                    }

                    Text("2. Selecciona Conductor:", color = ColorPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    drivers.forEach { drv ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDriverId = drv.id },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedDriverId == drv.id,
                                onClick = { selectedDriverId = drv.id },
                                colors = RadioButtonDefaults.colors(selectedColor = ColorPrimary)
                            )
                            Text(
                                text = "${drv.name} (${drv.status.name})",
                                color = if (drv.status == DriverStatus.AVAILABLE) ColorTextOnDark else ColorTextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text("3. Selecciona Vehículo Reefer:", color = ColorPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    vehicles.forEach { veh ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedVehicleId = veh.id },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedVehicleId == veh.id,
                                onClick = { selectedVehicleId = veh.id },
                                colors = RadioButtonDefaults.colors(selectedColor = ColorPrimary)
                            )
                            Text(
                                text = "${veh.plate} • ${veh.unitCode} (${veh.status.label})",
                                color = if (veh.status == VehicleStatus.AVAILABLE) ColorTextOnDark else ColorTextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (dispatches.isNotEmpty()) {
                Button(
                    onClick = {
                        onConfirmAssign(selectedDispatchId, selectedDriverId, selectedVehicleId)
                    },
                    enabled = selectedDispatchId.isNotBlank() && selectedDriverId.isNotBlank() && selectedVehicleId.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary, contentColor = ColorOnPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirmar Asignación", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = ColorTextMuted)
            }
        }
    )
}

@Composable
private fun SegmentedTabs(
    selectedTab: FleetTab,
    driversCount: Int,
    vehiclesCount: Int,
    onTabSelected: (FleetTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FruitLogixTheme.shapes.Pill)
            .background(FruitLogixTheme.colors.surfaceDark)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SegmentedTabItem(
            title = stringResource(R.string.fleet_drivers_tab_drivers, driversCount),
            iconRes = R.drawable.ic_fleet_driver,
            isSelected = selectedTab == FleetTab.DRIVERS,
            onClick = { onTabSelected(FleetTab.DRIVERS) },
            modifier = Modifier.weight(1f)
        )

        SegmentedTabItem(
            title = stringResource(R.string.fleet_drivers_tab_vehicles, vehiclesCount),
            iconRes = R.drawable.ic_fleet_truck,
            isSelected = selectedTab == FleetTab.VEHICLES,
            onClick = { onTabSelected(FleetTab.VEHICLES) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SegmentedTabItem(
    title: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) {
        FruitLogixTheme.colors.primary
    } else {
        FruitLogixTheme.colors.surfaceDark
    }
    val contentColor = if (isSelected) {
        FruitLogixTheme.colors.onPrimary
    } else {
        FruitLogixTheme.colors.textMuted
    }

    Box(
        modifier = modifier
            .clip(FruitLogixTheme.shapes.Pill)
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(
                id = iconRes,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                style = FruitLogixTheme.typography.bodyMedium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun FilterChipsRow(
    selectedFilter: DriverFilter,
    onFilterSelected: (DriverFilter) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val filters = listOf(
            DriverFilter.ALL to R.string.fleet_drivers_filter_all,
            DriverFilter.AVAILABLE to R.string.fleet_drivers_filter_available,
            DriverFilter.ON_ROUTE to R.string.fleet_drivers_filter_on_route,
            DriverFilter.OFF_DUTY to R.string.fleet_drivers_filter_off_duty
        )

        items(filters) { (filter, labelRes) ->
            val isSelected = selectedFilter == filter
            val bg = if (isSelected) FruitLogixTheme.colors.primary else FruitLogixTheme.colors.surfaceDark
            val textColor = if (isSelected) FruitLogixTheme.colors.onPrimary else FruitLogixTheme.colors.textMuted

            Box(
                modifier = Modifier
                    .clip(FruitLogixTheme.shapes.Pill)
                    .background(bg)
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(labelRes),
                    style = FruitLogixTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun EmptyDriversCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FruitLogixTheme.spacing.sm, vertical = 16.dp),
        shape = FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = FruitLogixTheme.colors.surfaceDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppIcon(
                id = R.drawable.ic_fleet_driver,
                contentDescription = null,
                tint = FruitLogixTheme.colors.textMuted,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.fleet_drivers_empty_drivers),
                style = FruitLogixTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FruitLogixTheme.colors.textOnDark
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DriversVehiclesScreenPreview() {
    val sampleDrivers = listOf(
        Driver(
            id = "driver_1",
            name = "Jorge Huamán",
            licenseClass = "A-IIIc",
            specialty = "Cold-chain certified",
            licenseExpiry = LocalDate.of(2027, 11, 30),
            status = DriverStatus.ON_ROUTE,
            currentAssignment = DriverAssignment(
                unitCode = "FL-102",
                plateNumber = "BQK-482",
                routeName = "Ica → Lima Central Hub"
            )
        )
    )

    val state = DriversVehiclesUiState(
        drivers = sampleDrivers,
        filteredDrivers = sampleDrivers,
        availableCount = 1,
        onRouteCount = 2,
        offDutyCount = 1,
        vehiclesCount = 4,
        totalDriversCount = 5
    )

    FruitLogixTheme {
        DriversVehiclesContent(
            state = state,
            onTabSelected = {},
            onFilterSelected = {},
            onDriverClick = {}
        )
    }
}