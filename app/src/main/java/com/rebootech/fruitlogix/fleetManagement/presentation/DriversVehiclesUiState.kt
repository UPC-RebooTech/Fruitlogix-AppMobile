package com.rebootech.fruitlogix.fleetManagement.presentation

import com.rebootech.fruitlogix.fleetManagement.domain.model.Driver

enum class FleetTab {
    DRIVERS,
    VEHICLES
}

enum class DriverFilter {
    ALL,
    AVAILABLE,
    ON_ROUTE,
    OFF_DUTY
}

data class DriversVehiclesUiState(
    val selectedTab: FleetTab = FleetTab.DRIVERS,
    val selectedFilter: DriverFilter = DriverFilter.ALL,
    val drivers: List<Driver> = emptyList(),
    val filteredDrivers: List<Driver> = emptyList(),
    val availableCount: Int = 0,
    val onRouteCount: Int = 0,
    val offDutyCount: Int = 0,
    val vehiclesCount: Int = 4,
    val totalDriversCount: Int = 5,
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false
)
