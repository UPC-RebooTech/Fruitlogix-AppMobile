package com.rebootech.fruitlogix.fleetManagement.presentation

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.fleetManagement.domain.model.Driver
import com.rebootech.fruitlogix.fleetManagement.domain.model.DriverAssignment
import com.rebootech.fruitlogix.fleetManagement.domain.model.DriverStatus
import com.rebootech.fruitlogix.fleetManagement.presentation.components.DriverCard
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.KpiCard
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily
import java.time.LocalDate

@Composable
fun DriversVehiclesScreen(
    onDriverClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: DriversVehiclesViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    DriversVehiclesContent(
        state = state,
        onTabSelected = viewModel::onTabSelected,
        onFilterSelected = viewModel::onFilterSelected,
        onDriverClick = onDriverClick,
        modifier = modifier
    )
}

@Composable
fun DriversVehiclesContent(
    state: DriversVehiclesUiState,
    onTabSelected: (FleetTab) -> Unit,
    onFilterSelected: (DriverFilter) -> Unit,
    onDriverClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FruitLogixTheme.colors.bg),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Header Section
        item {
            HeaderSection()
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = FruitLogixTheme.spacing.sm)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Segmented Tabs
                SegmentedTabs(
                    selectedTab = state.selectedTab,
                    driversCount = state.totalDriversCount,
                    vehiclesCount = state.vehiclesCount,
                    onTabSelected = onTabSelected
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.selectedTab == FleetTab.VEHICLES) {
                    VehiclesPlaceholderCard()
                } else {
                    // Drivers Tab Content

                    // 3 KPI Tiles: Available, On route, Off duty
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

                    // Filter Chips
                    FilterChipsRow(
                        selectedFilter = state.selectedFilter,
                        onFilterSelected = onFilterSelected
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Drivers List / Loading / Empty State
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
        }
    }
}

@Composable
private fun HeaderSection() {
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

            // READ-ONLY Chip
            Box(
                modifier = Modifier
                    .clip(FruitLogixTheme.shapes.Pill)
                    .background(FruitLogixTheme.colors.surfaceDark)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(
                        id = R.drawable.ic_fleet_lock,
                        contentDescription = null,
                        tint = FruitLogixTheme.colors.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.fleet_read_only_chip),
                        style = FruitLogixTheme.typography.labelSmall,
                        color = FruitLogixTheme.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
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
        // Drivers Tab
        SegmentedTabItem(
            title = stringResource(R.string.fleet_drivers_tab_drivers, driversCount),
            iconRes = R.drawable.ic_fleet_driver,
            isSelected = selectedTab == FleetTab.DRIVERS,
            onClick = { onTabSelected(FleetTab.DRIVERS) },
            modifier = Modifier.weight(1f)
        )

        // Vehicles Tab
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
private fun VehiclesPlaceholderCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
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
                id = R.drawable.ic_fleet_truck,
                contentDescription = null,
                tint = FruitLogixTheme.colors.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.fleet_drivers_vehicles_placeholder),
                style = FruitLogixTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FruitLogixTheme.colors.textOnDark
            )
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
        ),
        Driver(
            id = "driver_2",
            name = "Mateo Silva",
            licenseClass = "A-IIIc",
            specialty = "Cold-chain certified",
            licenseExpiry = LocalDate.of(2028, 3, 31),
            status = DriverStatus.ON_ROUTE,
            currentAssignment = DriverAssignment(
                unitCode = "FL-408",
                plateNumber = "AZT-901",
                routeName = "Chavín de Huántar → Callao Cold Hub"
            )
        ),
        Driver(
            id = "driver_3",
            name = "Carlos Mendoza Jr.",
            licenseClass = "A-IIIc",
            specialty = "Freight senior",
            licenseExpiry = LocalDate.now().plusDays(20),
            status = DriverStatus.AVAILABLE,
            currentAssignment = null
        ),
        Driver(
            id = "driver_4",
            name = "Luis Quispe",
            licenseClass = "A-IIb",
            specialty = "Regional carrier",
            licenseExpiry = LocalDate.of(2026, 8, 31),
            status = DriverStatus.OFF_DUTY,
            currentAssignment = DriverAssignment(
                restPeriodEnds = "Rest period ends tomorrow 06:00 PET"
            )
        ),
        Driver(
            id = "driver_5",
            name = "Ricardo Paredes",
            licenseClass = "A-IIIc",
            specialty = "Heavy cargo",
            licenseExpiry = LocalDate.of(2023, 10, 12),
            status = DriverStatus.EXPIRED,
            currentAssignment = DriverAssignment(
                cannotBeAssignedReason = "Cannot be assigned • License expired (12/10/2023)"
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

@Preview(showBackground = true)
@Composable
private fun DriversVehiclesEmptyStatePreview() {
    val state = DriversVehiclesUiState(
        drivers = emptyList(),
        filteredDrivers = emptyList(),
        availableCount = 0,
        onRouteCount = 0,
        offDutyCount = 0,
        vehiclesCount = 4,
        totalDriversCount = 0
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
