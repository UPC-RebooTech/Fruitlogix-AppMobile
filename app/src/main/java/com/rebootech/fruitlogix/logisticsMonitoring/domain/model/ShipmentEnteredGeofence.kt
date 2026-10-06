package com.rebootech.fruitlogix.logisticsMonitoring.domain.model

/**
 * Domain event published when a shipment unit breaches the warehouse geofence.
 */
data class ShipmentEnteredGeofence(
    val unitId: String,
    val geofenceName: String,
    val distanceKm: Float,
    val timestampMs: Long = System.currentTimeMillis()
)
