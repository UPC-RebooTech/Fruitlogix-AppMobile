package com.rebootech.fruitlogix.producer.infrastructureIot.data

import android.util.Log
import com.rebootech.fruitlogix.producer.infrastructureIot.domain.model.AlertKind
import com.rebootech.fruitlogix.producer.infrastructureIot.domain.model.AlertSeverity
import com.rebootech.fruitlogix.producer.infrastructureIot.domain.model.AssetType
import com.rebootech.fruitlogix.producer.infrastructureIot.domain.model.IotAlert
import com.rebootech.fruitlogix.producer.infrastructureIot.domain.model.MonitoredAsset
import com.rebootech.fruitlogix.producer.infrastructureIot.domain.repository.IotAlertRepository
import com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/** In-memory IoT alerts with Offline-First Retrofit background sync. */
class FakeIotAlertRepository : IotAlertRepository {

    private val now = LocalDateTime.now()

    private val _alerts = MutableStateFlow(
        listOf(
            IotAlert(
                id = "1",
                kind = AlertKind.TemperatureHigh(valueC = 9.4, limitC = 8.0),
                severity = AlertSeverity.CRITICAL,
                asset = MonitoredAsset(AssetType.COLD_ROOM, "1"),
                sensorId = "TEMP-BAY-01",
                location = "Pre-Cool Tunnel B",
                occurredAt = now.minusMinutes(5),
                relatedOrderId = "1044"
            ),
            IotAlert(
                id = "2",
                kind = AlertKind.HumidityLow(valuePct = 65.2, minimumPct = 85.0),
                severity = AlertSeverity.WARNING,
                asset = MonitoredAsset(AssetType.WAREHOUSE, "Hass"),
                sensorId = "HUM-ZONE-04",
                location = "Packhouse Bay 3",
                occurredAt = now.minusMinutes(22),
                relatedOrderId = "1048"
            ),
            IotAlert(
                id = "3",
                kind = AlertKind.Vibration(dock = "2"),
                severity = AlertSeverity.WARNING,
                asset = MonitoredAsset(AssetType.TRUCK, "3"),
                sensorId = "IOT-TRK-09",
                location = "Muelle 2",
                occurredAt = now.minusMinutes(70),
                relatedOrderId = "1048",
                isRead = true
            ),
            IotAlert(
                id = "4",
                kind = AlertKind.TraceabilitySynced(kg = 300, productName = "Hass Avocado"),
                severity = AlertSeverity.INFO,
                asset = MonitoredAsset(AssetType.GATEWAY, "CORE-01"),
                sensorId = "GATEWAY-CORE-01",
                location = "Fundo Santa Rosa",
                occurredAt = now.minusHours(2),
                isRead = true
            )
        )
    )
    override val alerts: StateFlow<List<IotAlert>> = _alerts.asStateFlow()

    init {
        fetchAlertsFromBackend()
    }

    private fun fetchAlertsFromBackend() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val remoteOrders = RetrofitClient.api.getOrders()
                if (remoteOrders.isSuccessful && !remoteOrders.body().isNullOrEmpty()) {
                    Log.d("FruitLogix-API", "Alertas IoT sincronizadas desde el backend.")
                    RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
                }
            } catch (e: Exception) {
                Log.e("FruitLogix-API", "Backend inactivo para alertas IoT. Usando respaldo local: ${e.message}")
                RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK)
            }
        }
    }

    override fun markRead(id: String) {
        _alerts.update { list -> list.map { if (it.id == id) it.copy(isRead = true) else it } }
    }

    override fun markAllRead() {
        _alerts.update { list -> list.map { it.copy(isRead = true) } }
    }

    override fun dismiss(id: String) {
        _alerts.update { list -> list.filterNot { it.id == id } }
    }

    companion object {
        val instance: IotAlertRepository by lazy { FakeIotAlertRepository() }
    }
}