package com.rebootech.fruitlogix.producer.qualityControl.harvests.data

import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.HarvestForecast
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.HarvestStatus
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.PlannedHarvest
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.ScheduledHarvest
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.repository.HarvestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import java.util.UUID

class FakeHarvestRepository : HarvestRepository {

    private val today: LocalDate = LocalDate.now()

    private val _recentHarvests = MutableStateFlow(
        listOf(
            PlannedHarvest(
                id = "1",
                fruitName = "Mango Kent",
                harvestedAt = today.atTime(LocalTime.of(6, 30)),
                weightKg = 3200,
                lotCode = "L-MNG-01",
                crewOrTransport = "Cuadrilla A • Fundo",
                sectorOrVault = "Galpón 1",
                status = HarvestStatus.READY
            ),
            PlannedHarvest(
                id = "2",
                fruitName = "Arándano Ventura",
                harvestedAt = today.minusDays(1).atTime(LocalTime.of(16, 15)),
                weightKg = 1500,
                lotCode = "L-ARA-03",
                crewOrTransport = "Cuadrilla B • Fundo",
                sectorOrVault = "Cámara Frío 2",
                status = HarvestStatus.RIPENING
            ),
            PlannedHarvest(
                id = "3",
                fruitName = "Palta Hass",
                harvestedAt = today.minusDays(6).atTime(LocalTime.of(8, 0)),
                weightKg = 4800,
                lotCode = "L-PLT-02",
                crewOrTransport = "Cuadrilla A • Fundo",
                sectorOrVault = "Galpón 2",
                status = HarvestStatus.SCHEDULED
            )
        )
    )

    override val recentHarvests: StateFlow<List<PlannedHarvest>> = _recentHarvests.asStateFlow()

    override val scheduledHarvests: StateFlow<List<ScheduledHarvest>> = MutableStateFlow(
        listOf(
            ScheduledHarvest(
                id = "s1",
                productName = "Blueberries (Biloxi)",
                scheduledFor = today.plusDays(2).atTime(LocalTime.of(7, 0)),
                estimatedYieldKg = 80,
                zone = "Block 4B Staging",
                crewName = "Crew Beta",
                pickers = 8
            ),
            ScheduledHarvest(
                id = "s2",
                productName = "Kent Mango",
                scheduledFor = today.plusDays(4).atTime(LocalTime.of(6, 30)),
                estimatedYieldKg = 260,
                zone = "Lote Norte 2",
                crewName = "Cuadrilla A",
                pickers = 12
            )
        )
    ).asStateFlow()

    override val weeklyForecast: HarvestForecast = HarvestForecast(
        weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
        productName = "Kent Mango",
        requestedKg = 900,
        fulfilledKg = 640,
        clients = listOf("Supermercados Wong", "Cencosud Mkt")
    )

    override fun observeUpcomingHarvest(): Flow<PlannedHarvest?> {
        return _recentHarvests.map { list -> list.firstOrNull() }
    }

    override val storageSectors: List<String> = listOf("Galpón 1", "Galpón 2", "Cámara Frío 2")

    override suspend fun addHarvest(
        fruitName: String,
        lotCode: String,
        weightKg: Int,
        harvestedAt: LocalDateTime,
        sector: String,
        brix: Double?,
        notes: String?
    ) {
        val newHarvest = PlannedHarvest(
            id = UUID.randomUUID().toString(),
            fruitName = fruitName,
            harvestedAt = harvestedAt,
            weightKg = weightKg,
            lotCode = lotCode,
            sectorOrVault = sector,
            status = HarvestStatus.REGISTERED,
            brix = brix,
            notes = notes
        )
        _recentHarvests.update { listOf(newHarvest) + it }
    }

    companion object {
        val instance: HarvestRepository = FakeHarvestRepository()
    }
}
