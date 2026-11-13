package com.rebootech.fruitlogix.distributor.fleetManagement.data

import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Assignment
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Driver
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.DriverStatus
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.PendingDispatch
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Vehicle
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleStatus
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleType
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.repository.DriverRepository
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.distributor.orderManagement.domain.repository.OrderRepository
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
import java.time.LocalDate
import kotlinx.coroutines.launch
/**
 * Drivers and vehicles of the Lima Central Hub: the same units, plates and people as the Fleet
 * trips. Orders "in preparation" (read through the Orders domain interface) are the dispatches
 * that still need a driver and a truck.
 */
class FakeDriverRepository(orderRepository: OrderRepository) : DriverRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val assignedOrders = MutableStateFlow(emptySet<String>())

    private val _drivers = MutableStateFlow(initialDrivers())
    override val drivers: StateFlow<List<Driver>> = _drivers.asStateFlow()

    private val _vehicles = MutableStateFlow(initialVehicles())
    override val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    override val pendingDispatches: StateFlow<List<PendingDispatch>> =
        combine(orderRepository.orders, assignedOrders) { orders, assigned ->
            orders.filter { it.status == OrderStatus.IN_PREPARATION && it.id !in assigned }
                .map { PendingDispatch(it.id, it.clientName, it.productName, it.quantityTons) }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    override suspend fun assign(orderId: String, driverId: String, vehicleId: String) {
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
        _vehicles.update { list -> list.map { if (it.id == vehicleId) it.copy(status = VehicleStatus.ON_ROUTE, driverName = driver.name) else it } }
        assignedOrders.update { it + orderId }
        // Sincronizar asignación con el backend en Render
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            runCatching {
                com.rebootech.fruitlogix.shared.core.network.RetrofitClient.api.assignProducer(
                    orderId.removePrefix("FX-"),
                    com.rebootech.fruitlogix.shared.core.network.AssignProducerRequestDto(
                        producerId = "${driver.name} - ${vehicle.plate}"
                    )
                )
            }
        }
    }

    private companion object {
        fun initialDrivers(): List<Driver> {
            val today = LocalDate.now()
            return listOf(
                Driver("d1", "Jorge Huamán", "+51 987 112 340", "A-IIIc", today.plusYears(1), true, DriverStatus.ON_ROUTE,
                    Assignment("FL-102", "BQK-482", "FX-1041", "Ica → Lima Central Hub")),
                Driver("d2", "Mateo Silva", "+51 956 410 228", "A-IIIc", today.plusYears(2), true, DriverStatus.ON_ROUTE,
                    Assignment("FL-408", "C7X-915", "FX-1042", "Chavín de Huántar → Lima Central Hub")),
                Driver("d3", "Luis Ramos", "+51 944 870 615", "A-IIIb", today.plusMonths(8), true, DriverStatus.ON_ROUTE,
                    Assignment("FL-219", "AFR-307", "FX-1038", "Piura → Lima Central Hub")),
                // License expiring soon: can drive, with a warning
                Driver("d4", "Pedro Salazar", "+51 933 210 456", "A-IIIc", today.plusDays(20), true, DriverStatus.AVAILABLE),
                Driver("d5", "Ana Torres", "+51 922 678 901", "A-IIb", today.plusYears(1), false, DriverStatus.OFF_DUTY,
                    restUntil = LocalDate.now().plusDays(1).atTime(6, 0)),
                // Expired license: listed but cannot be assigned
                Driver("d6", "Ricardo Paredes", "+51 911 456 789", "A-IIIc", today.minusMonths(13), true, DriverStatus.AVAILABLE)
            )
        }

        fun initialVehicles() = listOf(
            Vehicle("v1", "FL-408", "C7X-915", VehicleType.HEAVY_REEFER, 12.0, 0.0, 8.0, VehicleStatus.ON_ROUTE, "Mateo Silva"),
            Vehicle("v2", "FL-102", "BQK-482", VehicleType.REEFER_TRUCK, 12.0, -1.0, 8.0, VehicleStatus.ON_ROUTE, "Jorge Huamán"),
            Vehicle("v3", "FL-219", "AFR-307", VehicleType.REEFER_TRUCK, 15.0, 2.0, 14.0, VehicleStatus.ON_ROUTE, "Luis Ramos"),
            Vehicle("v4", "FL-510", "HJK-264", VehicleType.HEAVY_REEFER, 14.0, -1.0, 10.0, VehicleStatus.AVAILABLE),
            Vehicle("v5", "FL-305", "CXP-112", VehicleType.MEDIUM_REEFER_VAN, 8.0, 2.0, 8.0, VehicleStatus.AVAILABLE),
            Vehicle("v6", "FL-607", "FGH-887", VehicleType.LIGHT_REEFER, 3.5, 4.0, 10.0, VehicleStatus.MAINTENANCE)
        )
    }
}
