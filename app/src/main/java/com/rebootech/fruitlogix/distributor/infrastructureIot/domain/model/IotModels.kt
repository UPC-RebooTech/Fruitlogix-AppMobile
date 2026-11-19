package com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model

import java.time.LocalDateTime

enum class DeviceStatus { ONLINE, LOW_BATTERY, OFFLINE, CALIBRATING }

data class SensorReading(
    val celsius: Double,
    val humidityPercent: Double,
    val at: LocalDateTime
)

/**
 * A reefer or warehouse sensor. Truck sensors carry the fleet [unitId] (FL-102…), so Fleet,
 * Dispatch detail and Home read the same temperature for the same truck.
 */
data class IotDevice(
    val id: String,
    /** Truck it is mounted on; null for warehouse sensors. */
    val unitId: String?,
    /** Place for warehouse sensors (data, not translated). */
    val location: String?,
    val status: DeviceStatus,
    val batteryPercent: Int,
    val lastReading: SensorReading
) {
    val isLowBattery: Boolean get() = batteryPercent <= LOW_BATTERY_PERCENT

    companion object {
        const val LOW_BATTERY_PERCENT = 20
    }
}

enum class AlertSeverity { INFO, WARNING, CRITICAL }

/** What an alert is about; the message is built in presentation, in the user's language. */
enum class AlertKind { TEMP_RISK, TEMP_OUT_OF_RANGE, SENSOR_OFFLINE, LOW_BATTERY }

data class IotAlert(
    val id: String,
    val deviceId: String,
    val unitId: String?,
    val kind: AlertKind,
    val severity: AlertSeverity,
    val createdAt: LocalDateTime,
    /** Measured value (°C or battery %), when it applies. */
    val value: Double? = null,
    /** The limit that was crossed or is about to be, when it applies. */
    val limit: Double? = null
)

/** Storage limits per fruit (US62). [fruitName] matches the order's product name. */
data class FruitThreshold(
    val id: String,
    val fruitName: String,
    val minTempCelsius: Double,
    val maxTempCelsius: Double,
    val minHumidityPercent: Double,
    val maxHumidityPercent: Double
)

/** Validation of fruit thresholds (US62). */
object ThresholdRules {
    const val MIN_TEMP = -5.0
    const val MAX_TEMP = 30.0
    const val MIN_HUMIDITY = 40.0
    const val MAX_HUMIDITY = 100.0

    enum class Error { NOT_A_NUMBER, TEMP_RANGE, HUMIDITY_RANGE }

    fun validate(minTemp: Double?, maxTemp: Double?, minHum: Double?, maxHum: Double?): Error? = when {
        minTemp == null || maxTemp == null || minHum == null || maxHum == null -> Error.NOT_A_NUMBER
        minTemp < MIN_TEMP || maxTemp > MAX_TEMP || minTemp >= maxTemp -> Error.TEMP_RANGE
        minHum < MIN_HUMIDITY || maxHum > MAX_HUMIDITY || minHum >= maxHum -> Error.HUMIDITY_RANGE
        else -> null
    }
}
