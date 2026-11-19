package com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/** Harvest lifecycle state. The UI maps it to a localized label. */
enum class HarvestStatus {
    READY,
    RIPENING,
    SCHEDULED,
    REGISTERED
}

/**
 * Harvest logged in stock. Data only (date, kg, status enum); the UI formats it in the
 * selected language. Crew and sector are place/team names (data, not UI text).
 */
data class PlannedHarvest(
    val id: String,
    val fruitName: String,
    val harvestedAt: LocalDateTime,
    val weightKg: Int,
    val lotCode: String,
    val crewOrTransport: String = "",
    val sectorOrVault: String = "",
    val status: HarvestStatus = HarvestStatus.REGISTERED,
    val brix: Double? = null,
    /** Free text written by the producer (data, not UI text). */
    val notes: String? = null
) {
    // Bridge properties used by the Home "Next harvest" card
    val productName: String get() = fruitName
    val expectedKg: Int get() = weightKg
    val crewName: String get() = crewOrTransport
}

/** Upcoming harvest planned in the field. */
data class ScheduledHarvest(
    val id: String,
    val productName: String,
    val scheduledFor: LocalDateTime,
    val estimatedYieldKg: Int,
    val zone: String,
    val crewName: String,
    val pickers: Int
)

/** Weekly demand forecast for the main product. */
data class HarvestForecast(
    val weekStart: LocalDate,
    val productName: String,
    val requestedKg: Int,
    val fulfilledKg: Int,
    val clients: List<String>
) {
    val fulfilledPct: Int get() = if (requestedKg == 0) 0 else (fulfilledKg * 100 / requestedKg)
}
