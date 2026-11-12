package com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.service

import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.GeofenceZone
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.ShipmentEnteredGeofence
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Decides when a truck enters the hub's geofence. Pure Kotlin.
 */
class DetectGeofenceEntryUseCase(
    private val zone: GeofenceZone = GeofenceZone()
) {

    /** Haversine distance in km between a position and the zone center. */
    fun distanceToHubKm(lat: Double, lng: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(zone.centerLat - lat)
        val dLng = Math.toRadians(zone.centerLng - lng)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat)) * cos(Math.toRadians(zone.centerLat)) * sin(dLng / 2) * sin(dLng / 2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    /** @return the event when [distanceKm] is inside the perimeter, otherwise null. */
    fun evaluate(unitId: String, distanceKm: Double): ShipmentEnteredGeofence? =
        if (distanceKm <= zone.radiusKm) {
            // TODO(events): publish ShipmentEnteredGeofence on an event bus when there is one
            ShipmentEnteredGeofence(unitId = unitId, geofenceName = zone.name, distanceKm = distanceKm.toFloat())
        } else null
}
