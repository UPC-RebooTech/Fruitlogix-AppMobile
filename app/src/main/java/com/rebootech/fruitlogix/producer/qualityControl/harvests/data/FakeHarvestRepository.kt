package com.rebootech.fruitlogix.producer.qualityControl.harvests.data

import com.rebootech.fruitlogix.producer.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.producer.profilesManagement.data.FakeProfileRepository
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.HarvestForecast
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.HarvestSaveResult
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.HarvestStatus
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.PlannedHarvest
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.ScheduledHarvest
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.repository.HarvestRepository
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.rules.HarvestForecastRules
import com.rebootech.fruitlogix.shared.core.network.ApiResult
import com.rebootech.fruitlogix.shared.core.network.CreateHarvestBatchRequestDto
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import com.rebootech.fruitlogix.shared.core.network.apiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.math.roundToInt

class FakeHarvestRepository : HarvestRepository {

    private val _recentHarvests = MutableStateFlow<List<PlannedHarvest>>(emptyList())

    override val recentHarvests: StateFlow<List<PlannedHarvest>> = _recentHarvests.asStateFlow()

    /** Planned harvests have no server endpoint yet: the list starts empty (no sample data). */
    override val scheduledHarvests: StateFlow<List<ScheduledHarvest>> =
        MutableStateFlow<List<ScheduledHarvest>>(emptyList()).asStateFlow()

    /** This week's demand from the producer's server orders (due Monday to Sunday). */
    override val weeklyForecast: HarvestForecast?
        get() = HarvestForecastRules.forWeek(FakeOrderRepository.instance.orderSnapshot(), LocalDate.now())

    override fun observeUpcomingHarvest(): Flow<PlannedHarvest?> {
        return _recentHarvests.map { list -> list.firstOrNull() }
    }

    /** The farm's storage places have no server endpoint: none are offered (the field is optional). */
    override val storageSectors: List<String> = emptyList()

    override suspend fun addHarvest(
        fruitName: String,
        lotCode: String,
        weightKg: Int,
        harvestedAt: LocalDateTime,
        sector: String,
        brix: Double?,
        notes: String?
    ): HarvestSaveResult {
        // The harvest is a batch on the server; the server takes the producer from the account
        val result = withContext(Dispatchers.IO) {
            apiCall {
                RetrofitClient.api.createHarvestBatch(
                    CreateHarvestBatchRequestDto(
                        producerId = FakeProfileRepository.instance.producerId.value ?: 0,
                        fruitType = fruitName,
                        quantityKg = weightKg.toDouble(),
                        harvestDate = harvestedAt.toLocalDate().toString()
                    )
                )
            }
        }
        // No signal: kept on this phone only. Refused (e.g. 409 without a producer profile): shown to the producer.
        if (result is ApiResult.Failed && !result.failure.isNetwork) {
            return HarvestSaveResult(false, result.failure.serverMessage)
        }
        val batch = (result as? ApiResult.Ok)?.value
        val newHarvest = PlannedHarvest(
            id = batch?.let { "batch-${it.id}" } ?: UUID.randomUUID().toString(),
            fruitName = fruitName,
            harvestedAt = harvestedAt,
            weightKg = weightKg,
            // A server batch is known by its number (the inspection and the distributor use it)
            lotCode = batch?.let { "LOT-${it.id}" } ?: lotCode,
            sectorOrVault = sector,
            status = HarvestStatus.REGISTERED,
            brix = brix,
            notes = notes,
            remoteId = batch?.id
        )
        _recentHarvests.update { listOf(newHarvest) + it }
        return HarvestSaveResult(true)
    }

    /**
     * GET /harvest-batches is not filtered by the server: only the batches of this producer's
     * profile are kept. Approved batches are ready; the rest are registered.
     */
    override suspend fun refresh() {
        val producerId = FakeProfileRepository.instance.producerId.value ?: return
        val result = withContext(Dispatchers.IO) { apiCall { RetrofitClient.api.getHarvestBatches() } }
        if (result !is ApiResult.Ok) return
        val previous = _recentHarvests.value.filter { it.remoteId != null }.associateBy { it.remoteId }
        val fromServer = result.value
            .filter { it.producerId == producerId }
            .sortedByDescending { it.id }
            .map { dto ->
                val old = previous[dto.id]
                PlannedHarvest(
                    id = "batch-${dto.id}",
                    fruitName = dto.fruitType.orEmpty(),
                    harvestedAt = old?.harvestedAt
                        ?: dto.harvestDate?.take(10)?.let { runCatching { LocalDate.parse(it).atStartOfDay() }.getOrNull() }
                        ?: LocalDateTime.now(),
                    weightKg = (dto.quantityKg ?: 0.0).roundToInt(),
                    lotCode = "LOT-${dto.id}",
                    sectorOrVault = old?.sectorOrVault.orEmpty(),
                    status = if (dto.status.equals("Approved", true)) HarvestStatus.READY else HarvestStatus.REGISTERED,
                    brix = old?.brix,
                    notes = old?.notes,
                    remoteId = dto.id
                )
            }
        _recentHarvests.update { list -> fromServer + list.filter { it.remoteId == null } }
    }

    companion object {
        val instance: HarvestRepository = FakeHarvestRepository()
    }
}
