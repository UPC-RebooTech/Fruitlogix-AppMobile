package com.rebootech.fruitlogix.logisticsMonitoring.domain.model

/**
 * Circular geofence zone around a warehouse center point.
 */
data class GeofenceZone(
    val name: String = "Callao Cold Hub Perimeter",
    val centerLat: Double = -12.0464,
    val centerLng: Double = -77.0428,
    val radiusKm: Double = 2.5
)
