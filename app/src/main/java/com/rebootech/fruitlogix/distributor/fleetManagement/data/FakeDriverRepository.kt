package com.rebootech.fruitlogix.distributor.fleetManagement.data

import android.util.Log
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Assignment
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Driver
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.DriverDraft
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.DriverStatus
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.PendingDispatch
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Vehicle
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleDraft
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleStatus
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleType
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.repository.DriverRepository
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.repository.FleetSaveResult
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.rules.AssignmentRules
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.rules.FleetResourceRules
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.repository.FleetRepository
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.distributor.orderManagement.domain.repository.OrderRepository
import com.rebootech.fruitlogix.shared.core.network.ApiResult
import com.rebootech.fruitlogix.shared.core.network.CreateDeliveryRequestDto
import com.rebootech.fruitlogix.shared.core.network.DriverDto
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import com.rebootech.fruitlogix.shared.core.network.SaveDriverRequestDto
import com.rebootech.fruitlogix.shared.core.network.SaveVehicleRequestDto
import com.rebootech.fruitlogix.shared.core.network.VehicleDto
import com.rebootech.fruitlogix.shared.core.network.apiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Drivers and vehicles of the distributor, from the server (/drivers, /vehicles).
 * - Assigning them to an order creates the delivery with driverId and vehicleId (POST /deliveries),
 *   and marks both "on route" with PUT: the server does not change their status by itself.
 * - What each driver is doing (unit, order and route) is not on the server, so this phone keeps it
 *   in [PrefsFleetStore] next to the orders already assigned.
 */
