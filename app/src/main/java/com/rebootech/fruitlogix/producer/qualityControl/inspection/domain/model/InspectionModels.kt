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
    val initialCalibers: Set<Int>,
    /** Built from a server order: the measurements start empty (nothing is pre-filled). */
    val fromServer: Boolean = false,
    val remoteOrderId: Int? = null,
    val batchId: Int? = null
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
    val notes: String,
    // Fields the server needs for a full inspection (CreateQualityInspectionResource)
    val ph: String = "",
    val brix: String = "",
    val hasStains: Boolean = false,
    val hasBruises: Boolean = false,
    val hasDeformations: Boolean = false,
    val hasRot: Boolean = false,
    val dryCleaningDone: Boolean = false,
    val packagingInspected: Boolean = false,
    val boxesSealed: Boolean = false
)

enum class InspectionGrade { A, B }

/** Accepted ranges for an export inspection. */
object InspectionRules {
    val CORE_TEMP_RANGE_C = 5.0..8.0
    val MOISTURE_RANGE_PCT = 70.0..82.0
    const val DEFECTS_MAX_PCT = 4.0
    const val GRADE_A_MIN_SCORE = 4
    const val NOTES_MAX = 300
    /** Fruit pulp pH and soluble solids (°Brix) accepted by the server form. */
    val PH_RANGE = 2.0..8.0
    val BRIX_RANGE = 0.0..40.0

    fun parse(value: String): Double? = value.trim().replace(',', '.').toDoubleOrNull()

    fun coreTempOk(value: String) = parse(value)?.let { it in CORE_TEMP_RANGE_C } ?: false
    fun moistureOk(value: String) = parse(value)?.let { it in MOISTURE_RANGE_PCT } ?: false
    fun defectsOk(value: String) = parse(value)?.let { it in 0.0..DEFECTS_MAX_PCT } ?: false
    fun phOk(value: String) = parse(value)?.let { it in PH_RANGE } ?: false
    fun brixOk(value: String) = parse(value)?.let { it in BRIX_RANGE } ?: false

    fun grade(visualScore: Int) = if (visualScore >= GRADE_A_MIN_SCORE) InspectionGrade.A else InspectionGrade.B

    /** True when every measurement is within range and at least one caliber is selected. */
    fun canSubmit(draft: InspectionDraft) =
        coreTempOk(draft.coreTemp) && moistureOk(draft.moisture) && defectsOk(draft.defects) &&
            phOk(draft.ph) && brixOk(draft.brix) && draft.calibers.isNotEmpty()
}
