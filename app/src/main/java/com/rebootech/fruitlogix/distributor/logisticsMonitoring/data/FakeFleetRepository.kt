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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap

class FakeFleetRepository(
    private val orderRepository: OrderRepository
) : FleetRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val trips = MutableStateFlow(initialTrips())
    private val deliveryIdByUnit = ConcurrentHashMap<String, Int>(
        mapOf("FL-408" to 1, "FL-102" to 2, "FL-219" to 3)
    )

    init {
        scope.launch { refresh() }
    }

    override val dispatches: StateFlow<List<ActiveDispatch>> =
        combine(trips, orderRepository.orders) { list, orders ->
            val activeOrderIds = orders.map { it.id }.toSet()
            list.map { trip ->
                if (trip.isActive && trip.orderId !in activeOrderIds) {
                    trip.copy(deliveryState = DeliveryState.DELIVERED, deliveredAt = trip.deliveredAt ?: LocalDateTime.now())
                } else trip
            }
        }.stateIn(scope, SharingStarted.Eagerly, trips.value)

    override fun dispatch(unitId: String): ActiveDispatch? =
        dispatches.value.firstOrNull { it.unitId.equals(unitId, ignoreCase = true) }

    private fun change(unitId: String, block: (ActiveDispatch) -> ActiveDispatch) =
        trips.update { list -> list.map { if (it.unitId.equals(unitId, ignoreCase = true)) block(it) else it } }

    override suspend fun updateDeliveryState(unitId: String, state: DeliveryState) {
        change(unitId) { it.copy(deliveryState = state) }
        if (state == DeliveryState.ON_ROUTE) {
            val remoteId = deliveryIdByUnit[unitId.uppercase()] ?: return
            scope.launch {
                runCatching { RetrofitClient.api.startDelivery(remoteId) }
                    .onFailure { Log.w("FruitLogix-API", "POST /deliveries/$remoteId/start falló: ${it.message}") }
            }
        }
    }

    override suspend fun updateEta(unitId: String, newEta: LocalTime, reason: DelayReason) {
        val current = dispatch(unitId) ?: return
        val updated = EtaRules.apply(current, newEta, reason)
        change(unitId) { updated }

        val remoteId = deliveryIdByUnit[unitId.uppercase()] ?: return
        scope.launch {
            runCatching {
                RetrofitClient.api.reportDeliveryDelay(
                    id = remoteId,
                    request = ReportDelayRequestDto(
                        newEstimatedArrival = LocalDate.now().atTime(newEta).toString(),
                        delayMinutes = updated.delayMinutes.coerceAtLeast(5),
                        reason = reason.name
                    )
                )
            }.onFailure {
                Log.w("FruitLogix-API", "POST /deliveries/$remoteId/delay falló: ${it.message}")
            }
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
        val remoteId = deliveryIdByUnit[unitId.uppercase()]
        if (remoteId != null) {
            scope.launch {
                runCatching { RetrofitClient.api.completeDelivery(remoteId) }
            }
        }
        orderRepository.confirmReception(trip.orderId)
    }

    override suspend fun rejectDelivery(unitId: String, reason: String) =
        change(unitId) { it.copy(deliveryState = DeliveryState.REJECTED, rejectionReason = reason.trim()) }

    override suspend fun refresh() {
        withContext(Dispatchers.IO) {
            val result = runCatching { RetrofitClient.api.getDeliveries() }
            result.onSuccess { response ->
                if (response.isSuccessful) {
                    RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
                    val remoteDeliveries = response.body().orEmpty()
                    remoteDeliveries.forEach { dto ->
                        mergeRemoteDelivery(dto)
                    }
                }
            }.onFailure { err ->
                Log.w("FruitLogix-API", "GET /deliveries no disponible (${err.message}); usando despachos locales.")
            }

            // Consultar TrackingLogs para las entregas activas conocidas
            for ((unitCode, delivId) in deliveryIdByUnit.entries) {
                runCatching { RetrofitClient.api.getTrackingLogs(delivId) }.onSuccess { resp ->
                    if (resp.isSuccessful) {
                        val logs = resp.body().orEmpty()
                        val latestLocation = logs.lastOrNull()?.location?.takeIf { it.isNotBlank() }
                        if (latestLocation != null) {
                            change(unitCode) { active ->
                                val updatedCheckpoints = active.checkpoints.map { cp ->
                                    if (cp.kind == CheckpointKind.CURRENT) cp.copy(name = latestLocation) else cp
                                }
                                active.copy(checkpoints = updatedCheckpoints)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun mergeRemoteDelivery(dto: DeliveryResponseDto) {
        val unitCode = dto.unitCode?.takeIf { it.isNotBlank() } ?: return
        dto.id?.let { deliveryIdByUnit[unitCode.uppercase()] = it }

        val mappedState = when (dto.status?.uppercase()) {
            "PENDING", "CREATED" -> DeliveryState.PENDING
            "DELIVERED", "COMPLETED" -> DeliveryState.DELIVERED
            "REJECTED", "CANCELLED" -> DeliveryState.REJECTED
            else -> DeliveryState.ON_ROUTE
        }

        change(unitCode) { existing ->
            existing.copy(
                deliveryState = mappedState,
                delayMinutes = dto.delayMinutes ?: existing.delayMinutes
            )
        }
    }

    private companion object {
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