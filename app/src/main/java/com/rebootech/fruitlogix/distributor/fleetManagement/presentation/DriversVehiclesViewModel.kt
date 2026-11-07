package com.rebootech.fruitlogix.distributor.fleetManagement.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.distributor.fleetManagement.data.FakeDriverRepository
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Driver
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.DriverStatus
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.repository.DriverRepository
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.service.LicenseValidity
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.service.LicenseValidityState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DriversVehiclesViewModel(
    private val repository: DriverRepository = FakeDriverRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriversVehiclesUiState(isLoading = true))
    val uiState: StateFlow<DriversVehiclesUiState> = _uiState.asStateFlow()

    init {
        loadDrivers()
    }

    fun loadDrivers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            combine(
                repository.getDrivers(),
                repository.getVehicles(),
                repository.getPendingDispatches()
            ) { rawDrivers, vehiclesList, pendingList ->
                Triple(rawDrivers, vehiclesList, pendingList)
            }.collect { (rawDrivers, vehiclesList, pendingList) ->
                val driversList = rawDrivers.map { driver ->
                    val validity = LicenseValidity.checkValidity(driver.licenseExpiry)
                    if (validity is LicenseValidityState.Expired) {
                        driver.copy(status = DriverStatus.EXPIRED)
                    } else {
                        driver
                    }
                }

                val availCount = driversList.count { it.status == DriverStatus.AVAILABLE }
                val routeCount = driversList.count { it.status == DriverStatus.ON_ROUTE }
                val offCount = driversList.count { it.status == DriverStatus.OFF_DUTY }

                _uiState.update { state ->
                    val filtered = filterDriversList(driversList, state.selectedFilter)
                    state.copy(
                        drivers = driversList,
                        filteredDrivers = filtered,
                        vehicles = vehiclesList,
                        pendingDispatches = pendingList,
                        availableCount = availCount,
                        onRouteCount = routeCount,
                        offDutyCount = offCount,
                        totalDriversCount = driversList.size,
                        vehiclesCount = vehiclesList.size,
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

    fun onDriverClick(driverId: String) {
        val found = _uiState.value.drivers.find { it.id == driverId }
        _uiState.update { it.copy(selectedDriverDetail = found) }
    }

    fun onDismissDriverDetail() {
        _uiState.update { it.copy(selectedDriverDetail = null) }
    }

    fun onOpenAssignDialog() {
        _uiState.update { it.copy(showAssignDialog = true, assignmentError = null) }
    }

    fun onDismissAssignDialog() {
        _uiState.update { it.copy(showAssignDialog = false, assignmentError = null) }
    }

    fun assignResources(dispatchId: String, driverId: String, vehicleId: String) {
        val result = repository.assignResourcesToDispatch(dispatchId, driverId, vehicleId)
        result.fold(
            onSuccess = { msg ->
                _uiState.update {
                    it.copy(
                        showAssignDialog = false,
                        assignmentFeedback = msg,
                        assignmentError = null
                    )
                }
            },
            onFailure = { err ->
                _uiState.update {
                    it.copy(assignmentError = err.message)
                }
            }
        )
    }

    fun clearFeedback() {
        _uiState.update { it.copy(assignmentFeedback = null, assignmentError = null) }
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