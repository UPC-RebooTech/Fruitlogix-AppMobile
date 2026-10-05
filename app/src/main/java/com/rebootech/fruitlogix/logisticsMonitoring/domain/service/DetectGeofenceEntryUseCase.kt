package com.rebootech.fruitlogix.logisticsMonitoring.domain.service

import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.Arrival
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.GeofenceZone
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.ShipmentEnteredGeofence
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Use case to evaluate whether a shipment location is inside a geofence zone using Haversine distance calculations.
 * Pure Kotlin logic.
 */
class DetectGeofenceEntryUseCase(
    private val defaultZone: GeofenceZone = GeofenceZone()
) {

    /**
     * Calculates distance in kilometers between two lat/lng coordinates using Haversine formula.
     */
    fun calculateDistanceKm(
        lat1: Double,
        lng1: Double,
        lat2: Double = defaultZone.centerLat,
        lng2: Double = defaultZone.centerLng
    ): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Checks if an arrival unit is within the geofence perimeter.
     */
    fun isInsideGeofence(arrival: Arrival, zone: GeofenceZone = defaultZone): Boolean {
        return arrival.distanceKm <= zone.radiusKm
    }

    /**
     * Evaluates geofence entry and publishes domain event.
     */
    fun evaluateAndPublishEvent(arrival: Arrival, zone: GeofenceZone = defaultZone): ShipmentEnteredGeofence? {
        if (isInsideGeofence(arrival, zone)) {
            val event = ShipmentEnteredGeofence(
                unitId = arrival.unitId,
                geofenceName = zone.name,
                distanceKm = arrival.distanceKm
            )
            // TODO: Publish ShipmentEnteredGeofence to core EventBus if available
            return event
        }
        return null
    }
}
