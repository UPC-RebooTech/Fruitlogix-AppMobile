package com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model

import java.time.LocalDateTime
import java.time.LocalTime

/**
 * One truck carrying one order to the distributor's hub. This is the single source for
 * every screen (Home, Fleet, Dispatch detail, Arrivals): same plate, driver, ETA and order.
 * Reefer temperature and humidity are NOT here: they come from the IoT context by [sensorId].
 */
data class ActiveDispatch(
    val unitId: String,
    val plate: String,
    val truckModel: String,
    val driver: DriverInfo,
    val orderId: String,
    val productName: String,
    val pallets: Int,
    val producerName: String,
    val originName: String,
    val destinationName: String,
    /** Dock letter at the hub, e.g. "B". */
    val dock: String,
    /** Electronic seal on the container doors. */
    val sealId: String,
    val sensorId: String,
    val totalKm: Double,
    val travelledKm: Double,
    val eta: LocalTime,
    val delayMinutes: Int = 0,
    val delayReason: DelayReason? = null,
    val deliveryState: DeliveryState = DeliveryState.ON_ROUTE,
    /** Lot times; null when unknown (the server does not send them). */
    val harvestedAt: LocalDateTime?,
    val inspectedAt: LocalDateTime?,
    val departedAt: LocalDateTime?,
    val deliveredAt: LocalDateTime? = null,
    val reeferCheckedAt: LocalDateTime? = null,
    val rejectionReason: String? = null,
    val checkpoints: List<Checkpoint>,
    /** Delivery id on the FruitLogix server; null for the sample trips that only live on this phone. */
    val remoteId: Int? = null
) {
    val remainingKm: Double get() = (totalKm - travelledKm).coerceAtLeast(0.0)
    val progress: Float get() = if (totalKm <= 0.0) 0f else (travelledKm / totalKm).toFloat().coerceIn(0f, 1f)
    val isDelayed: Boolean get() = delayMinutes > 0

    /** Still the distributor's concern: not delivered and not rejected. */
    val isActive: Boolean get() = deliveryState == DeliveryState.PENDING || deliveryState == DeliveryState.ON_ROUTE
}

data class DriverInfo(
    val name: String,
    val phone: String,
    val license: String
)

enum class CheckpointKind { ORIGIN, CHECKPOINT, CURRENT, DESTINATION }

/**
 * A point on the route. [name] is a place name (data, not translated).
 * [passedAt] is null for points not reached yet; for the destination the ETA is used instead.
 */
data class Checkpoint(
    val name: String,
    val kind: CheckpointKind,
    val passedAt: LocalTime? = null,
    /** Km mark, used for the current position. */
    val km: Int? = null,
    /** Something went wrong here (e.g. a long weigh-station stop). */
    val hasIssue: Boolean = false
)

/** Order status as the distributor sees the trip (US14). */
enum class DeliveryState { PENDING, ON_ROUTE, DELIVERED, REJECTED }

/** Why a delivery is late (US17). Shown as one-tap choices. */
enum class DelayReason { TRAFFIC, WEIGH_STATION, ROAD_WORKS, WEATHER, MECHANICAL, OTHER }

/** Evidence collected at the destination (US16 signature, US52 photo, US56 GPS). */
data class DeliveryProof(
    val hasSignature: Boolean,
    val hasPhoto: Boolean,
    /** The phone's GPS is inside the expected delivery radius. */
    val insideDeliveryRadius: Boolean,
    /** When outside the radius, the person confirmed the alternative location by hand. */
    val outsideLocationConfirmed: Boolean = false
)

/** Cold-chain certificate of a trip (US71). */
data class ColdChainCertificate(
    val orderId: String,
    val unitId: String,
    val plate: String,
    val productName: String,
    val route: String,
    val minCelsius: Double,
    val maxCelsius: Double,
    val readingsCount: Int,
    val excursions: Int,
    val hash: String
) {
    val isCompliant: Boolean get() = excursions == 0
}

enum class LotStageType { HARVESTED, INSPECTED, LOADED, RECEIVED, INVOICED }

/** One step of the lot's end-to-end traceability (US72). */
data class LotStage(
    val type: LotStageType,
    val at: LocalDateTime?,
    val done: Boolean
)
