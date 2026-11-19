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
    val status: ProducerStatus = ProducerStatus.ACTIVE,
    /** RUC (11 digits); the server requires it. */
    val ruc: String = "",
    /** Id on the FruitLogix server; null for producers that only live on this phone (samples, invites). */
    val remoteId: Int? = null
)

/** What the distributor fills in to register or edit a producer (US05, US39). */
data class ProducerDraft(
    val name: String,
    val farmName: String,
    val location: String,
    val cropType: String,
    val email: String,
    val phone: String,
    val entityType: ProducerEntityType,
    val ruc: String = ""
)

/** Result of saving a producer on the server. */
sealed interface ProducerSaveResult {
    data class Saved(val producer: Producer) : ProducerSaveResult
    /** The server did not answer: the change was kept on this phone only. */
    data class SavedOnPhoneOnly(val producer: Producer) : ProducerSaveResult
    /** Another producer already has this RUC (409). */
    data object RucTaken : ProducerSaveResult
    /** The server refused the data (400). */
    data object Rejected : ProducerSaveResult
}

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
