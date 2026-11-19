package com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model

/** Fruit size class: code and approximate weight per fruit. */
data class Caliber(val code: Int, val approxGrams: Int)

/** Lot to inspect for an order. Data only; the UI formats every label. */
data class InspectionLot(
    val orderId: String,
    val lotCode: String,
    val clientName: String,
    val productName: String,
    val netWeightKg: Int,
    val crates: Int,
    val crateKg: Int,
    /** Reason of a previous rejection when this is a re-inspection (written by the distributor). */
    val previousRejectionReason: String?,
    val initialCoreTempC: Double,
    val initialMoisturePct: Double,
    val initialDefectsPct: Double,
    val initialVisualScore: Int,
    val calibers: List<Caliber>,
    val initialCalibers: Set<Int>
) {
    val isReinspection: Boolean get() = previousRejectionReason != null
}

/** What the producer recorded. Numbers are kept as typed text until submitted. */
data class InspectionDraft(
    val orderId: String,
    val visualScore: Int,
    val calibers: Set<Int>,
    val coreTemp: String,
    val moisture: String,
    val defects: String,
    val notes: String
)

enum class InspectionGrade { A, B }

/** Accepted ranges for an export inspection. */
object InspectionRules {
    val CORE_TEMP_RANGE_C = 5.0..8.0
    val MOISTURE_RANGE_PCT = 70.0..82.0
    const val DEFECTS_MAX_PCT = 4.0
    const val GRADE_A_MIN_SCORE = 4
    const val NOTES_MAX = 300

    fun parse(value: String): Double? = value.trim().replace(',', '.').toDoubleOrNull()

    fun coreTempOk(value: String) = parse(value)?.let { it in CORE_TEMP_RANGE_C } ?: false
    fun moistureOk(value: String) = parse(value)?.let { it in MOISTURE_RANGE_PCT } ?: false
    fun defectsOk(value: String) = parse(value)?.let { it in 0.0..DEFECTS_MAX_PCT } ?: false

    fun grade(visualScore: Int) = if (visualScore >= GRADE_A_MIN_SCORE) InspectionGrade.A else InspectionGrade.B

    /** True when every measurement is within range and at least one caliber is selected. */
    fun canSubmit(draft: InspectionDraft) =
        coreTempOk(draft.coreTemp) && moistureOk(draft.moisture) && defectsOk(draft.defects) && draft.calibers.isNotEmpty()
}
