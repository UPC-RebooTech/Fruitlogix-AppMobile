package com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules

import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.TemperatureReading
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.ActiveDispatch
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.ColdChainCertificate
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DelayReason
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DeliveryProof
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DeliveryState
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.LotStage
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.LotStageType
import java.security.MessageDigest
import java.time.Duration
import java.time.LocalTime

/** Order status changes (US14): forward one step at a time, never skipping "on route". */
object DeliveryStateMachine {

    sealed interface Result {
        data object Allowed : Result
        data object NoChange : Result
        /** Pending → Delivered: the truck has to be on route first. */
        data object CannotSkipOnRoute : Result
        /** Delivered or rejected orders are closed. */
        data object AlreadyClosed : Result
        /** On route → Delivered is allowed only by registering the delivery proof. */
        data object RequiresDeliveryProof : Result
    }

    fun check(from: DeliveryState, to: DeliveryState): Result = when {
        from == to -> Result.NoChange
        from == DeliveryState.DELIVERED || from == DeliveryState.REJECTED -> Result.AlreadyClosed
        from == DeliveryState.PENDING && to == DeliveryState.DELIVERED -> Result.CannotSkipOnRoute
        from == DeliveryState.PENDING && to == DeliveryState.REJECTED -> Result.CannotSkipOnRoute
        to == DeliveryState.DELIVERED || to == DeliveryState.REJECTED -> Result.RequiresDeliveryProof
        else -> Result.Allowed // Pending ⇄ On route
    }
}

/** New ETA when a delivery is late (US17). */
object EtaRules {

    enum class Error { NEW_ETA_REQUIRED, NOT_LATER_THAN_CURRENT, REASON_REQUIRED }

    fun validate(currentEta: LocalTime, newEta: LocalTime?, reason: DelayReason?): Error? = when {
        newEta == null -> Error.NEW_ETA_REQUIRED
        !newEta.isAfter(currentEta) -> Error.NOT_LATER_THAN_CURRENT
        reason == null -> Error.REASON_REQUIRED
        else -> null
    }

    /** Applies a valid new ETA: the delay adds up and the reason is kept. */
    fun apply(dispatch: ActiveDispatch, newEta: LocalTime, reason: DelayReason): ActiveDispatch {
        val added = Duration.between(dispatch.eta, newEta).toMinutes().toInt().coerceAtLeast(0)
        return dispatch.copy(eta = newEta, delayMinutes = dispatch.delayMinutes + added, delayReason = reason)
    }
}

/** Reefer check before dispatch or at any time (US64). */
object ReeferRules {

    enum class Status { WITHIN_RANGE, TOO_COLD, TOO_WARM }

    fun check(celsius: Double, min: Double, max: Double): Status = when {
        celsius < min -> Status.TOO_COLD
        celsius > max -> Status.TOO_WARM
        else -> Status.WITHIN_RANGE
    }
}

/** What a delivery needs before it can be closed (US16, US52, US56). */
object DeliveryProofRules {

    enum class Error { MISSING_SIGNATURE, MISSING_PHOTO, LOCATION_NOT_CONFIRMED, REJECTION_REASON_REQUIRED }

    fun validate(proof: DeliveryProof): Error? = when {
        !proof.hasSignature -> Error.MISSING_SIGNATURE
        !proof.hasPhoto -> Error.MISSING_PHOTO
        !proof.insideDeliveryRadius && !proof.outsideLocationConfirmed -> Error.LOCATION_NOT_CONFIRMED
        else -> null
    }

    fun validateRejection(reason: String): Error? =
        if (reason.isBlank()) Error.REJECTION_REASON_REQUIRED else null
}

/** Builds the cold-chain certificate from the trip readings (US71). */
object ColdChainCertificateBuilder {

    /** @return null when there are no readings: a certificate without data would be meaningless. */
    fun build(
        dispatch: ActiveDispatch,
        readings: List<TemperatureReading>,
        min: Double,
        max: Double
    ): ColdChainCertificate? {
        if (readings.isEmpty()) return null
        val excursions = readings.count { it.celsius < min || it.celsius > max }
        val source = listOf(
            dispatch.orderId, dispatch.unitId, dispatch.plate, dispatch.driver.license,
            min, max, readings.size, excursions
        ).joinToString("|")
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(source.toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(24)
            .uppercase()
        return ColdChainCertificate(
            orderId = dispatch.orderId,
            unitId = dispatch.unitId,
            plate = dispatch.plate,
            productName = dispatch.productName,
            route = "${dispatch.originName} → ${dispatch.destinationName}",
            minCelsius = min,
            maxCelsius = max,
            readingsCount = readings.size,
            excursions = excursions,
            hash = hash
        )
    }
}

/** Lot traceability from harvest to invoice (US72). */
object LotTraceability {

    fun stages(dispatch: ActiveDispatch): List<LotStage> {
        val delivered = dispatch.deliveryState == DeliveryState.DELIVERED
        return listOf(
            LotStage(LotStageType.HARVESTED, dispatch.harvestedAt, done = true),
            LotStage(LotStageType.INSPECTED, dispatch.inspectedAt, done = true),
            LotStage(LotStageType.LOADED, dispatch.departedAt, done = true),
            LotStage(LotStageType.RECEIVED, dispatch.deliveredAt, done = delivered),
            // Invoicing happens automatically when the delivery is confirmed
            LotStage(LotStageType.INVOICED, dispatch.deliveredAt, done = delivered)
        )
    }
}

/** Which trucks count as "arrivals" at the hub. */
object ArrivalRules {
    /** Trucks closer than this are shown in Arrivals. */
    const val ARRIVALS_RADIUS_KM = 60.0
    /** Closer than this the truck is at the gate and reception can start. */
    const val AT_GATE_KM = 0.5

    fun isArriving(dispatch: ActiveDispatch): Boolean =
        dispatch.deliveryState == DeliveryState.ON_ROUTE && dispatch.remainingKm <= ARRIVALS_RADIUS_KM

    fun isAtGate(dispatch: ActiveDispatch): Boolean =
        isArriving(dispatch) && dispatch.remainingKm <= AT_GATE_KM
}
