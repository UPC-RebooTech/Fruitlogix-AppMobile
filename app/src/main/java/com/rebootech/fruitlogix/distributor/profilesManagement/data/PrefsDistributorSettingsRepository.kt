package com.rebootech.fruitlogix.distributor.profilesManagement.data

import android.content.Context
import androidx.core.content.edit
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.DistributorNotificationSettings
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.DistributorSyncSettings
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ServerStatus
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.repository.DistributorSettingsRepository
import com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDateTime

/**
 * Distributor settings stored on the phone (SharedPreferences, so they survive restarts).
 * "Sync now" asks the repositories that talk to Render (invoices, producers) to refresh.
 */
class PrefsDistributorSettingsRepository(
    context: Context,
    private val refreshers: List<suspend () -> Unit>
) : DistributorSettingsRepository {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _notifications = MutableStateFlow(
        DistributorNotificationSettings(
            temperatureAlerts = prefs.getBoolean(KEY_TEMPERATURE, true),
            arrivals = prefs.getBoolean(KEY_ARRIVALS, true),
            delays = prefs.getBoolean(KEY_DELAYS, true),
            qualityLots = prefs.getBoolean(KEY_QUALITY, true),
            orderUpdates = prefs.getBoolean(KEY_ORDERS, true),
            invoices = prefs.getBoolean(KEY_INVOICES, true),
            quietHours = prefs.getBoolean(KEY_QUIET, false)
        )
    )
    override val notifications: StateFlow<DistributorNotificationSettings> = _notifications.asStateFlow()

    private val _sync = MutableStateFlow(
        DistributorSyncSettings(
            autoSync = prefs.getBoolean(KEY_AUTO_SYNC, true),
            wifiOnly = prefs.getBoolean(KEY_WIFI_ONLY, false)
        )
    )
    override val sync: StateFlow<DistributorSyncSettings> = _sync.asStateFlow()

    private val _lastSyncAt = MutableStateFlow(
        prefs.getString(KEY_LAST_SYNC, null)?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
    )
    override val lastSyncAt: StateFlow<LocalDateTime?> = _lastSyncAt.asStateFlow()

    override val serverStatus: StateFlow<ServerStatus> = RetrofitClient.syncStatus.map {
        when (it) {
            BackendSyncStatus.CONNECTING -> ServerStatus.CONNECTING
            BackendSyncStatus.ONLINE -> ServerStatus.ONLINE
            BackendSyncStatus.OFFLINE_FALLBACK -> ServerStatus.OFFLINE
        }
    }.stateIn(scope, SharingStarted.Eagerly, ServerStatus.CONNECTING)

    override fun updateNotifications(settings: DistributorNotificationSettings) {
        _notifications.value = settings
        prefs.edit {
            putBoolean(KEY_TEMPERATURE, settings.temperatureAlerts)
            putBoolean(KEY_ARRIVALS, settings.arrivals)
            putBoolean(KEY_DELAYS, settings.delays)
            putBoolean(KEY_QUALITY, settings.qualityLots)
            putBoolean(KEY_ORDERS, settings.orderUpdates)
            putBoolean(KEY_INVOICES, settings.invoices)
            putBoolean(KEY_QUIET, settings.quietHours)
        }
    }

    override fun updateSync(settings: DistributorSyncSettings) {
        _sync.value = settings
        prefs.edit {
            putBoolean(KEY_AUTO_SYNC, settings.autoSync)
            putBoolean(KEY_WIFI_ONLY, settings.wifiOnly)
        }
    }

    override suspend fun syncNow() {
        RetrofitClient.updateSyncStatus(BackendSyncStatus.CONNECTING)
        refreshers.forEach { refresh -> runCatching { refresh() } }
        val now = LocalDateTime.now()
        _lastSyncAt.value = now
        prefs.edit { putString(KEY_LAST_SYNC, now.toString()) }
    }

    private companion object {
        const val PREFS = "distributor_settings"
        const val KEY_TEMPERATURE = "notif_temperature"
        const val KEY_ARRIVALS = "notif_arrivals"
        const val KEY_DELAYS = "notif_delays"
        const val KEY_QUALITY = "notif_quality"
        const val KEY_ORDERS = "notif_orders"
        const val KEY_INVOICES = "notif_invoices"
        const val KEY_QUIET = "notif_quiet"
        const val KEY_AUTO_SYNC = "sync_auto"
        const val KEY_WIFI_ONLY = "sync_wifi_only"
        const val KEY_LAST_SYNC = "sync_last"
    }
}
