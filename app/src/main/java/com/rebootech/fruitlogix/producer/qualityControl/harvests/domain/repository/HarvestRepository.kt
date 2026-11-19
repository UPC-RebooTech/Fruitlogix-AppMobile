package com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.repository

import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.HarvestForecast
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.PlannedHarvest
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.ScheduledHarvest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

interface HarvestRepository {
    val recentHarvests: StateFlow<List<PlannedHarvest>>
    val scheduledHarvests: StateFlow<List<ScheduledHarvest>>
    val weeklyForecast: HarvestForecast

    /** Storage places on the farm where a harvest can be logged (names are data). */
    val storageSectors: List<String>

    // Requerido por FakeDashboardRepository para el Hero Card del Home
    fun observeUpcomingHarvest(): Flow<PlannedHarvest?>

    suspend fun addHarvest(
        fruitName: String,
        lotCode: String,
        weightKg: Int,
        harvestedAt: LocalDateTime,
        sector: String,
        brix: Double? = null,
        notes: String? = null
    )
}
