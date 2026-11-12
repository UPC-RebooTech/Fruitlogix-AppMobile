package com.rebootech.fruitlogix.distributor.profilesManagement.domain.repository

import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.DistributorNotificationSettings
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.DistributorSyncSettings
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ServerStatus
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

/** Settings saved on this phone, plus the server sync status. */
interface DistributorSettingsRepository {
    val notifications: StateFlow<DistributorNotificationSettings>
    val sync: StateFlow<DistributorSyncSettings>
    val lastSyncAt: StateFlow<LocalDateTime?>
    val serverStatus: StateFlow<ServerStatus>

    fun updateNotifications(settings: DistributorNotificationSettings)
    fun updateSync(settings: DistributorSyncSettings)

    /** Pulls invoices and producers from the server now. */
    suspend fun syncNow()
}
