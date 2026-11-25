package com.rebootech.fruitlogix.distributor.qualityControl.data

import android.util.Log
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.LotSource
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.LotStatus
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.QualityLot
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.RejectionKind
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.RejectionReason
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.ShrinkageCause
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.ShrinkageRecord
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.ShrinkageStage
import com.rebootech.fruitlogix.distributor.qualityControl.domain.repository.QualityRepository
import com.rebootech.fruitlogix.shared.core.network.ApiResult
import com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus
import com.rebootech.fruitlogix.shared.core.network.CreateIncidentRequestDto
import com.rebootech.fruitlogix.shared.core.network.HarvestBatchDto
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import com.rebootech.fruitlogix.shared.core.network.UpdateHarvestBatchStatusRequestDto
import com.rebootech.fruitlogix.shared.core.network.apiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.Locale

/**
 * Distributor quality control.
 * - Server lots are the harvest batches (GET /harvest-batches), shown as "LOT-<id>". Approving or
 *   rejecting one changes its status on the server (PATCH /harvest-batches/{id}/status), and a voice
 *   incident on one creates an incident there (POST /incidents).
 * - The full inspection (ratings, pH, Brix…) is registered by the producer on their own lots; the
 *   distributor's quick field check (temperature and humidity) has no server endpoint, so it stays here.
 * - Shrinkage has no server endpoint yet, so it starts empty (no sample data).
 */
class FakeQualityRepository(
    /** Producer name for a server producer id (from the producer network). */
    private val producerName: (Int) -> String? = { null }
) : QualityRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var nextId = 100

    private val _lots = MutableStateFlow<List<QualityLot>>(emptyList())
    override val lots: StateFlow<List<QualityLot>> = _lots.asStateFlow()

    private val thisMonth = YearMonth.now()

    init {
        scope.launch { refresh() }
    }

    override val shrinkage: List<ShrinkageRecord> = emptyList()

    override fun shrinkageMonths(): List<YearMonth> = listOf(thisMonth, thisMonth.minusMonths(1), thisMonth.minusMonths(2))

    override suspend fun approve(lotId: String) {
        val current = _lots.value.firstOrNull { it.id == lotId } ?: return
        change(lotId) { it.copy(status = LotStatus.APPROVED, rejection = null, pendingSync = current.remoteId != null) }
        current.remoteId?.let { sendStatus(lotId, it, "Approved") }
    }

    override suspend fun reject(lotId: String, reason: RejectionReason) {
        val current = _lots.value.firstOrNull { it.id == lotId } ?: return
        val cleanReason = reason.copy(note = reason.note.trim())
        change(lotId) { it.copy(status = LotStatus.REJECTED, rejection = cleanReason, pendingSync = current.remoteId != null) }
        // The server keeps only the status; the reason stays on this phone
        current.remoteId?.let { sendStatus(lotId, it, "Rejected") }
    }

    private suspend fun sendStatus(lotId: String, batchId: Int, status: String) {
        val result = withContext(Dispatchers.IO) {
            apiCall { RetrofitClient.api.updateHarvestBatchStatus(batchId, UpdateHarvestBatchStatusRequestDto(status)) }
        }
        when (result) {
            is ApiResult.Ok -> change(lotId) { it.copy(pendingSync = false) }
            // Stays "pending sync" so the person sees it was not saved on the server
            is ApiResult.Failed -> Log.w(TAG, "PATCH harvest-batches/$batchId/status answered ${result.failure.code}")
        }
    }

    private fun change(id: String, block: (QualityLot) -> QualityLot) =
        _lots.update { list -> list.map { if (it.id == id) block(it) else it } }

    override suspend fun addFieldInspection(lotCode: String, productName: String, celsius: Double, humidityPercent: Double) {
        add(
            QualityLot(
                id = "q${nextId++}",
                lotCode = lotCode.trim().uppercase(),
                orderId = null,
                productName = productName.trim(),
                producerName = "",
                celsius = celsius,
                humidityPercent = humidityPercent,
                status = LotStatus.PENDING,
                source = LotSource.FIELD_INSPECTION,
                recordedAt = LocalDateTime.now()
            )
        )
    }

    override suspend fun addVoiceIncident(lotCode: String, description: String, voiceNoteSeconds: Int) {
        val normalizedCode = lotCode.trim().uppercase()
        val batchId = serverBatchId(normalizedCode)
        val newLot = QualityLot(
            id = "q${nextId++}",
            lotCode = normalizedCode,
            orderId = null,
            productName = _lots.value.firstOrNull { it.lotCode == normalizedCode }?.productName.orEmpty(),
            producerName = "",
            celsius = null,
            humidityPercent = null,
            status = LotStatus.PENDING,
            source = LotSource.VOICE_INCIDENT,
            recordedAt = LocalDateTime.now(),
            pendingSync = batchId != null,
            voiceNoteSeconds = voiceNoteSeconds,
            incidentDescription = description.trim()
        )
        add(newLot)
        if (batchId == null) return
        // The audio itself is not uploaded (the server stores images only); the written description is
        val result = withContext(Dispatchers.IO) {
            apiCall { RetrofitClient.api.createIncident(CreateIncidentRequestDto(batchId, description.trim())) }
        }
        if (result is ApiResult.Ok) change(newLot.id) { it.copy(pendingSync = false) }
    }

    override suspend fun refresh() {
        val result = withContext(Dispatchers.IO) { apiCall { RetrofitClient.api.getHarvestBatches() } }
        if (result !is ApiResult.Ok) {
            RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK, "GET /harvest-batches")
            return
        }
        RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
        val previous = _lots.value.filter { it.remoteId != null }.associateBy { it.remoteId }
        val fromServer = result.value.map { batch -> toLot(batch, previous[batch.id]) }
        // The server list replaces the previous server copy; field records of this phone stay
        _lots.update { list -> fromServer + list.filter { it.remoteId == null } }
    }

    private fun toLot(batch: HarvestBatchDto, previous: QualityLot?): QualityLot {
        val status = when (batch.status?.lowercase(Locale.ROOT)) {
            "approved" -> LotStatus.APPROVED
            "rejected" -> LotStatus.REJECTED
            else -> LotStatus.PENDING
        }
        val harvested = batch.harvestDate?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        return QualityLot(
            id = "q-b${batch.id}",
            lotCode = lotCodeFor(batch.id),
            orderId = null,
            productName = batch.fruitType.orEmpty(),
            producerName = batch.producerId?.let(producerName).orEmpty(),
            celsius = previous?.celsius,
            humidityPercent = previous?.humidityPercent,
            status = status,
            source = LotSource.RECEPTION,
            recordedAt = harvested?.atStartOfDay() ?: previous?.recordedAt ?: LocalDateTime.now(),
            rejection = if (status == LotStatus.REJECTED) previous?.rejection else null,
            remoteId = batch.id
        )
    }

    /** "LOT-12" → 12 when that batch came from the server. */
    private fun serverBatchId(lotCode: String): Int? =
        _lots.value.firstOrNull { it.lotCode.equals(lotCode, ignoreCase = true) && it.remoteId != null }?.remoteId

    private fun add(lot: QualityLot) = _lots.update { listOf(lot) + it }

    private companion object {
        const val TAG = "FruitLogix-Quality"

        fun lotCodeFor(batchId: Int) = "LOT-$batchId"
    }
}
