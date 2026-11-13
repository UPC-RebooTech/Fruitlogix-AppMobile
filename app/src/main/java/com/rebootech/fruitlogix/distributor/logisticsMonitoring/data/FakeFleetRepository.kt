package com.rebootech.fruitlogix.distributor.logisticsMonitoring.data

import android.util.Log
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.ActiveDispatch
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.Checkpoint
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.CheckpointKind
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DelayReason
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DeliveryState
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DriverInfo
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.repository.FleetRepository
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules.EtaRules
import com.rebootech.fruitlogix.distributor.orderManagement.domain.repository.OrderRepository
import com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus
import com.rebootech.fruitlogix.shared.core.network.DeliveryResponseDto
import com.rebootech.fruitlogix.shared.core.network.ReportDelayRequestDto
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import com.rebootech.fruitlogix.shared.core.network.TrackingLogResponseDto
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
import kotlin.coroutines.cancellation.CancellationException

/**
 * Trips in Fleet.
 * - Sample trips (FL-408, FL-102, FL-219) only live on this phone, so the app works offline.
 * - Server deliveries (/api/v1/deliveries) are shown as "D-<id>" trips. Starting a trip and
 *   reporting a delay are sent to the server (start-dispatch, report-delay).
 * - The server has no endpoint to mark a delivery as delivered or rejected yet: those changes
 *   stay on this phone, and the reception is confirmed on the order (/orders/{id}/reception).
 */
