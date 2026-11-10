package com.rebootech.fruitlogix.distributor.fleetManagement.presentation

import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Driver
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.PendingDispatchOption
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Vehicle

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
    val vehicles: List<Vehicle> = emptyList(),
    val pendingDispatches: List<PendingDispatchOption> = emptyList(),
    val selectedDriverDetail: Driver? = null,
    val showAssignDialog: Boolean = false,
    val assignmentFeedback: String? = null,
    val assignmentError: String? = null,
    val availableCount: Int = 0,
    val onRouteCount: Int = 0,
    val offDutyCount: Int = 0,
    val vehiclesCount: Int = 4,
    val totalDriversCount: Int = 5,
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false
)