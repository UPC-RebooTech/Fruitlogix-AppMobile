package com.rebootech.fruitlogix.distributor.fleetManagement.data

import android.util.Log
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

/**
 * Conductores y vehículos del Lima Central Hub.
 *
 * NOTA DE INTEGRACIÓN CON BACKEND:
 * El backend de FruitLogix no expone endpoints para un catálogo de conductores (/drivers) ni vehículos (/vehicles),
 * ni un endpoint específico para asignar conductor/vehículo a una orden (el endpoint PATCH /orders/{id}/assign-producer
 * es exclusivo para productores agrícolas y NO debe usarse aquí).
 * Cuando el distribuidor asigna un conductor y vehículo a una orden en preparación (US79), si la orden cuenta con un ID
 * real en la base de datos de Render, se crea la entrega correspondiente mediante POST /api/v1/deliveries.
 */
class FakeDriverRepository(
    private val orderRepository: OrderRepository
) : DriverRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
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
        _vehicles.update { list ->
            list.map {
                if (it.id == vehicleId) it.copy(status = VehicleStatus.ON_ROUTE, driverName = driver.name) else it
            }
        }
        assignedOrders.update { it + orderId }

        // Si la orden existe en la BD del backend, registramos el despacho en POST /api/v1/deliveries
        val realOrderId = orderRepository.backendIdFor(order.orderId)
        if (realOrderId != null) {
            scope.launch {
                runCatching {
                    RetrofitClient.api.createDelivery(
                        CreateDeliveryRequestDto(
                            orderId = realOrderId,
                            originAddress = "Lima Central Hub, Callao",
                            destinationAddress = order.clientName,
                            estimatedArrival = LocalDateTime.now().plusHours(4).toString(),
                            notes = "Unidad: ${vehicle.unitCode} (${vehicle.plate}) | Conductor: ${driver.name}"
                        )
                    )
                }.onSuccess { response ->
                    if (!response.isSuccessful) {
                        Log.w("FruitLogix-API", "POST /deliveries devolvió HTTP ${response.code()} al asignar recursos")
                    }
                }.onFailure { err ->
                    Log.w("FruitLogix-API", "Asignación guardada localmente; sin conexión a /deliveries: ${err.message}")
                }
            }
        } else {
            Log.i(
                "FruitLogix-API",
                "Asignación de conductor ${driver.name} y unidad ${vehicle.plate} para ${order.orderId} mantenida localmente (falta endpoint /assign-driver-vehicle en backend)."
            )
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
                Driver("d4", "Pedro Salazar", "+51 933 210 456", "A-IIIc", today.plusDays(20), true, DriverStatus.AVAILABLE),
                Driver("d5", "Ana Torres", "+51 922 678 901", "A-IIb", today.plusYears(1), false, DriverStatus.OFF_DUTY,
                    restUntil = LocalDate.now().plusDays(1).atTime(6, 0)),
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