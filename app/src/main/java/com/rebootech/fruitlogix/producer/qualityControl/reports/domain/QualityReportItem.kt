package com.rebootech.fruitlogix.producer.qualityControl.reports.domain

import java.time.LocalDateTime

enum class QualityStatus {
    PENDING, APPROVED, REJECTED
}

/** Maximum accepted defect percentage; above it a lot is flagged. */
const val DEFECT_LIMIT_PCT = 3.5

/** Accepted ranges for the other inspection metrics. */
const val VISUAL_SCORE_MIN = 4.0
const val CORE_TEMP_MAX_C = 8.0
val HUMIDITY_RANGE_PCT = 85..95

/**
 * Quality report. Data only: dates are [LocalDateTime] and alerts are derived from the
 * metrics, so the UI builds every sentence in the selected language.
 */
data class QualityReportItem(
    val id: String,
    val orderNumber: String,
    val clientName: String,
    val inspectedAt: LocalDateTime,
    val locationInfo: String,
    val status: QualityStatus,
    val productName: String,
    val weightKg: Int,
    val cratesCount: Int,
    /** Lot code without prefix, e.g. "HA-882". */
    val lotCode: String,
    val visualScore: Double,
    val defectsPercentage: Double,
    val coreTemp: Double,
    val humidityPercentage: Int,
    val inspectorName: String,
    /** Free text written by the inspector (data, not UI text). */
    val inspectorNotes: String? = null,
    val harvestedOn: java.time.LocalDate? = null
) {
    val visualOk: Boolean get() = visualScore >= VISUAL_SCORE_MIN
    val defectsOk: Boolean get() = defectsPercentage <= DEFECT_LIMIT_PCT
    val coreTempOk: Boolean get() = coreTemp <= CORE_TEMP_MAX_C
    val humidityOk: Boolean get() = humidityPercentage in HUMIDITY_RANGE_PCT

    /** Rejected because defects exceed [DEFECT_LIMIT_PCT]; the lot is quarantined for re-grading. */
    val isQuarantined: Boolean
        get() = status == QualityStatus.REJECTED && defectsPercentage > DEFECT_LIMIT_PCT
}

/** Weekly quality summary shown at the top of the Reports screen. */
data class QualitySummary(
    val passRatePct: Double,
    val weeklyDeltaPct: Double,
    val sampledTodayKg: Int,
    val cratesInspectedToday: Int
)
