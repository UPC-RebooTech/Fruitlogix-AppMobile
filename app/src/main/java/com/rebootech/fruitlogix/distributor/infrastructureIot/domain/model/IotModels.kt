package com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model

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
    val lastReading: SensorReading,
    val lastSyncLabel: String = "En vivo"
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

data class FruitThreshold(
    val id: String,
    val fruitName: String,
    val minTempCelsius: Double,
    val maxTempCelsius: Double,
    val minHumidityPercent: Double,
    val maxHumidityPercent: Double
)