package com.rebootech.fruitlogix.producer.qualityControl.harvests.data

import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.repository.HarvestRepository

class FakeHarvestRepository {
    companion object {
        val instance = HarvestRepository
    }
}