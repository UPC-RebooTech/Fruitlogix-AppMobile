package com.rebootech.fruitlogix.logisticsMonitoring.domain.model

import com.rebootech.fruitlogix.shared.ui.components.TemperaturePoint

enum class MilestoneStatus {
    COMPLETED,
    IN_TRANSIT,
    PENDING
}

data class RouteMilestone(
    val id: String,
    val title: String,
    val locationName: String,
    val timestamp: String,
    val status: MilestoneStatus,
    val detailNote: String? = null
)

data class DispatchDetail(
    val unitId: String,
    val licensePlate: String,
    val truckModel: String,
    val cargoDescription: String,
    val originName: String,
    val destinationName: String,
    val routeLabel: String,
    val status: DispatchStatus,
    val dockEta: String,
    val progressFraction: Float,
    val progressLabel: String,
    val currentTemp: String,
    val tempTarget: String,
    val rateOfRise: String,
    val timeToBreachMinutes: Int,
    val humidity: String,
    val speedKmh: String,
    val ambientTemp: String,
    val batteryPercent: Int,
    val sensorId: String,
    val sensorSignal: String,
    val currentLocationLabel: String,
    val hasPredictiveAlert: Boolean,
    val predictiveAlertMessage: String?,
    val driverName: String,
    val driverPhone: String,
    val driverLicense: String,
    val temperaturePoints: List<TemperaturePoint>,
    val milestones: List<RouteMilestone>,
    val minTempThreshold: Float = 2.0f,
    val maxTempThreshold: Float = 4.0f
)
