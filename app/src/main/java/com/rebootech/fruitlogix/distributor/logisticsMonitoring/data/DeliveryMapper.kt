package com.rebootech.fruitlogix.distributor.logisticsMonitoring.data

import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.ActiveDispatch
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.Checkpoint
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.CheckpointKind
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DeliveryState
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DriverInfo
import com.rebootech.fruitlogix.shared.core.network.DeliveryEventDto
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

    /**
     * PendingDispatch → pending; InTransit, Delayed and Arrived → on route (Arrived waits at the gate
     * for the client's reception); Delivered → delivered; Failed → rejected. Unknown values → on route.
     */
    fun state(currentStatus: String?): DeliveryState = when (currentStatus?.lowercase(Locale.ROOT)) {
        "pendingdispatch" -> DeliveryState.PENDING
        "delivered" -> DeliveryState.DELIVERED
        "failed" -> DeliveryState.REJECTED
        else -> DeliveryState.ON_ROUTE
    }

    fun isArrived(currentStatus: String?) = currentStatus.equals("Arrived", ignoreCase = true)

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
        events: List<DeliveryEventDto> = emptyList(),
        now: LocalDateTime = LocalDateTime.now(),
        zone: ZoneId = ZoneId.systemDefault()
    ): ActiveDispatch {
        val state = state(dto.currentStatus)
        val arrived = isArrived(dto.currentStatus)
        val totalKm = dto.routeDistanceKm ?: 0.0
        val eta = parseDateTime(dto.estimatedTimeOfArrival, zone) ?: now
        val dispatchedAt = parseDateTime(dto.dispatchedAt, zone)
        val arrivedAt = parseDateTime(dto.arrivedAt, zone) ?: if (arrived) previous?.arrivedAt ?: now else null
        val deliveredAt = parseDateTime(dto.deliveredAt, zone)

        // Delay minutes are only known when the ETA has already passed
        val minutesLate = java.time.Duration.between(eta, now).toMinutes().toInt()
        val delayed = dto.currentStatus.equals("Delayed", ignoreCase = true)

        // Rejected stays on this phone (the server has no endpoint for it yet)
        val keepRejected = previous?.deliveryState == DeliveryState.REJECTED && state != DeliveryState.DELIVERED

        return ActiveDispatch(
            unitId = unitIdFor(dto.id),
            plate = dto.vehiclePlate.orEmpty(),
            truckModel = dto.vehicleType.orEmpty(),
            driver = DriverInfo(dto.driverName.orEmpty(), dto.driverPhone.orEmpty(), license = ""),
            orderId = orderCode,
            productName = productName,
            pallets = pallets,
            producerName = previous?.producerName.orEmpty(),
            originName = dto.routeOrigin.orEmpty(),
            destinationName = dto.routeDestination.orEmpty(),
            dock = "",
            sealId = "",
            sensorId = "",
            totalKm = totalKm,
            // Arrived or delivered: the whole route is done (the truck is at the client)
            travelledKm = if (arrived || state == DeliveryState.DELIVERED) totalKm else 0.0,
            eta = eta.toLocalTime(),
            delayMinutes = when {
                previous?.delayMinutes?.let { it > 0 } == true -> previous.delayMinutes
                delayed && minutesLate > 0 -> minutesLate
                else -> 0
            },
            delayReason = previous?.delayReason,
            deliveryState = if (keepRejected) DeliveryState.REJECTED else state,
            harvestedAt = null,
            inspectedAt = null,
            departedAt = dispatchedAt,
            deliveredAt = deliveredAt ?: previous?.deliveredAt?.takeIf { state == DeliveryState.DELIVERED },
            arrivedAt = arrivedAt,
            reeferCheckedAt = previous?.reeferCheckedAt,
            rejectionReason = previous?.rejectionReason,
            checkpoints = checkpoints(dto, state, arrived, dispatchedAt, arrivedAt, events, latestLog, zone),
            remoteId = dto.id
        )
    }

    /** Origin, the checkpoints registered on the server, the last GPS position and the destination. */
    private fun checkpoints(
        dto: DeliveryResponseDto,
        state: DeliveryState,
        arrived: Boolean,
        dispatchedAt: LocalDateTime?,
        arrivedAt: LocalDateTime?,
        events: List<DeliveryEventDto>,
        latestLog: TrackingLogResponseDto?,
        zone: ZoneId
    ): List<Checkpoint> = buildList {
        add(Checkpoint(dto.routeOrigin.orEmpty(), CheckpointKind.ORIGIN, passedAt = dispatchedAt?.toLocalTime()))
        events
            .filter { it.type.equals("Checkpoint", ignoreCase = true) }
            .sortedBy { parseDateTime(it.occurredAt, zone) }
            .forEach { event ->
                add(
                    Checkpoint(
                        name = event.name?.takeIf { it.isNotBlank() } ?: position(event.latitude, event.longitude).orEmpty(),
                        kind = CheckpointKind.CHECKPOINT,
                        passedAt = parseDateTime(event.occurredAt, zone)?.toLocalTime()
                    )
                )
            }
        val gps = latestLog?.let { position(it.latitude, it.longitude) }
        if (gps != null && state == DeliveryState.ON_ROUTE && !arrived) add(Checkpoint(gps, CheckpointKind.CURRENT))
        add(Checkpoint(dto.routeDestination.orEmpty(), CheckpointKind.DESTINATION, passedAt = arrivedAt?.toLocalTime()))
    }

    /** "-12.0464, -77.0428"; null without coordinates or at (0,0), which means "no GPS signal". */
    fun position(latitude: Double?, longitude: Double?): String? {
        if (latitude == null || longitude == null) return null
        if (latitude == 0.0 && longitude == 0.0) return null
        return String.format(Locale.ROOT, "%.4f, %.4f", latitude, longitude)
    }
}
