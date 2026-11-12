package com.rebootech.fruitlogix.distributor.fleetManagement.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.DistributorDependencies
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Driver
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.DriverStatus
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.PendingDispatch
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Vehicle
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.repository.DriverRepository
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.rules.AssignmentRules
import com.rebootech.fruitlogix.shared.core.model.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat

enum class ResourcesTab { DRIVERS, VEHICLES }

data class DriversVehiclesUiState(
    val drivers: List<Driver> = emptyList(),
    val vehicles: List<Vehicle> = emptyList(),
    val pending: List<PendingDispatch> = emptyList(),
    val tab: ResourcesTab = ResourcesTab.DRIVERS,
    val status: DriverStatus? = null,
    val assigning: Boolean = false,
    @field:StringRes val assignError: Int? = null,
    val assignErrorArgs: List<String> = emptyList(),
    val message: UiText? = null
) {
    /** Available first (they can be assigned), then on route, then resting. */
    val visibleDrivers: List<Driver>
        get() = drivers.filter { status == null || it.status == status }.sortedBy { it.status.ordinal }

    fun count(value: DriverStatus) = drivers.count { it.status == value }
}

/** Drivers and vehicles, and the assignment of both to an order (US79). */
class DriversVehiclesViewModel(private val repository: DriverRepository) : ViewModel() {

    /** Used by the default ViewModel factory. */
    constructor() : this(DistributorDependencies.driverRepository)

    private val local = MutableStateFlow(DriversVehiclesUiState())

    val uiState: StateFlow<DriversVehiclesUiState> =
        combine(repository.drivers, repository.vehicles, repository.pendingDispatches, local) { drivers, vehicles, pending, local ->
            local.copy(drivers = drivers, vehicles = vehicles, pending = pending)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DriversVehiclesUiState())

    fun onTabChange(value: ResourcesTab) = local.update { it.copy(tab = value) }
    fun onStatusChange(value: DriverStatus?) = local.update { it.copy(status = value) }
    fun openAssign() = local.update { it.copy(assigning = true, assignError = null) }
    fun closeAssign() = local.update { it.copy(assigning = false, assignError = null) }
    fun onMessageShown() = local.update { it.copy(message = null) }

    fun assign(orderId: String?, driverId: String?, vehicleId: String?) {
        val state = uiState.value
        val order = state.pending.firstOrNull { it.orderId == orderId }
        val driver = state.drivers.firstOrNull { it.id == driverId }
        val vehicle = state.vehicles.firstOrNull { it.id == vehicleId }
        val tons = NumberFormat.getNumberInstance().apply { maximumFractionDigits = 1 }
        when (AssignmentRules.validate(order, driver, vehicle)) {
            AssignmentRules.Error.ORDER_REQUIRED -> error(R.string.asg_error_order)
            AssignmentRules.Error.DRIVER_REQUIRED -> error(R.string.asg_error_driver)
            AssignmentRules.Error.VEHICLE_REQUIRED -> error(R.string.asg_error_vehicle)
            AssignmentRules.Error.DRIVER_NOT_AVAILABLE -> error(R.string.asg_error_driver_busy)
            AssignmentRules.Error.LICENSE_EXPIRED -> error(R.string.asg_error_license)
            AssignmentRules.Error.VEHICLE_NOT_AVAILABLE -> error(R.string.asg_error_vehicle_busy)
            AssignmentRules.Error.OVER_CAPACITY ->
                error(R.string.asg_error_capacity, listOf(tons.format(order!!.quantityTons), tons.format(vehicle!!.capacityTons)))
            null -> viewModelScope.launch {
                repository.assign(order!!.orderId, driver!!.id, vehicle!!.id)
                local.update {
                    it.copy(
                        assigning = false, assignError = null,
                        message = UiText.StringResource(R.string.asg_done, arrayOf(order.orderId, driver.name, "${vehicle.unitCode} (${vehicle.plate})"))
                    )
                }
            }
        }
    }

    private fun error(@StringRes res: Int, args: List<String> = emptyList()) =
        local.update { it.copy(assignError = res, assignErrorArgs = args) }
}
