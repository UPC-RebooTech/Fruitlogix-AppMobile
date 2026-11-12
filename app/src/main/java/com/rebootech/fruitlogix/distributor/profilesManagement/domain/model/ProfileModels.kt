package com.rebootech.fruitlogix.distributor.profilesManagement.domain.model

/** Legal type of a producer; [backendValue] is what the Render API expects. */
enum class ProducerEntityType(val backendValue: String) {
    INDIVIDUAL("Individual"),
    COMPANY("Company")
}

enum class ProducerStatus { ACTIVE, INVITED }

/** A producer in the distributor's network. Names match Orders, Fleet and Invoices. */
data class Producer(
    val id: String,
    val name: String,
    val farmName: String,
    val location: String,
    val cropType: String,
    val email: String,
    val phone: String,
    val entityType: ProducerEntityType,
    val qualityRating: Double,
    val weeklyTons: Double,
    val stockKg: Int,
    val activeOrders: Int,
    val incidents30d: Int,
    val status: ProducerStatus = ProducerStatus.ACTIVE
)

/** What the distributor fills in to register or edit a producer (US05, US39). */
data class ProducerDraft(
    val name: String,
    val farmName: String,
    val location: String,
    val cropType: String,
    val email: String,
    val phone: String,
    val entityType: ProducerEntityType
)

/** Network summary (US42). */
data class ProducerKpis(
    val total: Int,
    val averageQuality: Double,
    val weeklyTons: Double,
    val incidents30d: Int
)

/** The signed-in distributor's business data. */
data class DistributorProfile(
    val fullName: String,
    val roleTitle: String,
    val companyHub: String,
    val email: String,
    val phone: String,
    val ruc: String
)
