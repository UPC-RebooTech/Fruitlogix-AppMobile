package com.rebootech.fruitlogix.producer.profilesManagement.domain.model

import java.time.YearMonth

/**
 * Producer account profile: the person who uses the app plus the farm they operate.
 * The account is the person (shown on the account card); farm and legal data are
 * business details shown only in "Edit profile".
 */
data class ProducerProfile(
    val fullName: String,
    val email: String,
    val phone: String,
    val farmName: String,
    val legalName: String,
    val ruc: String,
    val location: String,
    val globalGapValidUntil: YearMonth?,
    /** Main crop (required by the server). */
    val crop: String = "",
    val type: ProducerType = ProducerType.COMPANY
)

/** Legal type on the server: "Individual" (person) or "Company". */
enum class ProducerType(val serverValue: String) { INDIVIDUAL("Individual"), COMPANY("Company") }

/** Whether the signed-in producer already has a profile on the server. */
enum class ProfileStatus { LOADING, READY, MISSING, OFFLINE }

/** Outcome of saving the profile on the server; [serverMessage] is the server's own text. */
data class ProfileSaveResult(val ok: Boolean, val serverMessage: String? = null)

/** In-app notification preferences. */
data class NotificationSettings(
    val newOrders: Boolean = true,
    val qualityResults: Boolean = true,
    val coldChainAlerts: Boolean = true,
    val payments: Boolean = true,
    val messages: Boolean = true,
    val quietHours: Boolean = false
)

/** Offline and synchronization preferences. */
data class SyncSettings(
    val autoSync: Boolean = true,
    val wifiOnly: Boolean = false
)
