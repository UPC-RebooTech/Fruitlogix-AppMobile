package com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.repository

import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.PlannedHarvest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface HarvestRepository {
    val recentHarvests: StateFlow<List<PlannedHarvest>>

    // Requerido por FakeDashboardRepository para el Hero Card del Home
    fun observeUpcomingHarvest(): Flow<PlannedHarvest?>

    suspend fun addHarvest(
        fruitName: String,
        lotCode: String,
        weightKg: String,
        dateTime: String,
        sector: String = "Galpón 1"
    )
}