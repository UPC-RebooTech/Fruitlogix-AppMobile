package com.rebootech.fruitlogix.distributor.qualityControl.data

import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.LotSource
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.LotStatus
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.QualityLot
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.RejectionReason
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.ShrinkageCause
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.ShrinkageRecord
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.ShrinkageStage
import com.rebootech.fruitlogix.distributor.qualityControl.domain.repository.QualityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.time.YearMonth

/** Lots of the same orders and producers as Orders and Fleet (e.g. FL-102's grapes at the gate). */
class FakeQualityRepository : QualityRepository {

    private var nextId = 100

    private val _lots = MutableStateFlow(initialLots())
    override val lots: StateFlow<List<QualityLot>> = _lots.asStateFlow()

    private val thisMonth = YearMonth.now()

    override val shrinkage: List<ShrinkageRecord> = listOf(
        ShrinkageRecord("m1", thisMonth, ShrinkageStage.TRANSPORT, ShrinkageCause.THERMAL_EXCURSION, "Arándano", 45, 810.0),
        ShrinkageRecord("m2", thisMonth, ShrinkageStage.RECEPTION, ShrinkageCause.MECHANICAL_DAMAGE, "Palta Hass", 62, 558.0),
        ShrinkageRecord("m3", thisMonth, ShrinkageStage.HARVEST, ShrinkageCause.UNDERSIZE, "Mango Kent", 30, 210.0),
        ShrinkageRecord("m4", thisMonth.minusMonths(1), ShrinkageStage.TRANSPORT, ShrinkageCause.ROUTE_DELAY, "Uva Red Globe", 28, 336.0)
    )

    // Two months ago has no shrinkage on purpose (US73, scenario 2)
    override fun shrinkageMonths(): List<YearMonth> = listOf(thisMonth, thisMonth.minusMonths(1), thisMonth.minusMonths(2))

    override suspend fun approve(lotId: String) = change(lotId) { it.copy(status = LotStatus.APPROVED, rejection = null) }

    override suspend fun reject(lotId: String, reason: RejectionReason) =
        change(lotId) { it.copy(status = LotStatus.REJECTED, rejection = reason.copy(note = reason.note.trim())) }

    private fun change(id: String, block: (QualityLot) -> QualityLot) =
        _lots.update { list -> list.map { if (it.id == id) block(it) else it } }

    override suspend fun addFieldInspection(lotCode: String, productName: String, celsius: Double, humidityPercent: Double) {
        add(
            QualityLot(
                id = "q${nextId++}", lotCode = lotCode.trim().uppercase(), orderId = null, productName = productName.trim(),
                producerName = "", celsius = celsius, humidityPercent = humidityPercent, status = LotStatus.PENDING,
                source = LotSource.FIELD_INSPECTION, recordedAt = LocalDateTime.now(),
                // TODO(sync): send when there is connection and clear the flag
                pendingSync = true
            )
        )
    }

    override suspend fun addVoiceIncident(lotCode: String, description: String, voiceNoteSeconds: Int) {
        add(
            QualityLot(
                id = "q${nextId++}", lotCode = lotCode.trim().uppercase(), orderId = null, productName = "",
                producerName = "", celsius = null, humidityPercent = null, status = LotStatus.PENDING,
                source = LotSource.VOICE_INCIDENT, recordedAt = LocalDateTime.now(),
                voiceNoteSeconds = voiceNoteSeconds, incidentDescription = description.trim()
            )
        )
    }

    private fun add(lot: QualityLot) = _lots.update { listOf(lot) + it }

    private companion object {
        fun initialLots(): List<QualityLot> {
            val now = LocalDateTime.now()
            return listOf(
                QualityLot("q1", "LOT-FX-1041-UVA", "FX-1041", "Uva Red Globe", "Viñedos de Ica", 0.8, 92.0,
                    LotStatus.PENDING, LotSource.RECEPTION, now.minusMinutes(5)),
                QualityLot("q2", "LOT-FX-1042-PALTA", "FX-1042", "Palta Hass", "Fundo Santa Rosa", 6.4, 88.0,
                    LotStatus.PENDING, LotSource.RECEPTION, now.minusMinutes(40)),
                QualityLot("q3", "LOT-FX-1038-MANGO", "FX-1038", "Mango Kent", "Campos del Norte", 13.4, 86.0,
                    LotStatus.PENDING, LotSource.RECEPTION, now.minusHours(2)),
                QualityLot("q4", "LOT-FX-1035-UVA", "FX-1035", "Uva Red Globe", "Finca Los Andes", 0.9, 93.0,
                    LotStatus.APPROVED, LotSource.RECEPTION, now.minusDays(3))
            )
        }
    }
}
