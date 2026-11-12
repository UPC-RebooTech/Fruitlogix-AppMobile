package com.rebootech.fruitlogix.distributor.profilesManagement.domain.model

/** Alerts a distributor can receive on this phone. */
data class DistributorNotificationSettings(
    /** Reefer temperature at risk or out of range (cannot be silenced by quiet hours). */
    val temperatureAlerts: Boolean = true,
    /** A truck enters the hub perimeter or is at the gate. */
    val arrivals: Boolean = true,
    /** A delivery is late or its ETA changed. */
    val delays: Boolean = true,
    /** Lots waiting for a quality decision. */
    val qualityLots: Boolean = true,
    /** A producer accepts or rejects an order. */
    val orderUpdates: Boolean = true,
    /** Invoices that are about to expire or already overdue. */
    val invoices: Boolean = true,
    val quietHours: Boolean = false
) {
    val enabledAlerts: Int
        get() = listOf(temperatureAlerts, arrivals, delays, qualityLots, orderUpdates, invoices).count { it }

    companion object {
        const val TOTAL_ALERTS = 6
    }
}

data class DistributorSyncSettings(
    val autoSync: Boolean = true,
    val wifiOnly: Boolean = false
)

/** Connection to the FruitLogix server (Render). */
enum class ServerStatus { CONNECTING, ONLINE, OFFLINE }
