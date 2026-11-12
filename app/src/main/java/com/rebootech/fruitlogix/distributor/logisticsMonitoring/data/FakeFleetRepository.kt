package com.rebootech.fruitlogix.distributor.logisticsMonitoring.data

import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.ActiveDispatch
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.Checkpoint
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.CheckpointKind
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DelayReason
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DeliveryState
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DriverInfo
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.repository.FleetRepository
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules.EtaRules
import com.rebootech.fruitlogix.distributor.orderManagement.domain.repository.OrderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Sample fleet of the Lima Central Hub (Callao, Peru). Every truck carries an order that is
 * "on route" in Orders. Reading the orders through their domain interface keeps both sides in
 * sync: an order received at the gate (Scan reception) also closes its trip here.
 */
class FakeFleetRepository(
    private val orderRepository: OrderRepository
) : FleetRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val trips = MutableStateFlow(initialTrips())

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

    override suspend fun updateDeliveryState(unitId: String, state: DeliveryState) =
        change(unitId) { it.copy(deliveryState = state) }

    override suspend fun updateEta(unitId: String, newEta: java.time.LocalTime, reason: DelayReason) =
        change(unitId) { EtaRules.apply(it, newEta, reason) }

    override suspend fun recordReeferCheck(unitId: String) =
        change(unitId) { it.copy(reeferCheckedAt = LocalDateTime.now()) }

    override suspend fun completeDelivery(unitId: String) {
        val trip = dispatch(unitId) ?: return
        change(unitId) { it.copy(deliveryState = DeliveryState.DELIVERED, deliveredAt = LocalDateTime.now(), travelledKm = it.totalKm) }
        // The order is received at the hub: it moves to the order history
        orderRepository.confirmReception(trip.orderId)
    }

    override suspend fun rejectDelivery(unitId: String, reason: String) =
        change(unitId) { it.copy(deliveryState = DeliveryState.REJECTED, rejectionReason = reason.trim()) }

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
