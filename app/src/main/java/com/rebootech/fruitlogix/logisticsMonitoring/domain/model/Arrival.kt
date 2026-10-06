package com.rebootech.fruitlogix.logisticsMonitoring.domain.model

/**
 * Represents an inbound arrival unit tracked by geofence at the warehouse.
 */
data class Arrival(
    val unitId: String,
    val licensePlate: String,
    val driverName: String,
    val cargoDescription: String,
    val palletsCount: Int,
    val orderId: String,
    val assignedDock: String,
    val reeferTemp: String,
    val tempSafeRange: String = "Safe 2.0 - 4.0°C",
    val humidity: String = "88% RH",
    val distanceKm: Float,
    val distanceLabel: String,
    val status: ArrivalStatus,
    val etaMinutes: Int? = null,
    val speedKmh: String = "0 km/h",
    val eSealStatus: String = "#PE-99410-X Intact",
    val notes: String? = null,
    val latitude: Double = -12.0464,
    val longitude: Double = -77.0428
)
