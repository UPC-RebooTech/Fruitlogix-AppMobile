package com.rebootech.fruitlogix.producer.profilesManagement.data

import android.content.Context
import androidx.core.content.edit
import com.rebootech.fruitlogix.producer.profilesManagement.domain.model.NotificationSettings
import com.rebootech.fruitlogix.producer.profilesManagement.domain.model.SyncSettings
import com.rebootech.fruitlogix.producer.profilesManagement.domain.repository.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

/**
 * App settings stored on the device (SharedPreferences), so they survive restarts.
 * Sync itself is simulated until the backend exists.
 */
class PrefsSettingsRepository private constructor(context: Context) : SettingsRepository {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _notifications = MutableStateFlow(
        NotificationSettings(
            newOrders = prefs.getBoolean(KEY_NEW_ORDERS, true),
            qualityResults = prefs.getBoolean(KEY_QUALITY, true),
            coldChainAlerts = prefs.getBoolean(KEY_COLD_CHAIN, true),
            payments = prefs.getBoolean(KEY_PAYMENTS, true),
            messages = prefs.getBoolean(KEY_MESSAGES, true),
            quietHours = prefs.getBoolean(KEY_QUIET_HOURS, false)
        )
    )
    override val notifications: StateFlow<NotificationSettings> = _notifications.asStateFlow()

    private val _sync = MutableStateFlow(
        SyncSettings(
            autoSync = prefs.getBoolean(KEY_AUTO_SYNC, true),
            wifiOnly = prefs.getBoolean(KEY_WIFI_ONLY, false)
        )
    )
    override val sync: StateFlow<SyncSettings> = _sync.asStateFlow()

    private val _lastSyncAt = MutableStateFlow(
        prefs.getString(KEY_LAST_SYNC, null)?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
            ?: LocalDateTime.now().minusMinutes(12)
    )
    override val lastSyncAt: StateFlow<LocalDateTime?> = _lastSyncAt.asStateFlow()

    // TODO(producer): connect to the real offline queue.
    private val _pendingChanges = MutableStateFlow(0)
    override val pendingChanges: StateFlow<Int> = _pendingChanges.asStateFlow()

    override fun updateNotifications(settings: NotificationSettings) {
        _notifications.value = settings
        prefs.edit {
            putBoolean(KEY_NEW_ORDERS, settings.newOrders)
            putBoolean(KEY_QUALITY, settings.qualityResults)
            putBoolean(KEY_COLD_CHAIN, settings.coldChainAlerts)
            putBoolean(KEY_PAYMENTS, settings.payments)
            putBoolean(KEY_MESSAGES, settings.messages)
            putBoolean(KEY_QUIET_HOURS, settings.quietHours)
        }
    }

    override fun updateSync(settings: SyncSettings) {
        _sync.value = settings
        prefs.edit {
            putBoolean(KEY_AUTO_SYNC, settings.autoSync)
            putBoolean(KEY_WIFI_ONLY, settings.wifiOnly)
        }
    }

    override suspend fun syncNow() {
        delay(1200) // simulated network call
        val now = LocalDateTime.now()
        _lastSyncAt.value = now
        _pendingChanges.value = 0
        prefs.edit { putString(KEY_LAST_SYNC, now.toString()) }
    }

    companion object {
        private const val PREFS = "producer_settings"
        private const val KEY_NEW_ORDERS = "notif_new_orders"
        private const val KEY_QUALITY = "notif_quality"
        private const val KEY_COLD_CHAIN = "notif_cold_chain"
        private const val KEY_PAYMENTS = "notif_payments"
        private const val KEY_MESSAGES = "notif_messages"
        private const val KEY_QUIET_HOURS = "notif_quiet_hours"
        private const val KEY_AUTO_SYNC = "sync_auto"
        private const val KEY_WIFI_ONLY = "sync_wifi_only"
        private const val KEY_LAST_SYNC = "sync_last"

        @Volatile private var instance: PrefsSettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository =
            instance ?: synchronized(this) {
                instance ?: PrefsSettingsRepository(context).also { instance = it }
            }
    }
}
