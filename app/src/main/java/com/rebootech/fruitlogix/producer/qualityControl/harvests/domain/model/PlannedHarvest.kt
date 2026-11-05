package com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model

data class PlannedHarvest(
    val id: String,
    val productName: String,
    val dateFormatted: String,
    val expectedKg: Int,
    val crewName: String? = null,
    val notes: String? = null,
    val statusText: String = "Planned"
)

