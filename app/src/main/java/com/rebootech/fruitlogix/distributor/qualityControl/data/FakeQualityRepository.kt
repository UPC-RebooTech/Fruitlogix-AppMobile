package com.rebootech.fruitlogix.distributor.qualityControl.data

import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.LotSource
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.LotStatus
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.QualityLot
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.RejectionReason
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.ShrinkageCause
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.ShrinkageRecord
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.ShrinkageStage
import com.rebootech.fruitlogix.distributor.qualityControl.domain.repository.QualityRepository
import com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus
import com.rebootech.fruitlogix.shared.core.network.QualityInspectionDto
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
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

class FakeQualityRepository : QualityRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var nextId = 100

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
        var target: QualityLot? = null
        change(lotId) { it.copy(status = LotStatus.APPROVED, rejection = null, pendingSync = false).also { l -> target = l } }
        target?.let { lot ->
            scope.launch {
                runCatching {
                    RetrofitClient.api.createQualityInspection(
                        QualityInspectionDto(
                            batchId = lot.lotCode,
                            productName = lot.productName,
                            temperature = lot.celsius ?: 5.0,
                            humidity = lot.humidityPercent ?: 88.0,
                            status = "APPROVED",
                            comments = "Lote aprobado por distribuidor"
                        )
                    )
                }
            }
        }
    }

    override suspend fun reject(lotId: String, reason: RejectionReason) {
        var target: QualityLot? = null
        change(lotId) {
            it.copy(status = LotStatus.REJECTED, rejection = reason.copy(note = reason.note.trim()), pendingSync = false)
                .also { l -> target = l }
        }
        target?.let { lot ->
            scope.launch {
                runCatching {
                    RetrofitClient.api.createQualityInspection(
                        QualityInspectionDto(
                            batchId = lot.lotCode,
                            productName = lot.productName,
                            temperature = lot.celsius ?: 12.0,
                            humidity = lot.humidityPercent ?: 85.0,
                            status = "REJECTED",
                            comments = "${reason.kind.name}: ${reason.note}"
                        )
                    )
                }
            }
        }
    }

    private fun change(id: String, block: (QualityLot) -> QualityLot) =
        _lots.update { list -> list.map { if (it.id == id) block(it) else it } }

    override suspend fun addFieldInspection(lotCode: String, productName: String, celsius: Double, humidityPercent: Double) {
        val newLot = QualityLot(
            id = "q${nextId++}", lotCode = lotCode.trim().uppercase(), orderId = null, productName = productName.trim(),
            producerName = "Inspección en Campo", celsius = celsius, humidityPercent = humidityPercent, status = LotStatus.PENDING,
            source = LotSource.FIELD_INSPECTION, recordedAt = LocalDateTime.now(),
            pendingSync = true
        )
        add(newLot)
        scope.launch {
            runCatching {
                RetrofitClient.api.createQualityInspection(
                    QualityInspectionDto(
                        batchId = newLot.lotCode,
                        productName = newLot.productName,
                        temperature = celsius,
                        humidity = humidityPercent,
                        status = "PENDING",
                        comments = "Inspección de campo registrada desde app móvil"
                    )
                )
            }.onSuccess {
                change(newLot.id) { it.copy(pendingSync = false) }
            }
        }
    }

    override suspend fun addVoiceIncident(lotCode: String, description: String, voiceNoteSeconds: Int) {
        val newLot = QualityLot(
            id = "q${nextId++}", lotCode = lotCode.trim().uppercase(), orderId = null, productName = "Incidencia por Voz",
            producerName = "Audio (${voiceNoteSeconds}s)", celsius = null, humidityPercent = null, status = LotStatus.PENDING,
            source = LotSource.VOICE_INCIDENT, recordedAt = LocalDateTime.now(),
            voiceNoteSeconds = voiceNoteSeconds, incidentDescription = description.trim()
        )
        add(newLot)
        scope.launch {
            runCatching {
                RetrofitClient.api.createQualityInspection(
                    QualityInspectionDto(
                        batchId = newLot.lotCode,
                        productName = "Incidencia Voz (${voiceNoteSeconds}s)",
                        temperature = 0.0,
                        humidity = 0.0,
                        status = "INCIDENT",
                        comments = description.trim()
                    )
                )
            }
        }
    }

    override suspend fun refresh() {
        val remote = withContext(Dispatchers.IO) { runCatching { RetrofitClient.api.getQualityInspections() } }
        remote.onSuccess { dtos ->
            RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
            val mapped = dtos.mapIndexedNotNull { index, dto ->
                val code = dto.batchId?.takeIf { it.isNotBlank() } ?: return@mapIndexedNotNull null
                QualityLot(
                    id = "q-api-${dto.id ?: index}",
                    lotCode = code,
                    orderId = null,
                    productName = dto.productName?.takeIf { it.isNotBlank() } ?: "Palta Hass",
                    producerName = dto.comments?.takeIf { it.isNotBlank() } ?: "Sincronizado con Render",
                    celsius = dto.temperature,
                    humidityPercent = dto.humidity,
                    status = when (dto.status?.uppercase()) {
                        "APPROVED" -> LotStatus.APPROVED
                        "REJECTED" -> LotStatus.REJECTED
                        else -> LotStatus.PENDING
                    },
                    source = LotSource.RECEPTION,
                    recordedAt = LocalDateTime.now().minusMinutes(10)
                )
            }
            if (mapped.isNotEmpty()) {
                _lots.update { (mapped + it).distinctBy { lot -> lot.lotCode } }
            }
        }.onFailure {
            RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK)
        }
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