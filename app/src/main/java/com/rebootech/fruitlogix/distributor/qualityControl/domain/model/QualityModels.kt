package com.rebootech.fruitlogix.distributor.qualityControl.domain.model

import java.time.LocalDateTime
import java.time.YearMonth

enum class LotStatus { PENDING, APPROVED, REJECTED }

/** Where the lot record comes from. */
enum class LotSource { RECEPTION, FIELD_INSPECTION, VOICE_INCIDENT }

/** Why a lot is rejected (US10). [OTHER] needs a written note. */
enum class RejectionKind { BRUISED, OUT_OF_TEMPERATURE, UNDERSIZE, DECAY, OTHER }

data class RejectionReason(val kind: RejectionKind, val note: String = "")

/**
 * A lot waiting for (or with) the distributor's quality decision. Names and products are the
 * same ones used by Orders and Fleet.
 */
data class QualityLot(
    val id: String,
    val lotCode: String,
    val orderId: String?,
    val productName: String,
    val producerName: String,
    val celsius: Double?,
    val humidityPercent: Double?,
    val status: LotStatus,
    val source: LotSource,
    val recordedAt: LocalDateTime,
    val rejection: RejectionReason? = null,
    /** Saved offline in the field and not sent yet (US53). */
    val pendingSync: Boolean = false,
    /** Voice note attached to an incident (US57). */
    val voiceNoteSeconds: Int? = null,
    val incidentDescription: String? = null
)

enum class ShrinkageStage { HARVEST, TRANSPORT, RECEPTION }

enum class ShrinkageCause { THERMAL_EXCURSION, MECHANICAL_DAMAGE, UNDERSIZE, ROUTE_DELAY }

/** Fruit lost in a month, by stage and cause (US73). */
data class ShrinkageRecord(
    val id: String,
    val month: YearMonth,
    val stage: ShrinkageStage,
    val cause: ShrinkageCause,
    val productName: String,
    val lostKg: Int,
    val costPen: Double
)

data class ShrinkageSummary(val totalKg: Int, val totalCost: Double, val records: List<ShrinkageRecord>) {
    val isEmpty: Boolean get() = records.isEmpty()
}
