package com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.repository

import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.PlannedHarvest
import kotlinx.coroutines.flow.Flow

interface HarvestRepository {
    fun observeUpcomingHarvest(): Flow<PlannedHarvest?>
}

