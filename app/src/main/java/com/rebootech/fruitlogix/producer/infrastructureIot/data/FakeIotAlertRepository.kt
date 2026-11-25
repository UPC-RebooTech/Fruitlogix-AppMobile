package com.rebootech.fruitlogix.producer.infrastructureIot.data

import com.rebootech.fruitlogix.producer.infrastructureIot.domain.model.AlertKind
import com.rebootech.fruitlogix.producer.infrastructureIot.domain.model.AlertSeverity
import com.rebootech.fruitlogix.producer.infrastructureIot.domain.model.AssetType
import com.rebootech.fruitlogix.producer.infrastructureIot.domain.model.IotAlert
import com.rebootech.fruitlogix.producer.infrastructureIot.domain.model.MonitoredAsset
import com.rebootech.fruitlogix.producer.infrastructureIot.domain.repository.IotAlertRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * IoT alerts of the producer. There are no sample alerts; the list stays empty until /alerts is
 * connected (the previous code only pinged /orders and showed fixed alerts).
 */
class FakeIotAlertRepository : IotAlertRepository {

    private val _alerts = MutableStateFlow<List<IotAlert>>(emptyList())
    override val alerts: StateFlow<List<IotAlert>> = _alerts.asStateFlow()

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