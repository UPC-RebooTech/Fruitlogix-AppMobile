package com.rebootech.fruitlogix.producer.qualityControl.harvests.data

import com.rebootech.fruitlogix.producer.qualityControl.harvests.data.remote.CreateHarvestBatchRequest
import com.rebootech.fruitlogix.producer.qualityControl.harvests.data.remote.HarvestApiService
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.PlannedHarvest
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.repository.HarvestRepository
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class RemoteHarvestRepository(
    private val apiService: HarvestApiService = RetrofitClient.createService()
) : HarvestRepository {

    private val _recentHarvests = MutableStateFlow<List<PlannedHarvest>>(emptyList())
    override val recentHarvests: StateFlow<List<PlannedHarvest>> = _recentHarvests.asStateFlow()

    init {
        loadHarvestBatches()
    }

    fun loadHarvestBatches() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getHarvestBatches()
                if (response.isSuccessful && response.body() != null) {
                    val remoteBatches = response.body()!!.map { dto ->
                        val kg = dto.quantityKg?.toInt() ?: 0
                        PlannedHarvest(
                            id = dto.id ?: UUID.randomUUID().toString(),
                            fruitName = dto.fruitType ?: "Fruta General",
                            dateTime = dto.harvestDate?.take(10) ?: LocalDate.now().toString(),
                            weight = "$kg kg",
                            lotCode = "L-PROD-${dto.producerId ?: 1}",
                            crewOrTransport = "Cuadrilla Fundo Central",
                            sectorOrVault = "Galpón 1",
                            statusText = dto.status ?: "Pending"
                        )
                    }
                    _recentHarvests.update { remoteBatches }
                } else {
                    applyDefaultFallbackData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                applyDefaultFallbackData()
            }
        }
    }

    override fun observeUpcomingHarvest(): Flow<PlannedHarvest?> {
        return _recentHarvests.map { list -> list.firstOrNull() }
    }

    override suspend fun addHarvest(
        fruitName: String,
        lotCode: String,
        weightKg: String,
        dateTime: String,
        sector: String
    ) {
        val parsedWeight = weightKg.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: 0.0
        val currentDateIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        val request = CreateHarvestBatchRequest(
            producerId = 1L,
            fruitType = fruitName,
            quantityKg = parsedWeight,
            harvestDate = currentDateIso
        )

        try {
            val response = apiService.createHarvestBatch(request)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val createdHarvest = PlannedHarvest(
                    id = dto.id ?: UUID.randomUUID().toString(),
                    fruitName = dto.fruitType ?: fruitName,
                    dateTime = dto.harvestDate ?: currentDateIso,
                    weight = "${(dto.quantityKg ?: parsedWeight).toInt()} kg",
                    lotCode = lotCode.ifBlank { "L-PROD-${dto.producerId ?: 1}" },
                    crewOrTransport = "Cuadrilla Fundo Central",
                    sectorOrVault = sector,
                    statusText = dto.status ?: "Pending"
                )
                // Inserta inmediatamente el registro creado sin depender del GET
                _recentHarvests.update { currentList -> listOf(createdHarvest) + currentList }
            } else {
                appendOptimisticHarvest(fruitName, lotCode, weightKg, dateTime, sector)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            appendOptimisticHarvest(fruitName, lotCode, weightKg, dateTime, sector)
        }
    }

    private fun appendOptimisticHarvest(
        fruitName: String,
        lotCode: String,
        weightKg: String,
        dateTime: String,
        sector: String
    ) {
        val fallbackHarvest = PlannedHarvest(
            id = UUID.randomUUID().toString(),
            fruitName = fruitName,
            dateTime = dateTime,
            weight = "$weightKg kg",
            lotCode = lotCode.ifBlank { "L-MOCK-01" },
            crewOrTransport = "Cuadrilla Fundo Central",
            sectorOrVault = sector,
            statusText = "Pending"
        )
        _recentHarvests.update { currentList -> listOf(fallbackHarvest) + currentList }
    }

    private fun applyDefaultFallbackData() {
        if (_recentHarvests.value.isEmpty()) {
            _recentHarvests.update {
                listOf(
                    PlannedHarvest(
                        id = "1",
                        fruitName = "Mango Kent",
                        dateTime = "Hoy, 06:30 AM",
                        weight = "3,200 kg",
                        lotCode = "L-MNG-01",
                        crewOrTransport = "Cuadrilla A • Fundo",
                        sectorOrVault = "Galpón 1",
                        statusText = "Listo para cosecha"
                    ),
                    PlannedHarvest(
                        id = "2",
                        fruitName = "Palta Hass",
                        dateTime = "Ayer, 04:15 PM",
                        weight = "4,800 kg",
                        lotCode = "L-PLT-02",
                        crewOrTransport = "Cuadrilla B • Fundo",
                        sectorOrVault = "Galpón 2",
                        statusText = "En maduración"
                    )
                )
            }
        }
    }

    companion object {
        val instance: HarvestRepository by lazy { RemoteHarvestRepository() }
    }
}