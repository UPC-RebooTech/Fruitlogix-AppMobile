package com.rebootech.fruitlogix.distributor.fleetManagement.presentation

import androidx.annotation.StringRes
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.DistributorDependencies
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Driver
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.DriverDraft
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.DriverStatus
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.PendingDispatch
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Vehicle
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleDraft
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.repository.DriverRepository
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.rules.AssignmentRules
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.rules.FleetResourceRules
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
    /** Add-driver / add-vehicle forms and their errors. */
    val addingDriver: Boolean = false,
    val addingVehicle: Boolean = false,
    val driverErrors: Set<FleetResourceRules.DriverError> = emptySet(),
    val vehicleErrors: Set<FleetResourceRules.VehicleError> = emptySet(),
    val message: UiText? = null
) {
    /** Available first (they can be assigned), then on route, then resting. */
    val visibleDrivers: List<Driver>
        get() = drivers.filter { status == null || it.status == status }.sortedBy { it.status.ordinal }

    fun count(value: DriverStatus) = drivers.count { it.status == value }
}

/** Drivers and vehicles, and the assignment of both to an order (US79). */
class DriversVehiclesViewModel(
    application: Application,
    private val repository: DriverRepository
) : AndroidViewModel(application) {

    /** Used by the default ViewModel factory. */
    constructor(application: Application) : this(application, DistributorDependencies.driverRepository(application))

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

    fun openAddDriver() = local.update { it.copy(addingDriver = true, driverErrors = emptySet()) }
    fun openAddVehicle() = local.update { it.copy(addingVehicle = true, vehicleErrors = emptySet()) }
    fun closeAddForms() = local.update { it.copy(addingDriver = false, addingVehicle = false, driverErrors = emptySet(), vehicleErrors = emptySet()) }

    fun addDriver(draft: DriverDraft) {
        val errors = FleetResourceRules.validateDriver(draft, uiState.value.drivers)
        if (errors.isNotEmpty()) {
            local.update { it.copy(driverErrors = errors) }
            return
        }
        viewModelScope.launch {
            val driver = repository.addDriver(draft)
            local.update {
                it.copy(addingDriver = false, driverErrors = emptySet(), tab = ResourcesTab.DRIVERS, status = null,
                    message = UiText.StringResource(R.string.drv_added, arrayOf(driver.name)))
            }
        }
    }

    fun addVehicle(draft: VehicleDraft) {
        val errors = FleetResourceRules.validateVehicle(draft, uiState.value.vehicles)
        if (errors.isNotEmpty()) {
            local.update { it.copy(vehicleErrors = errors) }
            return
        }
        viewModelScope.launch {
            val vehicle = repository.addVehicle(draft)
            local.update {
                it.copy(addingVehicle = false, vehicleErrors = emptySet(), tab = ResourcesTab.VEHICLES,
                    message = UiText.StringResource(R.string.veh_added, arrayOf("${vehicle.unitCode} (${vehicle.plate})")))
            }
        }
    }

    fun markDriverAvailable(driver: Driver) = viewModelScope.launch {
        repository.markDriverAvailable(driver.id)
        local.update { it.copy(message = UiText.StringResource(R.string.drv_now_available, arrayOf(driver.name))) }
    }

    fun markVehicleAvailable(vehicle: Vehicle) = viewModelScope.launch {
        repository.markVehicleAvailable(vehicle.id)
        local.update { it.copy(message = UiText.StringResource(R.string.drv_now_available, arrayOf(vehicle.unitCode))) }
    }

    /** @param routeKmText distance typed by the person ("380" or "380,5"). */
    fun assign(orderId: String?, driverId: String?, vehicleId: String?, routeKmText: String) {
        val state = uiState.value
        val order = state.pending.firstOrNull { it.orderId == orderId }
        val driver = state.drivers.firstOrNull { it.id == driverId }
        val vehicle = state.vehicles.firstOrNull { it.id == vehicleId }
        val kgFormat = NumberFormat.getNumberInstance().apply { maximumFractionDigits = 1 }
        val routeKm = routeKmText.trim().replace(',', '.').toDoubleOrNull()
        when (AssignmentRules.validate(order, driver, vehicle)) {
            AssignmentRules.Error.ORDER_REQUIRED -> error(R.string.asg_error_order)
            AssignmentRules.Error.DRIVER_REQUIRED -> error(R.string.asg_error_driver)
            AssignmentRules.Error.VEHICLE_REQUIRED -> error(R.string.asg_error_vehicle)
            AssignmentRules.Error.DRIVER_NOT_AVAILABLE -> error(R.string.asg_error_driver_busy)
            AssignmentRules.Error.LICENSE_EXPIRED -> error(R.string.asg_error_license)
            AssignmentRules.Error.VEHICLE_NOT_AVAILABLE -> error(R.string.asg_error_vehicle_busy)
            AssignmentRules.Error.OVER_CAPACITY ->
                error(R.string.asg_error_capacity, listOf(kgFormat.format(order!!.quantityKg), kgFormat.format(vehicle!!.capacityKg)))
            AssignmentRules.Error.ROUTE_KM -> error(R.string.asg_error_route_km)
            null -> if (!AssignmentRules.isValidRouteKm(routeKm)) error(R.string.asg_error_route_km) else viewModelScope.launch {
                repository.assign(order!!.orderId, driver!!.id, vehicle!!.id, routeKm!!)
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