class FakeFleetRepository(
    private val orderRepository: OrderRepository
) : FleetRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val trips = MutableStateFlow(initialTrips())

    init {
        scope.launch { refresh() }
    }

    override val dispatches: StateFlow<List<ActiveDispatch>> =
        combine(trips, orderRepository.orders) { list, orders ->
            val activeOrderIds = orders.map { it.id }.toSet()
            list.map { trip ->
                // Sample trips follow their sample order; server trips follow the server status
                if (trip.remoteId == null && trip.isActive && trip.orderId !in activeOrderIds) {
                    trip.copy(deliveryState = DeliveryState.DELIVERED, deliveredAt = trip.deliveredAt ?: LocalDateTime.now())
                } else trip
            }
        }.stateIn(scope, SharingStarted.Eagerly, trips.value)

    override fun dispatch(unitId: String): ActiveDispatch? =
        dispatches.value.firstOrNull { it.unitId.equals(unitId, ignoreCase = true) }

    private fun change(unitId: String, block: (ActiveDispatch) -> ActiveDispatch) =
        trips.update { list -> list.map { if (it.unitId.equals(unitId, ignoreCase = true)) block(it) else it } }

    override suspend fun updateDeliveryState(unitId: String, state: DeliveryState) {
        val before = dispatch(unitId) ?: return
        change(unitId) { it.copy(deliveryState = state) }
        val remoteId = before.remoteId ?: return
        // The server only allows starting a delivery that is still pending dispatch
        if (state == DeliveryState.ON_ROUTE && before.deliveryState == DeliveryState.PENDING) {
            callServer("start-dispatch $remoteId") { RetrofitClient.api.startDelivery(remoteId) }
        }
    }

    override suspend fun updateEta(unitId: String, newEta: LocalTime, reason: DelayReason) {
        val current = dispatch(unitId) ?: return
        val updated = EtaRules.apply(current, newEta, reason)
        change(unitId) { updated }
        val remoteId = current.remoteId ?: return
        callServer("report-delay $remoteId") {
            RetrofitClient.api.reportDeliveryDelay(remoteId, ReportDelayRequestDto(reason = reason.name))
        }
    }

    override suspend fun recordReeferCheck(unitId: String) =
        change(unitId) { it.copy(reeferCheckedAt = LocalDateTime.now()) }

    override suspend fun completeDelivery(unitId: String) {
        val trip = dispatch(unitId) ?: return
        change(unitId) {
            it.copy(
                deliveryState = DeliveryState.DELIVERED,
                deliveredAt = LocalDateTime.now(),
                travelledKm = it.totalKm
            )
        }
        // No delivery endpoint for this on the server: the reception is recorded on the order
        orderRepository.confirmReception(trip.orderId)
    }

    override suspend fun rejectDelivery(unitId: String, reason: String) =
        change(unitId) { it.copy(deliveryState = DeliveryState.REJECTED, rejectionReason = reason.trim()) }

    override suspend fun refresh() {
        val response = withContext(Dispatchers.IO) {
            try {
                RetrofitClient.api.getDeliveries()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Log.w(TAG, "GET deliveries failed: ${e.javaClass.simpleName}")
                RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK, "GET /deliveries: ${e.javaClass.simpleName}")
                null
            }
        } ?: return
        if (!response.isSuccessful) {
            Log.w(TAG, "GET deliveries answered ${response.code()}")
            return
        }
        RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
        val deliveries = response.body().orEmpty()
        val previous = trips.value.filter { it.remoteId != null }.associateBy { it.remoteId }
        val fromServer = deliveries.map { dto ->
            val onRoute = DeliveryMapper.state(dto.currentStatus) == DeliveryState.ON_ROUTE
            toDispatch(dto, if (onRoute) latestTrackingLog(dto.id) else null, previous[dto.id])
        }
        // The server list replaces the previous server copy; sample trips stay
        trips.update { list -> list.filter { it.remoteId == null } + fromServer }
    }

    private fun toDispatch(dto: DeliveryResponseDto, latestLog: TrackingLogResponseDto?, previous: ActiveDispatch?): ActiveDispatch {
        val orderCode = dto.orderId?.let { orderRepository.orderCodeFor(it) ?: "FX-$it" }.orEmpty()
        val order = orderRepository.orderById(orderCode)
        return DeliveryMapper.toDispatch(
            dto = dto,
            orderCode = orderCode,
            productName = order?.productName.orEmpty(),
            pallets = order?.pallets ?: 0,
            latestLog = latestLog,
            previous = previous
        )
    }

    private suspend fun latestTrackingLog(deliveryId: Int): TrackingLogResponseDto? = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.api.getTrackingLogs(deliveryId)
            if (!response.isSuccessful) return@withContext null
            response.body().orEmpty().maxByOrNull { DeliveryMapper.parseDateTime(it.timestamp) ?: LocalDateTime.MIN }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            null
        }
    }

    /** Sends a change of a server delivery; on success the trip takes the server's answer. */
    private suspend fun callServer(label: String, call: suspend () -> Response<DeliveryResponseDto>) {
        val response = withContext(Dispatchers.IO) {
            try {
                call()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Log.w(TAG, "$label failed: ${e.javaClass.simpleName}")
                null
            }
        } ?: return
        val dto = response.body()
        if (!response.isSuccessful || dto == null) {
            // e.g. 500 when the server refuses the change (it does not answer 400/404 for these)
            Log.w(TAG, "$label answered ${response.code()}")
            return
        }
        val unitId = DeliveryMapper.unitIdFor(dto.id)
        change(unitId) { current -> toDispatch(dto, null, current).copy(checkpoints = current.checkpoints) }
    }

    private companion object {
        const val TAG = "FruitLogix-Fleet"

        fun initialTrips(): List<ActiveDispatch> {
            val now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
            val today = now.toLocalTime()
            return listOf(
                ActiveDispatch(
                    unitId = "FL-408",
                    plate = "C7X-915",
                    truckModel = "International LT",
                    driver = DriverInfo("Mateo Silva", "+51 956 410 228", "Q-41872093"),
                    orderId = "FX-1042",
                    productName = "Palta Hass",
                    pallets = 18,
                    producerName = "Fundo Santa Rosa",
                    originName = "Chavín de Huántar",
                    destinationName = "Lima Central Hub",
                    dock = "A",
                    sealId = "PE-99410-X",
                    sensorId = "TH-408",
                    totalKm = 223.0,
                    travelledKm = 182.0,
                    eta = today.plusMinutes(50),
                    harvestedAt = now.minusDays(1).withHour(5).withMinute(30),
                    inspectedAt = now.minusDays(1).withHour(7).withMinute(15),
                    departedAt = now.minusMinutes(300),
                    checkpoints = listOf(
                        Checkpoint("Chavín de Huántar", CheckpointKind.ORIGIN, passedAt = today.minusMinutes(300)),
                        Checkpoint("Huaraz", CheckpointKind.CHECKPOINT, passedAt = today.minusMinutes(200)),
                        Checkpoint("Pativilca", CheckpointKind.CHECKPOINT, passedAt = today.minusMinutes(65)),
                        Checkpoint("Panamericana Norte", CheckpointKind.CURRENT, km = 182),
                        Checkpoint("Lima Central Hub", CheckpointKind.DESTINATION)
                    )
                ),
                ActiveDispatch(
                    unitId = "FL-102",
                    plate = "BQK-482",
                    truckModel = "Kenworth T680",
                    driver = DriverInfo("Jorge Huamán", "+51 987 112 340", "Q-30551876"),
                    orderId = "FX-1041",
                    productName = "Uva Red Globe",
                    pallets = 16,
                    producerName = "Viñedos de Ica",
                    originName = "Ica",
                    destinationName = "Lima Central Hub",
                    dock = "B",
                    sealId = "PE-99387-K",
                    sensorId = "TH-102",
                    totalKm = 395.0,
                    travelledKm = 394.6,
                    eta = today.plusMinutes(3),
                    harvestedAt = now.minusDays(1).withHour(4).withMinute(50),
                    inspectedAt = now.minusDays(1).withHour(6).withMinute(40),
                    departedAt = now.minusMinutes(360),
                    reeferCheckedAt = now.minusMinutes(365),
                    checkpoints = listOf(
                        Checkpoint("Ica", CheckpointKind.ORIGIN, passedAt = today.minusMinutes(360)),
                        Checkpoint("Chincha", CheckpointKind.CHECKPOINT, passedAt = today.minusMinutes(290)),
                        Checkpoint("Lurín", CheckpointKind.CHECKPOINT, passedAt = today.minusMinutes(70)),
                        Checkpoint("Av. Néstor Gambetta", CheckpointKind.CURRENT, km = 394),
                        Checkpoint("Lima Central Hub", CheckpointKind.DESTINATION)
                    )
                ),
                ActiveDispatch(
                    unitId = "FL-219",
                    plate = "AFR-307",
                    truckModel = "Freightliner M2",
                    driver = DriverInfo("Luis Ramos", "+51 944 870 615", "Q-27409318"),
                    orderId = "FX-1038",
                    productName = "Mango Kent",
                    pallets = 20,
                    producerName = "Campos del Norte",
                    originName = "Piura",
                    destinationName = "Lima Central Hub",
                    dock = "C",
                    sealId = "PE-99355-M",
                    sensorId = "TH-219",
                    totalKm = 980.0,
                    travelledKm = 812.0,
                    eta = today.plusMinutes(195),
                    delayMinutes = 45,
                    delayReason = DelayReason.WEIGH_STATION,
                    harvestedAt = now.minusDays(2).withHour(5).withMinute(0),
                    inspectedAt = now.minusDays(2).withHour(8).withMinute(20),
                    departedAt = now.minusMinutes(600),
                    checkpoints = listOf(
                        Checkpoint("Piura", CheckpointKind.ORIGIN, passedAt = today.minusMinutes(600)),
                        Checkpoint("Chiclayo", CheckpointKind.CHECKPOINT, passedAt = today.minusMinutes(430)),
                        Checkpoint("Huarmey", CheckpointKind.CHECKPOINT, passedAt = today.minusMinutes(240), hasIssue = true),
                        Checkpoint("Panamericana Norte", CheckpointKind.CURRENT, km = 812),
                        Checkpoint("Lima Central Hub", CheckpointKind.DESTINATION)
                    )
                )
            )
        }
    }
}
