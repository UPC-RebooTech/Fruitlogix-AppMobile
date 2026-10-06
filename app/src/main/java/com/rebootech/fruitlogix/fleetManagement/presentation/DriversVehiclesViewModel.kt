package com.rebootech.fruitlogix.fleetManagement.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.fleetManagement.data.FakeDriverRepository
import com.rebootech.fruitlogix.fleetManagement.domain.model.Driver
import com.rebootech.fruitlogix.fleetManagement.domain.model.DriverStatus
import com.rebootech.fruitlogix.fleetManagement.domain.repository.DriverRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DriversVehiclesViewModel(
    private val repository: DriverRepository = FakeDriverRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriversVehiclesUiState())
    val uiState: StateFlow<DriversVehiclesUiState> = _uiState.asStateFlow()

    init {
        loadDrivers()
    }

    fun loadDrivers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getDrivers().collect { driversList ->
                val availCount = driversList.count { it.status == DriverStatus.AVAILABLE }
                val routeCount = driversList.count { it.status == DriverStatus.ON_ROUTE }
                val offCount = driversList.count { it.status == DriverStatus.OFF_DUTY }

                _uiState.update { state ->
                    val filtered = filterDriversList(driversList, state.selectedFilter)
                    state.copy(
                        drivers = driversList,
                        filteredDrivers = filtered,
                        availableCount = availCount,
                        onRouteCount = routeCount,
                        offDutyCount = offCount,
                        totalDriversCount = driversList.size,
                        isLoading = false,
                        isEmpty = driversList.isEmpty()
                    )
                }
            }
        }
    }

    fun onTabSelected(tab: FleetTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onFilterSelected(filter: DriverFilter) {
        _uiState.update { state ->
            val filtered = filterDriversList(state.drivers, filter)
            state.copy(
                selectedFilter = filter,
                filteredDrivers = filtered
            )
        }
    }

    private fun filterDriversList(
        drivers: List<Driver>,
        filter: DriverFilter
    ): List<Driver> {
        return when (filter) {
            DriverFilter.ALL -> drivers
            DriverFilter.AVAILABLE -> drivers.filter { it.status == DriverStatus.AVAILABLE }
            DriverFilter.ON_ROUTE -> drivers.filter { it.status == DriverStatus.ON_ROUTE }
            DriverFilter.OFF_DUTY -> drivers.filter { it.status == DriverStatus.OFF_DUTY }
        }
    }
}
