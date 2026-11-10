package com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model

data class PlannedHarvest(
    val id: String,
    val fruitName: String,
    val dateTime: String,
    val weight: String,
    val lotCode: String,
    val crewOrTransport: String = "Cuadrilla Fundo Central",
    val sectorOrVault: String = "Galpón 1",
    val statusText: String = "Listo para cosecha"
) {
    // Propiedades puente para UpcomingHarvestCard (Dashboard / Home)
    val productName: String get() = fruitName
    val dateFormatted: String get() = dateTime
    val expectedKg: Int get() = weight.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
    val crewName: String get() = crewOrTransport
}