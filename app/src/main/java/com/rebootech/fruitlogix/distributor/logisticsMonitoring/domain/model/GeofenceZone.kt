package com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model

/** Circular geofence around the distributor's hub (Callao, Peru). */
data class GeofenceZone(
    val name: String = "Lima Central Hub",
    val centerLat: Double = -12.0464,
    val centerLng: Double = -77.0428,
    val radiusKm: Double = 2.5
)
