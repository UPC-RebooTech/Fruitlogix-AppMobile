package com.rebootech.fruitlogix.producer.infrastructureIot.domain.model

import java.time.LocalDateTime

enum class AlertSeverity { CRITICAL, WARNING, INFO }

/** Where the sensor is installed. The UI turns it into a localized label ("Cold room 1"). */
enum class AssetType { COLD_ROOM, WAREHOUSE, TRUCK, GATEWAY }

data class MonitoredAsset(val type: AssetType, val code: String)

/**
 * What the sensor detected. Each kind carries the values the UI needs to build
 * a localized title and description.
 */
sealed class AlertKind {
    data class TemperatureHigh(val valueC: Double, val limitC: Double) : AlertKind()
    data class HumidityLow(val valuePct: Double, val minimumPct: Double) : AlertKind()
    data class Vibration(val dock: String) : AlertKind()
    data class TraceabilitySynced(val kg: Int, val productName: String) : AlertKind()
}

/** Field/IoT alert shown in the producer's notification inbox. */
data class IotAlert(
    val id: String,
    val kind: AlertKind,
    val severity: AlertSeverity,
    val asset: MonitoredAsset,
    val sensorId: String,
    val location: String,
    val occurredAt: LocalDateTime,
    val relatedOrderId: String? = null,
    val isRead: Boolean = false
) {
    /** Cold-room alerts: temperature or humidity measured in a cold room. */
    val isColdChain: Boolean
        get() = asset.type == AssetType.COLD_ROOM ||
            kind is AlertKind.TemperatureHigh || kind is AlertKind.HumidityLow
}
