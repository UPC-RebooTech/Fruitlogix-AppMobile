package com.rebootech.fruitlogix.infrastructureIot.domain.model

enum class DeviceConnectionStatus {
    CONNECTED,
    CALIBRATING,
    DISCONNECTED
}

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

data class SensorReading(
    val temperatureCelsius: Double,
    val humidityPercent: Double,
    val timestamp: Long = System.currentTimeMillis()
)

data class IotDevice(
    val id: String,
    val deviceCode: String,
    val vehicleId: String,
    val connectionStatus: DeviceConnectionStatus,
    val batteryPercent: Int,
    val lastReading: SensorReading
)

data class IotAlert(
    val id: String,
    val deviceId: String,
    val vehicleCode: String,
    val message: String,
    val severity: AlertSeverity,
    val timestamp: String,
    val isResolved: Boolean = false
)