class FakeDriverRepository(
    private val orderRepository: OrderRepository,
    /** Refreshed after a delivery is created on the server, so the new trip shows up in Fleet. */
    private val fleetRepository: FleetRepository? = null,
    private val store: PrefsFleetStore? = null
) : DriverRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val assignedOrders = MutableStateFlow(store?.loadAssignedOrders().orEmpty())

    /** Assignments of this phone, by driver id (the server does not keep them). */
    private val assignments = MutableStateFlow(
        store?.loadDrivers().orEmpty().mapNotNull { d -> d.assignment?.let { d.id to it } }.toMap()
    )

    private val _drivers = MutableStateFlow<List<Driver>>(emptyList())
    override val drivers: StateFlow<List<Driver>> = _drivers.asStateFlow()

    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    override val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    /** Last server copies, so a PUT (which replaces every field) keeps what the app does not change. */
    private val driverCopies = mutableMapOf<Int, DriverDto>()
    private val vehicleCopies = mutableMapOf<Int, VehicleDto>()

    override val pendingDispatches: StateFlow<List<PendingDispatch>> =
        combine(orderRepository.orders, assignedOrders, fleetRepository?.dispatches ?: flowOf(emptyList())) { orders, assigned, trips ->
            // Orders that already have a delivery on the server are not pending either
            val withTrip = trips.map { it.orderId }.toSet()
            orders.filter { it.status == OrderStatus.IN_PREPARATION && it.id !in assigned && it.id !in withTrip }
                .map { PendingDispatch(it.id, it.clientName.ifBlank { "#${it.clientId ?: "?"}" }, it.productName, it.quantityKg) }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    init {
        scope.launch { refresh() }
    }

    override suspend fun refresh() {
        withContext(Dispatchers.IO) {
            (apiCall { RetrofitClient.api.getDrivers() } as? ApiResult.Ok)?.value?.let { list ->
                driverCopies.clear()
                list.forEach { driverCopies[it.id] = it }
                _drivers.value = list.mapNotNull { toDriver(it) }
            }
            (apiCall { RetrofitClient.api.getVehicles() } as? ApiResult.Ok)?.value?.let { list ->
                vehicleCopies.clear()
                list.forEach { vehicleCopies[it.id] = it }
                val onRoute = _drivers.value.mapNotNull { d -> d.assignment?.let { it.plate to d.name } }.toMap()
                _vehicles.value = list.mapNotNull { toVehicle(it, onRoute) }
            }
        }
    }

    override suspend fun assign(orderId: String, driverId: String, vehicleId: String, routeKm: Double): FleetSaveResult {
        val vehicle = _vehicles.value.firstOrNull { it.id == vehicleId } ?: return FleetSaveResult(false)
        val driver = _drivers.value.firstOrNull { it.id == driverId } ?: return FleetSaveResult(false)
        val order = pendingDispatches.value.firstOrNull { it.orderId == orderId } ?: return FleetSaveResult(false)
        val serverOrderId = orderRepository.backendIdFor(order.orderId) ?: return FleetSaveResult(false)

        val departure = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        val request = CreateDeliveryRequestDto(
            orderId = serverOrderId,
            // With the ids the server takes name, phone, plate and type from /drivers and /vehicles
            driverName = driver.name,
            driverPhone = driver.phone,
            vehiclePlate = vehicle.plate,
            vehicleType = typeForServer(vehicle.type),
            routeOrigin = HUB_NAME,
            routeDestination = order.clientName,
            routeDistanceKm = routeKm,
            estimatedTimeOfArrival = AssignmentRules.estimatedArrival(departure, routeKm).toString(),
            driverId = driverId.toIntOrNull(),
            vehicleId = vehicleId.toIntOrNull()
        )
        val created = withContext(Dispatchers.IO) { apiCall { RetrofitClient.api.createDelivery(request) } }
        if (created is ApiResult.Failed) return failure(created)

        // The server does not move them to "on route" by itself
        saveDriver(driver.copy(status = DriverStatus.ON_ROUTE))
        saveVehicle(vehicle.copy(status = VehicleStatus.ON_ROUTE))
        assignments.update { it + (driver.id to Assignment(vehicle.unitCode, vehicle.plate, order.orderId, "$HUB_NAME → ${order.clientName}")) }
        assignedOrders.update { it + orderId }
        _drivers.update { list -> list.map { if (it.id == driver.id) it.copy(status = DriverStatus.ON_ROUTE, assignment = assignments.value[it.id]) else it } }
        _vehicles.update { list -> list.map { if (it.id == vehicle.id) it.copy(status = VehicleStatus.ON_ROUTE, driverName = driver.name) else it } }
        persist()
        fleetRepository?.refresh()
        return FleetSaveResult(true)
    }

    override suspend fun addDriver(draft: DriverDraft): FleetSaveResult {
        val body = SaveDriverRequestDto(
            name = draft.name.trim(),
            phone = draft.phone.trim().ifBlank { null },
            licenseClass = draft.licenseClass,
            licenseExpiry = requireNotNull(draft.licenseExpiry).toString(),
            coldChainCertified = draft.coldChainCertified,
            status = STATUS_AVAILABLE,
            restUntil = null
        )
        return when (val result = withContext(Dispatchers.IO) { apiCall { RetrofitClient.api.createDriver(body) } }) {
            is ApiResult.Ok -> {
                driverCopies[result.value.id] = result.value
                toDriver(result.value)?.let { added -> _drivers.update { (it + added).sortedBy { d -> d.name.lowercase() } } }
                FleetSaveResult(true)
            }
            is ApiResult.Failed -> failure(result)
        }
    }

    override suspend fun addVehicle(draft: VehicleDraft): FleetSaveResult {
        val body = SaveVehicleRequestDto(
            unitCode = draft.unitCode.trim().uppercase(),
            plate = FleetResourceRules.normalizePlate(draft.plate),
            type = typeForServer(draft.type),
            capacityKg = requireNotNull(draft.capacityKg),
            minCelsius = requireNotNull(draft.minCelsius),
            maxCelsius = requireNotNull(draft.maxCelsius),
            status = STATUS_AVAILABLE
        )
        return when (val result = withContext(Dispatchers.IO) { apiCall { RetrofitClient.api.createVehicle(body) } }) {
            is ApiResult.Ok -> {
                vehicleCopies[result.value.id] = result.value
                toVehicle(result.value, emptyMap())?.let { added -> _vehicles.update { (it + added).sortedBy { v -> v.unitCode } } }
                FleetSaveResult(true)
            }
            // 409: another unit already uses that plate
            is ApiResult.Failed -> failure(result)
        }
    }

    override suspend fun markDriverAvailable(driverId: String): FleetSaveResult {
        val driver = _drivers.value.firstOrNull { it.id == driverId } ?: return FleetSaveResult(false)
        val saved = saveDriver(driver.copy(status = DriverStatus.AVAILABLE, restUntil = null))
        if (!saved.ok) return saved
        // The truck they were driving comes back with them
        val plate = driver.assignment?.plate
        assignments.update { it - driverId }
        _drivers.update { list ->
            list.map { if (it.id == driverId) it.copy(status = DriverStatus.AVAILABLE, assignment = null, restUntil = null) else it }
        }
        _vehicles.value.firstOrNull { it.plate == plate && it.status == VehicleStatus.ON_ROUTE }?.let { truck ->
            if (saveVehicle(truck.copy(status = VehicleStatus.AVAILABLE)).ok) {
                _vehicles.update { list -> list.map { if (it.id == truck.id) it.copy(status = VehicleStatus.AVAILABLE, driverName = null) else it } }
            }
        }
        persist()
        return saved
    }

    override suspend fun markVehicleAvailable(vehicleId: String): FleetSaveResult {
        val vehicle = _vehicles.value.firstOrNull { it.id == vehicleId } ?: return FleetSaveResult(false)
        val saved = saveVehicle(vehicle.copy(status = VehicleStatus.AVAILABLE))
        if (saved.ok) {
            _vehicles.update { list -> list.map { if (it.id == vehicleId) it.copy(status = VehicleStatus.AVAILABLE, driverName = null) else it } }
        }
        return saved
    }

    /** PUT replaces every field: the whole driver is sent. */
    private suspend fun saveDriver(driver: Driver): FleetSaveResult {
        val id = driver.id.toIntOrNull() ?: return FleetSaveResult(false)
        val body = SaveDriverRequestDto(
            name = driver.name,
            phone = driver.phone.ifBlank { null },
            licenseClass = driver.licenseClass,
            licenseExpiry = driver.licenseExpiry.toString(),
            coldChainCertified = driver.coldChainCertified,
            status = statusForServer(driver.status),
            restUntil = driver.restUntil?.atZone(ZoneId.systemDefault())?.toOffsetDateTime()?.toString()
        )
        return when (val result = withContext(Dispatchers.IO) { apiCall { RetrofitClient.api.updateDriver(id, body) } }) {
            is ApiResult.Ok -> FleetSaveResult(true).also { driverCopies[id] = result.value }
            is ApiResult.Failed -> failure(result)
        }
    }

    private suspend fun saveVehicle(vehicle: Vehicle): FleetSaveResult {
        val id = vehicle.id.toIntOrNull() ?: return FleetSaveResult(false)
        val body = SaveVehicleRequestDto(
            unitCode = vehicle.unitCode,
            plate = vehicle.plate,
            type = typeForServer(vehicle.type),
            capacityKg = vehicle.capacityKg,
            minCelsius = vehicle.minCelsius,
            maxCelsius = vehicle.maxCelsius,
            status = when (vehicle.status) {
                VehicleStatus.AVAILABLE -> "Available"
                VehicleStatus.ON_ROUTE -> "OnRoute"
                VehicleStatus.MAINTENANCE -> "Maintenance"
            }
        )
        return when (val result = withContext(Dispatchers.IO) { apiCall { RetrofitClient.api.updateVehicle(id, body) } }) {
            is ApiResult.Ok -> FleetSaveResult(true).also { vehicleCopies[id] = result.value }
            is ApiResult.Failed -> failure(result)
        }
    }

    private fun failure(result: ApiResult.Failed): FleetSaveResult {
        Log.w(TAG, "Fleet change answered ${result.failure.code}")
        return FleetSaveResult(false, result.failure.serverMessage, offline = result.failure.isNetwork)
    }

    private fun toDriver(dto: DriverDto): Driver? {
        val expiry = dto.licenseExpiry?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return null
        val id = dto.id.toString()
        val status = when (dto.status?.lowercase(Locale.ROOT)) {
            "onroute" -> DriverStatus.ON_ROUTE
            "offduty" -> DriverStatus.OFF_DUTY
            else -> DriverStatus.AVAILABLE
        }
        return Driver(
            id = id,
            name = dto.name.orEmpty(),
            phone = dto.phone.orEmpty(),
            licenseClass = dto.licenseClass.orEmpty(),
            licenseExpiry = expiry,
            coldChainCertified = dto.coldChainCertified ?: false,
            status = status,
            assignment = assignments.value[id].takeIf { status == DriverStatus.ON_ROUTE },
            restUntil = dto.restUntil?.let { parseDateTime(it) }
        )
    }

    private fun toVehicle(dto: VehicleDto, driverByPlate: Map<String, String>): Vehicle? {
        val plate = dto.plate ?: return null
        val status = when (dto.status?.lowercase(Locale.ROOT)) {
            "onroute" -> VehicleStatus.ON_ROUTE
            "maintenance" -> VehicleStatus.MAINTENANCE
            else -> VehicleStatus.AVAILABLE
        }
        return Vehicle(
            id = dto.id.toString(),
            unitCode = dto.unitCode.orEmpty(),
            plate = plate,
            type = when (dto.type?.lowercase(Locale.ROOT)) {
                "heavyreefer" -> VehicleType.HEAVY_REEFER
                "mediumreefervan" -> VehicleType.MEDIUM_REEFER_VAN
                "lightreefer" -> VehicleType.LIGHT_REEFER
                else -> VehicleType.REEFER_TRUCK
            },
            capacityKg = dto.capacityKg ?: 0.0,
            minCelsius = dto.minCelsius ?: 0.0,
            maxCelsius = dto.maxCelsius ?: 0.0,
            status = status,
            driverName = driverByPlate[plate].takeIf { status == VehicleStatus.ON_ROUTE }
        )
    }

    private fun parseDateTime(value: String): LocalDateTime? =
        runCatching { OffsetDateTime.parse(value).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime() }.getOrNull()
            ?: runCatching { LocalDateTime.parse(value) }.getOrNull()

    private fun persist() {
        store?.save(_drivers.value, _vehicles.value, assignedOrders.value)
    }

    private companion object {
        const val TAG = "FruitLogix-Drivers"
        const val HUB_NAME = "Lima Central Hub"
        const val STATUS_AVAILABLE = "Available"

        fun statusForServer(status: DriverStatus) = when (status) {
            DriverStatus.AVAILABLE -> "Available"
            DriverStatus.ON_ROUTE -> "OnRoute"
            DriverStatus.OFF_DUTY -> "OffDuty"
        }

        fun typeForServer(type: VehicleType) = when (type) {
            VehicleType.HEAVY_REEFER -> "HeavyReefer"
            VehicleType.REEFER_TRUCK -> "ReeferTruck"
            VehicleType.MEDIUM_REEFER_VAN -> "MediumReeferVan"
            VehicleType.LIGHT_REEFER -> "LightReefer"
        }
    }
}
