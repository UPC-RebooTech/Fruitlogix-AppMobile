package com.rebootech.fruitlogix.distributor.profilesManagement.domain.model

enum class ProducerEntityType(val backendValue: String, val displayLabel: String) {
    INDIVIDUAL("Individual", "Persona Natural (Individual)"),
    COMPANY("Company", "Empresa Agrícola (Company)")
}

data class ProducerItem(
    val id: String,
    val name: String,
    val farmName: String,
    val location: String,
    val cropType: String,
    val email: String,
    val phone: String,
    val entityType: ProducerEntityType,
    val qualityRating: Double = 4.5,
    val weeklyTons: Double = 2.0,
    val currentStockSummary: String = "Palta Hass: 150kg",
    val activeOrdersCount: Int = 1,
    val incidents30d: Int = 0,
    val isPending: Boolean = false
)

data class ProducerKpiSummary(
    val totalProducers: Int = 0,
    val averageQuality: Double = 0.0,
    val weeklyProductionTons: Double = 0.0,
    val incidentsLast30d: Int = 0
)

data class DistributorProfile(
    val id: String = "DIST-01",
    val fullName: String,
    val roleTitle: String,
    val companyHub: String,
    val email: String,
    val phone: String,
    val ruc: String
)