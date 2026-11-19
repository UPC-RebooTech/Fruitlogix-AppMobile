package com.rebootech.fruitlogix.distributor.logisticsMonitoring.data

import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.ActiveDispatch
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.Checkpoint
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.CheckpointKind
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DeliveryState
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DriverInfo
import com.rebootech.fruitlogix.shared.core.network.DeliveryResponseDto
import com.rebootech.fruitlogix.shared.core.network.TrackingLogResponseDto
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.Locale

/**
 * Server delivery (DeliveryResource) → trip shown in Fleet.
 * The server does not know the lot times, the dock, the seal or the sensor, so they stay empty
 * instead of being invented. Local-only facts (delay reason, reefer check, rejection) are kept
 * from the previous copy of the same trip.
 */
internal object DeliveryMapper {

    /** Trips from the server use "D-" + their id, so they never collide with the sample units (FL-…). */
    fun unitIdFor(remoteId: Int) = "D-$remoteId"

    fun state(currentStatus: String?): DeliveryState = when (currentStatus?.lowercase(Locale.ROOT)) {
        "pendingdispatch" -> DeliveryState.PENDING
        "delivered" -> DeliveryState.DELIVERED
        "failed" -> DeliveryState.REJECTED
        // InTransit and Delayed: the truck is on the road
        else -> DeliveryState.ON_ROUTE
    }

    /** Accepts "2026-11-13T15:30:00", with fraction of seconds, or with a UTC offset ("…Z"). */
    fun parseDateTime(value: String?, zone: ZoneId = ZoneId.systemDefault()): LocalDateTime? {
        if (value.isNullOrBlank()) return null
        return runCatching { OffsetDateTime.parse(value).atZoneSameInstant(zone).toLocalDateTime() }.getOrNull()
            ?: runCatching { LocalDateTime.parse(value) }.getOrNull()
    }

    fun toDispatch(
        dto: DeliveryResponseDto,
        orderCode: String,
        productName: String,
        pallets: Int,
        latestLog: TrackingLogResponseDto?,
        previous: ActiveDispatch?,
        now: LocalDateTime = LocalDateTime.now()
    ): ActiveDispatch {
        val state = state(dto.currentStatus)
        val totalKm = dto.routeDistanceKm ?: 0.0
        val eta = parseDateTime(dto.estimatedTimeOfArrival) ?: now
        val origin = dto.routeOrigin.orEmpty()
        val destination = dto.routeDestination.orEmpty()
        val position = latestLog?.let { log ->
            val lat = log.latitude
            val lon = log.longitude
            if (lat != null && lon != null) String.format(Locale.ROOT, "%.4f, %.4f", lat, lon) else null
        }
        val checkpoints = buildList {
            add(Checkpoint(origin, CheckpointKind.ORIGIN))
            if (position != null && state == DeliveryState.ON_ROUTE) add(Checkpoint(position, CheckpointKind.CURRENT))
            add(Checkpoint(destination, CheckpointKind.DESTINATION))
        }
        // Delay minutes are only known when the ETA has already passed
        val minutesLate = java.time.Duration.between(eta, now).toMinutes().toInt()
        val delayed = dto.currentStatus.equals("Delayed", ignoreCase = true)

        return ActiveDispatch(
            unitId = unitIdFor(dto.id),
            plate = dto.vehiclePlate.orEmpty(),
            truckModel = dto.vehicleType.orEmpty(),
            driver = DriverInfo(dto.driverName.orEmpty(), dto.driverPhone.orEmpty(), license = ""),
            orderId = orderCode,
            productName = productName,
            pallets = pallets,
            producerName = previous?.producerName.orEmpty(),
            originName = origin,
            destinationName = destination,
            dock = "",
            sealId = "",
            sensorId = "",
            totalKm = totalKm,
            travelledKm = if (state == DeliveryState.DELIVERED) totalKm else 0.0,
            eta = eta.toLocalTime(),
            delayMinutes = when {
                previous?.delayMinutes?.let { it > 0 } == true -> previous.delayMinutes
                delayed && minutesLate > 0 -> minutesLate
                else -> 0
            },
            delayReason = previous?.delayReason,
            deliveryState = previous?.deliveryState?.takeIf { it == DeliveryState.DELIVERED || it == DeliveryState.REJECTED } ?: state,
            harvestedAt = null,
            inspectedAt = null,
            departedAt = null,
            deliveredAt = previous?.deliveredAt,
            reeferCheckedAt = previous?.reeferCheckedAt,
            rejectionReason = previous?.rejectionReason,
            checkpoints = checkpoints,
            remoteId = dto.id
        )
    }
}
