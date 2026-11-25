package com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.repository

import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.HarvestForecast
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.HarvestPlanDraft
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.HarvestSaveResult
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.PlannedHarvest
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.ScheduledHarvest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

interface HarvestRepository {
    val recentHarvests: StateFlow<List<PlannedHarvest>>
    val scheduledHarvests: StateFlow<List<ScheduledHarvest>>
    /** null when there are no orders due this week. */
    val weeklyForecast: HarvestForecast?

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
    ): HarvestSaveResult

    /** Plans a harvest on the server (POST /harvest-plans). */
    suspend fun schedule(draft: HarvestPlanDraft): HarvestSaveResult = HarvestSaveResult(false)

    /** Removes a planned harvest (DELETE /harvest-plans/{id}). */
    suspend fun cancelPlan(id: String): HarvestSaveResult = HarvestSaveResult(false)

    /** Reads the producer's own harvest batches and planned harvests from the server again. */
    suspend fun refresh() {}
}
