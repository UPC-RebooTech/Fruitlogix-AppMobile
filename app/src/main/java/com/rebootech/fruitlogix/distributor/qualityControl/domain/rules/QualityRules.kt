package com.rebootech.fruitlogix.distributor.qualityControl.domain.rules

import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.RejectionKind
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.ShrinkageRecord
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.ShrinkageSummary
import java.time.YearMonth

/** A rejected lot always carries a reason (US10). */
object RejectionRules {
    enum class Error { REASON_REQUIRED, NOTE_REQUIRED }

    fun validate(kind: RejectionKind?, note: String): Error? = when {
        kind == null -> Error.REASON_REQUIRED
        kind == RejectionKind.OTHER && note.isBlank() -> Error.NOTE_REQUIRED
        else -> null
    }
}

/** Offline field inspection (US53). */
object FieldInspectionRules {
    const val MIN_TEMP = 0.0
    const val MAX_TEMP = 40.0

    enum class Error { LOT_REQUIRED, PRODUCT_REQUIRED, TEMP_INVALID, HUMIDITY_INVALID }

    /** @return the errors per field (empty when the inspection can be saved). */
    fun validate(lotCode: String, product: String, temp: Double?, humidity: Double?): Set<Error> = buildSet {
        if (lotCode.isBlank()) add(Error.LOT_REQUIRED)
        if (product.isBlank()) add(Error.PRODUCT_REQUIRED)
        if (temp == null || temp < MIN_TEMP || temp > MAX_TEMP) add(Error.TEMP_INVALID)
        if (humidity == null || humidity < 0.0 || humidity > 100.0) add(Error.HUMIDITY_INVALID)
    }
}

/** Incident reported by voice (US57). Recordings shorter than one second are discarded. */
object VoiceIncidentRules {
    const val MIN_SECONDS = 1

    enum class Error { LOT_REQUIRED, DESCRIPTION_REQUIRED, RECORDING_TOO_SHORT }

    fun isValidRecording(seconds: Int): Boolean = seconds >= MIN_SECONDS

    fun validate(lotCode: String, description: String, recordedSeconds: Int?): Error? = when {
        lotCode.isBlank() -> Error.LOT_REQUIRED
        description.isBlank() -> Error.DESCRIPTION_REQUIRED
        recordedSeconds == null || !isValidRecording(recordedSeconds) -> Error.RECORDING_TOO_SHORT
        else -> null
    }
}

/** Shrinkage report of a month (US73). */
object ShrinkageRules {
    fun summary(records: List<ShrinkageRecord>, month: YearMonth): ShrinkageSummary {
        val inMonth = records.filter { it.month == month }.sortedByDescending { it.lostKg }
        return ShrinkageSummary(inMonth.sumOf { it.lostKg }, inMonth.sumOf { it.costPen }, inMonth)
    }
}
