package com.rebootech.fruitlogix.distributor.logisticsMonitoring.data

import android.util.Log
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.ActiveDispatch
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.Checkpoint
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.CheckpointKind
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DelayReason
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DeliveryState
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DriverInfo
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.FleetActionResult
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.repository.FleetRepository
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules.EtaRules
import com.rebootech.fruitlogix.distributor.orderManagement.domain.repository.OrderRepository
import com.rebootech.fruitlogix.shared.core.network.AddCheckpointRequestDto
import com.rebootech.fruitlogix.shared.core.network.ApiResult
import com.rebootech.fruitlogix.shared.core.network.ArriveDeliveryRequestDto
import com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus
import com.rebootech.fruitlogix.shared.core.network.CreateTrackingLogRequestDto
import com.rebootech.fruitlogix.shared.core.network.DeliveryEventDto
import com.rebootech.fruitlogix.shared.core.network.DeliveryResponseDto
import com.rebootech.fruitlogix.shared.core.network.ReportDelayRequestDto
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import com.rebootech.fruitlogix.shared.core.network.TrackingLogResponseDto
import com.rebootech.fruitlogix.shared.core.network.UpdateDeliveryAssignmentRequestDto
import com.rebootech.fruitlogix.shared.core.network.apiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * Trips in Fleet.
 * - Server deliveries (/api/v1/deliveries) are shown as "D-<id>" trips. Starting a trip
 *   (start-dispatch, which also moves the order to InTransit), reporting a delay, checkpoints,
 *   GPS positions (tracking-logs), driver/truck changes and the arrival are sent to the server.
 * - The trip is closed as delivered when the client confirms the reception from their app.
 */
