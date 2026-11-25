package com.rebootech.fruitlogix.distributor.fleetManagement.data

import android.util.Log
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Assignment
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Driver
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.DriverDraft
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.DriverStatus
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.PendingDispatch
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Vehicle
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleStatus
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleType
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleDraft
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.rules.FleetResourceRules
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.repository.DriverRepository
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.rules.AssignmentRules
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.repository.FleetRepository
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.distributor.orderManagement.domain.repository.OrderRepository
import com.rebootech.fruitlogix.shared.core.network.CreateDeliveryRequestDto
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Drivers and vehicles of the Lima Central Hub.
 * The server has no drivers or vehicles yet (a delivery only stores the driver's name and the plate as
 * text), so they are kept on this phone ([PrefsFleetStore]); without a store they live in memory.
 * Assigning a driver and a truck to a server order creates the delivery there (POST /api/v1/deliveries).
 */
class FakeDriverRepository(
    private val orderRepository: OrderRepository,
    /** Refreshed after a delivery is created on the server, so the new trip shows up in Fleet. */
    private val fleetRepository: FleetRepository? = null,
    private val store: PrefsFleetStore? = null
) : DriverRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val assignedOrders = MutableStateFlow(store?.loadAssignedOrders().orEmpty())

    private val _drivers = MutableStateFlow(store?.loadDrivers().orEmpty().filterNot { (it.id to it.name) in OLD_SAMPLE_DRIVERS })
    override val drivers: StateFlow<List<Driver>> = _drivers.asStateFlow()

    private val _vehicles = MutableStateFlow(store?.loadVehicles().orEmpty().filterNot { (it.id to it.plate) in OLD_SAMPLE_VEHICLES })
    override val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    override val pendingDispatches: StateFlow<List<PendingDispatch>> =
        combine(orderRepository.orders, assignedOrders) { orders, assigned ->
            orders.filter { it.status == OrderStatus.IN_PREPARATION && it.id !in assigned }
                .map { PendingDispatch(it.id, it.clientName.ifBlank { "#${it.clientId ?: "?"}" }, it.productName, it.quantityKg) }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    override suspend fun assign(orderId: String, driverId: String, vehicleId: String, routeKm: Double) {
        val vehicle = _vehicles.value.firstOrNull { it.id == vehicleId } ?: return
        val driver = _drivers.value.firstOrNull { it.id == driverId } ?: return
        val order = pendingDispatches.value.firstOrNull { it.orderId == orderId } ?: return

        _drivers.update { list ->
            list.map {
                if (it.id == driverId) it.copy(
                    status = DriverStatus.ON_ROUTE,
                    assignment = Assignment(vehicle.unitCode, vehicle.plate, order.orderId, "Lima Central Hub → ${order.clientName}")
                ) else it
            }
        }
        _vehicles.update { list ->
            list.map {
                if (it.id == vehicleId) it.copy(status = VehicleStatus.ON_ROUTE, driverName = driver.name) else it
            }
        }
        assignedOrders.update { it + orderId }
        persist()

        // Orders that exist on the server get a delivery there (POST /api/v1/deliveries); others stay on this phone
        val realOrderId = orderRepository.backendIdFor(order.orderId)
        if (realOrderId == null) {
            Log.i(TAG, "${order.orderId} is not on the server: assignment kept on this phone")
            return
        }
        val departure = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        val request = CreateDeliveryRequestDto(
            orderId = realOrderId,
            driverName = driver.name,
            driverPhone = driver.phone,
            vehiclePlate = vehicle.plate,
            vehicleType = vehicle.type.name,
            routeOrigin = HUB_NAME,
            routeDestination = order.clientName,
            routeDistanceKm = routeKm,
            estimatedTimeOfArrival = AssignmentRules.estimatedArrival(departure, routeKm).toString()
        )
        scope.launch {
            runCatching { RetrofitClient.api.createDelivery(request) }
                .onSuccess { response ->
                    if (response.isSuccessful) fleetRepository?.refresh()
                    else Log.w(TAG, "POST deliveries answered ${response.code()}")
                }
                .onFailure { Log.w(TAG, "POST deliveries failed: ${it.javaClass.simpleName}") }
        }
    }

    override suspend fun addDriver(draft: DriverDraft): Driver {
        val driver = Driver(
            id = "d-${System.currentTimeMillis()}",
            name = draft.name.trim(),
            phone = draft.phone.trim(),
            licenseClass = draft.licenseClass,
            licenseExpiry = requireNotNull(draft.licenseExpiry),
            coldChainCertified = draft.coldChainCertified,
            status = DriverStatus.AVAILABLE
        )
        _drivers.update { it + driver }
        persist()
        return driver
    }

    override suspend fun addVehicle(draft: VehicleDraft): Vehicle {
        val vehicle = Vehicle(
            id = "v-${System.currentTimeMillis()}",
            unitCode = draft.unitCode.trim().uppercase(),
            plate = FleetResourceRules.normalizePlate(draft.plate),
            type = draft.type,
            capacityKg = requireNotNull(draft.capacityKg),
            minCelsius = requireNotNull(draft.minCelsius),
            maxCelsius = requireNotNull(draft.maxCelsius),
            status = VehicleStatus.AVAILABLE
        )
        _vehicles.update { it + vehicle }
        persist()
        return vehicle
    }

    override suspend fun markDriverAvailable(driverId: String) {
        val driver = _drivers.value.firstOrNull { it.id == driverId } ?: return
        // The truck they were driving comes back with them
        val plate = driver.assignment?.plate
        _drivers.update { list ->
            list.map { if (it.id == driverId) it.copy(status = DriverStatus.AVAILABLE, assignment = null, restUntil = null) else it }
        }
        if (plate != null) {
            _vehicles.update { list ->
                list.map { if (it.plate == plate && it.status == VehicleStatus.ON_ROUTE) it.copy(status = VehicleStatus.AVAILABLE, driverName = null) else it }
            }
        }
        persist()
    }

    override suspend fun markVehicleAvailable(vehicleId: String) {
        _vehicles.update { list ->
            list.map { if (it.id == vehicleId) it.copy(status = VehicleStatus.AVAILABLE, driverName = null) else it }
        }
        persist()
    }

    private fun persist() {
        store?.save(_drivers.value, _vehicles.value, assignedOrders.value)
    }

    private companion object {
        const val TAG = "FruitLogix-Drivers"
        const val HUB_NAME = "Lima Central Hub"

        /** The sample fleet of older versions, removed from phones that saved it (no sample data now). */
        val OLD_SAMPLE_DRIVERS = setOf(
            "d1" to "Jorge Huamán", "d2" to "Mateo Silva", "d3" to "Luis Ramos",
            "d4" to "Pedro Salazar", "d5" to "Ana Torres", "d6" to "Ricardo Paredes"
        )
        val OLD_SAMPLE_VEHICLES = setOf(
            "v1" to "C7X-915", "v2" to "BQK-482", "v3" to "AFR-307",
            "v4" to "HJK-264", "v5" to "CXP-112", "v6" to "FGH-887"
        )
    }
}