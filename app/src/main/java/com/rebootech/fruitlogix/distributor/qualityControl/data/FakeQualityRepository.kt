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
import com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus
import com.rebootech.fruitlogix.shared.core.network.CreateQualityInspectionRequestDto
import com.rebootech.fruitlogix.shared.core.network.QualityChecklistDto
import com.rebootech.fruitlogix.shared.core.network.QualityInspectionResponseDto
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import com.rebootech.fruitlogix.shared.core.network.TechnicalParametersDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.concurrent.ConcurrentHashMap

/**
 * Control de Calidad del Distribuidor conectado a:
 * - POST /api/v1/quality-inspections (con batchId numérico real, notas, inspección visual, parámetros técnicos y checklist)
 * - GET /api/v1/quality-inspections/batch/{batchId}
 *
 * NOTA TÉCNICA DE BACKEND:
 * El backend no expone un GET /api/v1/quality-inspections general sin batchId ni un PUT para editar inspecciones,
 * ni subida de archivos de voz (US57), ni un endpoint para mermas (US73). Por ello, refresh() consulta por los
 * batchId conocidos y las operaciones mantienen `pendingSync = true` si el servidor devuelve error.
 */
class FakeQualityRepository : QualityRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var nextId = 100

    // Mapeo entre el código de lote ("LOT-FX-1041-UVA") y el batchId numérico real de la BD
    private val batchIdByLotCode = ConcurrentHashMap<String, Int>(
        mapOf(
            "LOT-FX-1041-UVA" to 1,
            "LOT-FX-1042-PALTA" to 2,
            "LOT-FX-1038-MANGO" to 3,
            "LOT-FX-1035-UVA" to 4
        )
    )

    private val _lots = MutableStateFlow(initialLots())
    override val lots: StateFlow<List<QualityLot>> = _lots.asStateFlow()

    private val thisMonth = YearMonth.now()

    init {
        scope.launch { refresh() }
    }

    override val shrinkage: List<ShrinkageRecord> = listOf(
        ShrinkageRecord("m1", thisMonth, ShrinkageStage.TRANSPORT, ShrinkageCause.THERMAL_EXCURSION, "Arándano", 45, 810.0),
        ShrinkageRecord("m2", thisMonth, ShrinkageStage.RECEPTION, ShrinkageCause.MECHANICAL_DAMAGE, "Palta Hass", 62, 558.0),
        ShrinkageRecord("m3", thisMonth, ShrinkageStage.HARVEST, ShrinkageCause.UNDERSIZE, "Mango Kent", 30, 210.0),
        ShrinkageRecord("m4", thisMonth.minusMonths(1), ShrinkageStage.TRANSPORT, ShrinkageCause.ROUTE_DELAY, "Uva Red Globe", 28, 336.0)
    )

    override fun shrinkageMonths(): List<YearMonth> = listOf(thisMonth, thisMonth.minusMonths(1), thisMonth.minusMonths(2))

    override suspend fun approve(lotId: String) {
        val current = _lots.value.firstOrNull { it.id == lotId } ?: return
        change(lotId) { it.copy(status = LotStatus.APPROVED, rejection = null, pendingSync = true) }

        val synced = postInspectionToBackend(
            lotCode = current.lotCode,
            visualInspection = "APPROVED",
            celsius = current.celsius ?: 5.0,
            humidity = current.humidityPercent ?: 88.0,
            checklistPassed = true,
            notes = "Lote ${current.lotCode} (${current.productName}) aprobado en recepción"
        )
        if (synced) {
            change(lotId) { it.copy(pendingSync = false) }
        }
    }

    override suspend fun reject(lotId: String, reason: RejectionReason) {
        val current = _lots.value.firstOrNull { it.id == lotId } ?: return
        val cleanReason = reason.copy(note = reason.note.trim())
        change(lotId) { it.copy(status = LotStatus.REJECTED, rejection = cleanReason, pendingSync = true) }

        val synced = postInspectionToBackend(
            lotCode = current.lotCode,
            visualInspection = "REJECTED",
            celsius = current.celsius ?: 12.0,
            humidity = current.humidityPercent ?: 85.0,
            checklistPassed = false,
            notes = "Rechazado [${cleanReason.kind.name}]: ${cleanReason.note}"
        )
        if (synced) {
            change(lotId) { it.copy(pendingSync = false) }
        }
    }

    private fun change(id: String, block: (QualityLot) -> QualityLot) =
        _lots.update { list -> list.map { if (it.id == id) block(it) else it } }

    override suspend fun addFieldInspection(lotCode: String, productName: String, celsius: Double, humidityPercent: Double) {
        val normalizedCode = lotCode.trim().uppercase()
        val newLot = QualityLot(
            id = "q${nextId++}",
            lotCode = normalizedCode,
            orderId = null,
            productName = productName.trim(),
            producerName = "Inspección en Campo",
            celsius = celsius,
            humidityPercent = humidityPercent,
            status = LotStatus.PENDING,
            source = LotSource.FIELD_INSPECTION,
            recordedAt = LocalDateTime.now(),
            pendingSync = true
        )
        add(newLot)

        val synced = postInspectionToBackend(
            lotCode = normalizedCode,
            visualInspection = "OBSERVED",
            celsius = celsius,
            humidity = humidityPercent,
            checklistPassed = celsius in -1.0..15.0,
            notes = "Inspección de campo para ${productName.trim()} ($normalizedCode)"
        )
        if (synced) {
            change(newLot.id) { it.copy(pendingSync = false) }
        }
    }

    override suspend fun addVoiceIncident(lotCode: String, description: String, voiceNoteSeconds: Int) {
        val normalizedCode = lotCode.trim().uppercase()
        val newLot = QualityLot(
            id = "q${nextId++}",
            lotCode = normalizedCode,
            orderId = null,
            productName = "Incidencia por Voz",
            producerName = "Audio (${voiceNoteSeconds}s)",
            celsius = null,
            humidityPercent = null,
            status = LotStatus.PENDING,
            source = LotSource.VOICE_INCIDENT,
            recordedAt = LocalDateTime.now(),
            pendingSync = true,
            voiceNoteSeconds = voiceNoteSeconds,
            incidentDescription = description.trim()
        )
        add(newLot)

        val synced = postInspectionToBackend(
            lotCode = normalizedCode,
            visualInspection = "OBSERVED",
            celsius = 0.0,
            humidity = 0.0,
            checklistPassed = false,
            notes = "Incidencia por voz (${voiceNoteSeconds}s): ${description.trim()}"
        )
        if (synced) {
            change(newLot.id) { it.copy(pendingSync = false) }
        }
    }

    override suspend fun refresh() {
        withContext(Dispatchers.IO) {
            var anySuccess = false
            for ((lotCode, batchId) in batchIdByLotCode.entries) {
                val result = runCatching { RetrofitClient.api.getQualityInspectionsByBatch(batchId) }
                result.onSuccess { response ->
                    if (response.isSuccessful) {
                        anySuccess = true
                        val inspections = response.body().orEmpty()
                        inspections.lastOrNull()?.let { latest ->
                            applyRemoteInspectionToLot(lotCode, batchId, latest)
                        }
                    }
                }.onFailure { err ->
                    Log.w("FruitLogix-API", "GET /quality-inspections/batch/$batchId falló: ${err.message}")
                }
            }
            if (anySuccess) {
                RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
            }
        }
    }

    private suspend fun postInspectionToBackend(
        lotCode: String,
        visualInspection: String,
        celsius: Double,
        humidity: Double,
        checklistPassed: Boolean,
        notes: String
    ): Boolean = withContext(Dispatchers.IO) {
        val realBatchId = resolveBatchId(lotCode)
        val dto = CreateQualityInspectionRequestDto(
            batchId = realBatchId,
            notes = notes,
            visualInspection = visualInspection,
            technicalParameters = TechnicalParametersDto(
                temperatureCelsius = celsius,
                humidityPercent = humidity
            ),
            checklist = QualityChecklistDto(
                packagingIntact = checklistPassed,
                labelingCorrect = true,
                pestFree = true,
                coldChainMaintained = checklistPassed
            )
        )

        val result = runCatching { RetrofitClient.api.createQualityInspection(dto) }
        result.fold(
            onSuccess = { response ->
                if (response.isSuccessful) {
                    RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
                    true
                } else {
                    Log.w("FruitLogix-API", "POST /quality-inspections rechazado (HTTP ${response.code()}); se conserva pendingSync=true")
                    false
                }
            },
            onFailure = { err ->
                Log.w("FruitLogix-API", "POST /quality-inspections falló (${err.message}); se conserva pendingSync=true")
                false
            }
        )
    }

    private fun applyRemoteInspectionToLot(
        lotCode: String,
        batchId: Int,
        dto: QualityInspectionResponseDto
    ) {
        val statusString = (dto.visualInspection ?: dto.status).orEmpty().uppercase()
        val mappedStatus = when {
            statusString.contains("APPROV") -> LotStatus.APPROVED
            statusString.contains("REJECT") -> LotStatus.REJECTED
            else -> LotStatus.PENDING
        }
        val temp = dto.technicalParameters?.temperatureCelsius ?: dto.temperatureCelsius
        val hum = dto.technicalParameters?.humidityPercent ?: dto.humidityPercent

        _lots.update { currentList ->
            val exists = currentList.any { it.lotCode.equals(lotCode, ignoreCase = true) }
            if (exists) {
                currentList.map { lot ->
                    if (lot.lotCode.equals(lotCode, ignoreCase = true)) {
                        lot.copy(
                            celsius = temp ?: lot.celsius,
                            humidityPercent = hum ?: lot.humidityPercent,
                            status = mappedStatus,
                            pendingSync = false,
                            rejection = if (mappedStatus == LotStatus.REJECTED) {
                                lot.rejection ?: RejectionReason(RejectionKind.OTHER, dto.notes.orEmpty())
                            } else null
                        )
                    } else lot
                }
            } else {
                listOf(
                    QualityLot(
                        id = "q-api-${dto.id ?: batchId}",
                        lotCode = lotCode,
                        orderId = null,
                        productName = "Lote #$batchId",
                        producerName = dto.notes ?: "Sincronizado con Render",
                        celsius = temp,
                        humidityPercent = hum,
                        status = mappedStatus,
                        source = LotSource.RECEPTION,
                        recordedAt = LocalDateTime.now().minusMinutes(5),
                        pendingSync = false
                    )
                ) + currentList
            }
        }
    }

    private fun resolveBatchId(lotCode: String): Int {
        batchIdByLotCode[lotCode]?.let { return it }
        val digits = Regex("\\d+").find(lotCode)?.value?.toIntOrNull()
        val resolved = if (digits != null && digits > 0) (digits % 10) + 1 else 1
        batchIdByLotCode[lotCode] = resolved
        return resolved
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