package com.rebootech.fruitlogix.producer.qualityControl.harvests.data

import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.PlannedHarvest
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.repository.HarvestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeHarvestRepository : HarvestRepository {
    private val _upcoming = MutableStateFlow<PlannedHarvest?>(
        PlannedHarvest(
            id = "h-01",
            productName = "Blueberries",
            dateFormatted = "Nov 5",
            expectedKg = 80,
            crewName = "Harvest crew scheduled for Sector 2",
            notes = "Sector 2",
            statusText = "Planned"
        )
    )

    override fun observeUpcomingHarvest(): Flow<PlannedHarvest?> = _upcoming.asStateFlow()

    companion object {
        val instance: FakeHarvestRepository by lazy { FakeHarvestRepository() }
    }
}

