package com.rebootech.fruitlogix.logisticsMonitoring.domain.model

import com.rebootech.fruitlogix.shared.ui.components.TemperaturePoint

enum class MilestoneStatus {
    COMPLETED,
    IN_TRANSIT,
    PENDING,
    WARNING
}

data class RouteMilestone(
    val id: String,
    val title: String,
    val locationName: String,
    val timestamp: String,
    val status: MilestoneStatus,
    val detailNote: String? = null
)

data class DispatchTemperaturePoint(
    val timeLabel: String,
    val celsius: Float,
    val isExcursion: Boolean = false,
    val excursionDurationMinutes: Int? = null,
    val excursionTooltip: String? = null,
    val isProjected: Boolean = false
)

enum class ChartTimeFilter {
    ONE_HOUR,
    SIX_HOURS,
    FULL_TRIP
}

data class DispatchDetail(
    val unitId: String,
    val licensePlate: String,
    val truckModel: String = "Reefer Express 400",
    val orderId: String = "FX-1042",
    val cargoDescription: String,
    val originName: String,
    val destinationName: String,
    val routeLabel: String,
    val status: DispatchStatus,
    val dockEta: String,
    val progressFraction: Float,
    val progressLabel: String,
    val distanceProgress: String = "182 / 220 km",
    val delayText: String? = null,
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
    val temperaturePoints: List<TemperaturePoint> = emptyList(),
    val detailedTemperaturePoints: List<DispatchTemperaturePoint> = emptyList(),
    val milestones: List<RouteMilestone>,
    val minTempThreshold: Float = 2.0f,
    val maxTempThreshold: Float = 4.0f
)

