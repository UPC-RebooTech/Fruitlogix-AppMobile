package com.rebootech.fruitlogix.producer.profilesManagement.domain.repository

import com.rebootech.fruitlogix.producer.profilesManagement.domain.model.NotificationSettings
import com.rebootech.fruitlogix.producer.profilesManagement.domain.model.SyncSettings
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

interface SettingsRepository {
    val notifications: StateFlow<NotificationSettings>
    val sync: StateFlow<SyncSettings>
    val lastSyncAt: StateFlow<LocalDateTime?>
    val pendingChanges: StateFlow<Int>

    fun updateNotifications(settings: NotificationSettings)
    fun updateSync(settings: SyncSettings)
    suspend fun syncNow()
}
