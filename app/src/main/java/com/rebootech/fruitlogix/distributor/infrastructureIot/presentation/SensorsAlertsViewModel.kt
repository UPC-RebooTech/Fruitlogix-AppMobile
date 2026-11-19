package com.rebootech.fruitlogix.distributor.infrastructureIot.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.DistributorDependencies
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.AlertSeverity
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.DeviceStatus
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.FruitThreshold
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotAlert
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotDevice
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.ThresholdRules
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.repository.IotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SensorsAlertsUiState(
    val devices: List<IotDevice> = emptyList(),
    val alerts: List<IotAlert> = emptyList(),
    val thresholds: List<FruitThreshold> = emptyList(),
    val editing: FruitThreshold? = null,
    @field:StringRes val editError: Int? = null,
    /** Fruit whose limits were just saved (for the confirmation). */
    val savedFruit: String? = null
) {
    val online: Int get() = devices.count { it.status == DeviceStatus.ONLINE }
    val lowBattery: Int get() = devices.count { it.status == DeviceStatus.LOW_BATTERY }
    val offline: Int get() = devices.count { it.status == DeviceStatus.OFFLINE }
}

/** Same sensors and alerts the Fleet screen shows (one shared IoT repository). */
class SensorsAlertsViewModel(private val repository: IotRepository) : ViewModel() {

    /** Used by the default ViewModel factory. */
    constructor() : this(DistributorDependencies.iotRepository)

    private val local = MutableStateFlow(SensorsAlertsUiState())

    val uiState: StateFlow<SensorsAlertsUiState> = combine(
        repository.devices, repository.alerts, repository.thresholds, local
    ) { devices, alerts, thresholds, local ->
        local.copy(
            // Problems first: no signal, low battery, then the rest
            devices = devices.sortedBy {
                when (it.status) {
                    DeviceStatus.OFFLINE -> 0
                    DeviceStatus.LOW_BATTERY -> 1
                    DeviceStatus.CALIBRATING -> 2
                    DeviceStatus.ONLINE -> 3
                }
            },
            alerts = alerts.sortedWith(compareByDescending<IotAlert> { it.severity == AlertSeverity.CRITICAL }.thenByDescending { it.createdAt }),
            thresholds = thresholds
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SensorsAlertsUiState())

    fun calibrate(deviceId: String) {
        viewModelScope.launch { repository.calibrate(deviceId) }
    }

    fun resolveAlert(alertId: String) {
        viewModelScope.launch { repository.resolveAlert(alertId) }
    }

    fun edit(threshold: FruitThreshold) = local.update { it.copy(editing = threshold, editError = null) }

    fun cancelEdit() = local.update { it.copy(editing = null, editError = null) }

    fun onSavedShown() = local.update { it.copy(savedFruit = null) }

    fun save(minTemp: String, maxTemp: String, minHum: String, maxHum: String) {
        val editing = local.value.editing ?: return
        fun parse(text: String) = text.trim().replace(',', '.').toDoubleOrNull()
        val values = listOf(parse(minTemp), parse(maxTemp), parse(minHum), parse(maxHum))
        ThresholdRules.validate(values[0], values[1], values[2], values[3])?.let {
            local.update { s -> s.copy(editError = it.toMessage()) }
            return
        }
        viewModelScope.launch {
            val error = repository.updateThreshold(editing.id, values[0]!!, values[1]!!, values[2]!!, values[3]!!)
            local.update {
                if (error != null) it.copy(editError = error.toMessage())
                else it.copy(editing = null, editError = null, savedFruit = editing.fruitName)
            }
        }
    }

    private fun ThresholdRules.Error.toMessage(): Int = when (this) {
        ThresholdRules.Error.NOT_A_NUMBER -> R.string.sensors_error_number
        ThresholdRules.Error.TEMP_RANGE -> R.string.sensors_error_temp
        ThresholdRules.Error.HUMIDITY_RANGE -> R.string.sensors_error_humidity
    }
}
