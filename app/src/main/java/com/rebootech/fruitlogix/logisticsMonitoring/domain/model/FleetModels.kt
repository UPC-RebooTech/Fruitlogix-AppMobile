package com.rebootech.fruitlogix.logisticsMonitoring.domain.model

/**
 * Domain models for the Fleet Control Center screen.
 * All data is sourced from Peru / Lima operations hub.
 */

// ============================================================
// Dispatch / Active Dispatches
// ============================================================

enum class DispatchStatus {
    ON_TIME,
    TEMP_ALERT,
    WEIGH_STATION_DELAY
}

/**
 * Summary of a single active dispatch shown in the Fleet tab card list.
 */
data class DispatchSummary(
    /** Unique dispatch route identifier, e.g. "FL-102". */
    val id: String,
    /** Peruvian plate in ABC-123 format, e.g. "BQK-482". */
    val licensePlate: String,
    /** Cargo description, e.g. "Ica grapes - 16 Pallets". */
    val cargoDescription: String,
    /** Route label, e.g. "Ica -> Lima Central Hub". */
    val routeLabel: String,
    /** Reefer temperature string, e.g. "3.4C". */
    val reeferTemp: String,
    /** Target reefer temperature string, e.g. "Target 3.0C". */
    val reeferTempTarget: String,
    /** Humidity reading string, e.g. "88% RH". */
    val humidity: String,
    /** Dock ETA label in 24h PET format, e.g. "14:30 PET". */
    val dockEta: String,
    /** Optional delay suffix appended to ETA, e.g. "+45m". */
    val etaDelta: String? = null,
    /** Transit progress 0.0-1.0. */
    val progressFraction: Float,
    /** Progress label text, e.g. "284 / 395 km". */
    val progressLabel: String,
    /** Status: ON_TIME, TEMP_ALERT, WEIGH_STATION_DELAY. */
    val status: DispatchStatus,
    /** Bottom note shown on the card, e.g. "BLE Sensatag Sync OK". */
    val footerNote: String? = null,
    /** True when an immediate reefer diagnostic button should be shown. */
    val showImmediateDiagnostic: Boolean = false
)

// ============================================================
// Fleet Alerts
// ============================================================

/**
 * Lightweight alert item shown in the Alerts tab.
 */
data class FleetAlert(
    val id: String,
    val title: String,
    val unitId: String,
    val severity: DispatchStatus
)

// ============================================================
// Sensors
// ============================================================

enum class SensorStatus {
    ONLINE,
    LOW_BATTERY,
    OFFLINE,
    STANDBY
}

enum class SensorType {
    TEMPERATURE,
    HUMIDITY,
    GPS
}

/** Filter categories for the Sensors tab. */
enum class SensorFilter {
    ALL,
    ON_ROUTE,
    IN_WAREHOUSE,
    OFFLINE
}

/**
 * A single IoT sensor node.
 */
data class Sensor(
    val id: String,
    val type: SensorType,
    /** Optional linked truck unit ID. */
    val linkedTruckId: String?,
    /** Optional linked order number. */
    val linkedOrderId: String?,
    /** Human-readable cargo / order name. */
    val cargoOrLocation: String,
    /** Last seen signal string, e.g. "2 min ago - BLE Gateway Sync". */
    val lastSignal: String,
    /** Battery percentage 0-100. */
    val batteryPercent: Int,
    val status: SensorStatus,
    /** Extra detail label 1, e.g. "CHILL CORE". */
    val detailLabel1: String? = null,
    val detailValue1: String? = null,
    /** Extra detail label 2, e.g. "HUMIDITY". */
    val detailLabel2: String? = null,
    val detailValue2: String? = null,
    /** Location string for unlinked warehouse sensors, e.g. "Callao Cold Hub, Dock B". */
    val warehouseLocation: String? = null,
    /** True for the offline TH-09 card: shows "Last seen 47 min ago". */
    val isOfflineWithLastSeen: Boolean = false
)

// ============================================================
// Temperature threshold logic (domain layer)
// ============================================================

/**
 * Checks whether a reefer dispatch is within temperature bounds.
 * @param currentCelsius The latest reefer temperature reading.
 * @param maxCelsius The threshold (upper bound) in Celsius.
 * @return True if the temperature is at or below the threshold.
 */
fun isReeferWithinThreshold(currentCelsius: Double, maxCelsius: Double): Boolean =
    currentCelsius <= maxCelsius
