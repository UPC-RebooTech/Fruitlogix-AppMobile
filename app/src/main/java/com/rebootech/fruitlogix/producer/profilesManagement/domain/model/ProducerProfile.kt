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
    val globalGapValidUntil: YearMonth?
)

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