class FakeFleetRepository(
    private val orderRepository: OrderRepository
) : FleetRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val trips = MutableStateFlow<List<ActiveDispatch>>(emptyList())

    init {
        scope.launch { refresh() }
    }

    override val dispatches: StateFlow<List<ActiveDispatch>> =
        combine(trips, orderRepository.orders) { list, orders ->
            val activeOrderIds = orders.map { it.id }.toSet()
            list.map { trip ->
                // Trips not yet on the server follow their order; server trips follow the server status
                if (trip.remoteId == null && trip.isActive && trip.orderId !in activeOrderIds) {
                    trip.copy(deliveryState = DeliveryState.DELIVERED, deliveredAt = trip.deliveredAt ?: LocalDateTime.now())
                } else trip
            }
        }.stateIn(scope, SharingStarted.Eagerly, trips.value)

    override fun dispatch(unitId: String): ActiveDispatch? =
        dispatches.value.firstOrNull { it.unitId.equals(unitId, ignoreCase = true) }

    private fun change(unitId: String, block: (ActiveDispatch) -> ActiveDispatch) =
        trips.update { list -> list.map { if (it.unitId.equals(unitId, ignoreCase = true)) block(it) else it } }

    override suspend fun updateDeliveryState(unitId: String, state: DeliveryState): FleetActionResult {
        val before = dispatch(unitId) ?: return FleetActionResult(false)
        val remoteId = before.remoteId
        // The server only allows starting a delivery that is still pending dispatch
        if (remoteId != null && state == DeliveryState.ON_ROUTE && before.deliveryState == DeliveryState.PENDING) {
            val result = callServer { RetrofitClient.api.startDelivery(remoteId) }
            // The order moved to "in transit" too: refresh it so Orders and the client see it
            if (result.ok) orderRepository.refresh()
            return result
        }
        change(unitId) { it.copy(deliveryState = state) }
        return FleetActionResult(ok = true, onServer = false)
    }

    override suspend fun updateEta(unitId: String, newEta: LocalTime, reason: DelayReason): FleetActionResult {
        val current = dispatch(unitId) ?: return FleetActionResult(false)
        val remoteId = current.remoteId
        val updated = EtaRules.apply(current, newEta, reason)
        if (remoteId == null) {
            change(unitId) { updated }
            return FleetActionResult(ok = true, onServer = false)
        }
        // After the arrival the server refuses it (409)
        val result = callServer { RetrofitClient.api.reportDeliveryDelay(remoteId, ReportDelayRequestDto(reason = reason.name)) }
        if (result.ok) change(unitId) { it.copy(eta = updated.eta, delayMinutes = updated.delayMinutes, delayReason = updated.delayReason) }
        return result
    }

    override suspend fun recordReeferCheck(unitId: String) =
        change(unitId) { it.copy(reeferCheckedAt = LocalDateTime.now()) }

    override suspend fun completeDelivery(unitId: String, latitude: Double?, longitude: Double?): FleetActionResult {
        val trip = dispatch(unitId) ?: return FleetActionResult(false)
        val remoteId = trip.remoteId
        if (remoteId == null) {
            change(unitId) {
                it.copy(deliveryState = DeliveryState.DELIVERED, deliveredAt = LocalDateTime.now(), travelledKm = it.totalKm)
            }
            orderRepository.confirmReception(trip.orderId)
            return FleetActionResult(ok = true, onServer = false)
        }
        // Already arrived (geofence or a previous tap): nothing to send, the client closes it
        if (trip.arrivedAt != null) return FleetActionResult(ok = true)
        return callServer {
            RetrofitClient.api.arriveDelivery(remoteId, ArriveDeliveryRequestDto(latitude, longitude, ARRIVAL_NOTE))
        }
    }

    override suspend fun rejectDelivery(unitId: String, reason: String) =
        change(unitId) { it.copy(deliveryState = DeliveryState.REJECTED, rejectionReason = reason.trim()) }

    override suspend fun addCheckpoint(unitId: String, name: String, note: String?, latitude: Double?, longitude: Double?): FleetActionResult {
        val trip = dispatch(unitId) ?: return FleetActionResult(false)
        val remoteId = trip.remoteId
        val now = LocalDateTime.now()
        if (remoteId == null) {
            change(unitId) { it.copy(checkpoints = withCheckpoint(it.checkpoints, name.trim(), now.toLocalTime())) }
            return FleetActionResult(ok = true, onServer = false)
        }
        val body = AddCheckpointRequestDto(
            name = name.trim(),
            latitude = latitude.takeIf { longitude != null },
            longitude = longitude.takeIf { latitude != null },
            note = note?.trim()?.takeIf { it.isNotBlank() }
        )
        return when (val result = withContext(Dispatchers.IO) { apiCall { RetrofitClient.api.addDeliveryCheckpoint(remoteId, body) } }) {
            is ApiResult.Ok -> {
                change(unitId) { it.copy(checkpoints = withCheckpoint(it.checkpoints, name.trim(), now.toLocalTime())) }
                FleetActionResult(ok = true)
            }
            is ApiResult.Failed -> FleetActionResult(false, result.failure.serverMessage)
        }
    }

    override suspend fun changeAssignment(unitId: String, driverName: String, driverPhone: String, plate: String, vehicleType: String, driverId: Int?, vehicleId: Int?): FleetActionResult {
        val trip = dispatch(unitId) ?: return FleetActionResult(false)
        val remoteId = trip.remoteId
        if (remoteId == null) {
            change(unitId) {
                it.copy(
                    driver = it.driver.copy(name = driverName.ifBlank { it.driver.name }, phone = driverPhone.ifBlank { it.driver.phone }),
                    plate = plate.ifBlank { it.plate }.uppercase(),
                    truckModel = vehicleType.ifBlank { it.truckModel }
                )
            }
            return FleetActionResult(ok = true, onServer = false)
        }
        // Only the fields that change are sent
        val body = UpdateDeliveryAssignmentRequestDto(
            driverName = driverName.trim().takeIf { it.isNotBlank() && it != trip.driver.name },
            driverPhone = driverPhone.trim().takeIf { it.isNotBlank() && it != trip.driver.phone },
            vehiclePlate = plate.trim().uppercase().takeIf { it.isNotBlank() && it != trip.plate },
            vehicleType = vehicleType.trim().takeIf { it.isNotBlank() && it != trip.truckModel },
            driverId = driverId,
            vehicleId = vehicleId
        )
        return callServer { RetrofitClient.api.updateDeliveryAssignment(remoteId, body) }
    }

    override suspend fun sendPosition(unitId: String, latitude: Double, longitude: Double, celsius: Double, humidity: Double): FleetActionResult {
        val trip = dispatch(unitId) ?: return FleetActionResult(false)
        val remoteId = trip.remoteId ?: return FleetActionResult(ok = false, onServer = false)
        val body = CreateTrackingLogRequestDto(
            deliveryId = remoteId,
            timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString(),
            latitude = latitude,
            longitude = longitude,
            temperature = celsius,
            humidity = humidity
        )
        return when (val result = withContext(Dispatchers.IO) { apiCall { RetrofitClient.api.createTrackingLog(body) } }) {
            is ApiResult.Ok -> {
                // The geofence may have marked it as arrived: read the trip again
                refreshOne(remoteId)
                FleetActionResult(ok = true)
            }
            is ApiResult.Failed -> FleetActionResult(false, result.failure.serverMessage)
        }
    }

    override suspend fun refresh() {
        val result = withContext(Dispatchers.IO) { apiCall { RetrofitClient.api.getDeliveries() } }
        if (result !is ApiResult.Ok) {
            RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK, "GET /deliveries")
            return
        }
        RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
        val previous = trips.value.filter { it.remoteId != null }.associateBy { it.remoteId }
        val fromServer = result.value.map { dto -> build(dto, previous[dto.id]) }
        // The server list is the whole list (no sample trips)
        trips.value = fromServer
    }

    /** Reads one delivery again (after a GPS position, the geofence may have changed it). */
    private suspend fun refreshOne(remoteId: Int) {
        val result = withContext(Dispatchers.IO) { apiCall { RetrofitClient.api.getDelivery(remoteId) } }
        if (result is ApiResult.Ok) replaceServerTrip(result.value)
    }

    /** Active trips also bring their events (checkpoints) and last GPS position. */
    private suspend fun build(dto: DeliveryResponseDto, previous: ActiveDispatch?): ActiveDispatch {
        val active = DeliveryMapper.state(dto.currentStatus) == DeliveryState.ON_ROUTE
        val events = if (active || DeliveryMapper.isArrived(dto.currentStatus)) events(dto.id) else emptyList()
        val latestLog = if (active) latestTrackingLog(dto.id) else null
        val orderCode = dto.orderId?.let { orderRepository.orderCodeFor(it) ?: "FX-$it" }.orEmpty()
        val order = orderRepository.orderById(orderCode)
        return DeliveryMapper.toDispatch(
            dto = dto,
            orderCode = orderCode,
            productName = order?.productName.orEmpty(),
            pallets = order?.pallets ?: 0,
            latestLog = latestLog,
            previous = previous,
            events = events
        )
    }

    private suspend fun replaceServerTrip(dto: DeliveryResponseDto) {
        val unitId = DeliveryMapper.unitIdFor(dto.id)
        val updated = build(dto, dispatch(unitId))
        trips.update { list -> if (list.any { it.unitId == unitId }) list.map { if (it.unitId == unitId) updated else it } else list + updated }
    }

    private suspend fun events(deliveryId: Int): List<DeliveryEventDto> =
        (withContext(Dispatchers.IO) { apiCall { RetrofitClient.api.getDeliveryEvents(deliveryId) } } as? ApiResult.Ok)?.value.orEmpty()

    private suspend fun latestTrackingLog(deliveryId: Int): TrackingLogResponseDto? {
        val logs = (withContext(Dispatchers.IO) { apiCall { RetrofitClient.api.getTrackingLogs(deliveryId) } } as? ApiResult.Ok)?.value
        // (0,0) means "no GPS signal": skip it
        return logs.orEmpty()
            .filter { DeliveryMapper.position(it.latitude, it.longitude) != null }
            .maxByOrNull { DeliveryMapper.parseDateTime(it.timestamp) ?: LocalDateTime.MIN }
    }

    /** Sends a change of a server delivery; on success the trip takes the server's answer. */
    private suspend fun callServer(call: suspend () -> Response<DeliveryResponseDto>): FleetActionResult =
        when (val result = withContext(Dispatchers.IO) { apiCall { call() } }) {
            is ApiResult.Ok -> {
                replaceServerTrip(result.value)
                FleetActionResult(ok = true)
            }
            is ApiResult.Failed -> {
                Log.w(TAG, "Delivery change answered ${result.failure.code}")
                FleetActionResult(false, result.failure.serverMessage)
            }
        }

    /** Adds a passed checkpoint before the current position / destination. */
    private fun withCheckpoint(list: List<Checkpoint>, name: String, at: LocalTime): List<Checkpoint> {
        val index = list.indexOfFirst { it.kind == CheckpointKind.CURRENT || it.kind == CheckpointKind.DESTINATION }
            .takeIf { it >= 0 } ?: list.size
        return list.toMutableList().apply { add(index, Checkpoint(name, CheckpointKind.CHECKPOINT, passedAt = at)) }
    }

    private companion object {
        const val TAG = "FruitLogix-Fleet"
        const val ARRIVAL_NOTE = "Delivery proof registered in the FruitLogix app"
    }
}
