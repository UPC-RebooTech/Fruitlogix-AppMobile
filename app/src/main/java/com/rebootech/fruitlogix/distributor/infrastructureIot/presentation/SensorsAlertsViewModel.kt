package com.rebootech.fruitlogix.distributor.infrastructureIot.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.distributor.infrastructureIot.data.FakeIotRepository
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.FruitThreshold
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.repository.IotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SensorsAlertsViewModel(
    private val repository: IotRepository = FakeIotRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SensorsAlertsUiState(isLoading = true))
    val uiState: StateFlow<SensorsAlertsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.getDevices(),
                repository.getActiveAlerts(),
                repository.getFruitThresholds()
            ) { devices, alerts, thresholds ->
                Triple(devices, alerts, thresholds)
            }.collect { (devices, alerts, thresholds) ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        devices = devices,
                        alerts = alerts,
                        thresholds = thresholds
                    )
                }
            }
        }
    }

    fun onCalibrateDevice(deviceId: String) {
        viewModelScope.launch {
            repository.calibrateDevice(deviceId)
        }
    }

    fun onDismissAlert(alertId: String) {
        viewModelScope.launch {
            repository.dismissAlert(alertId)
        }
    }

    fun onSelectThresholdToEdit(threshold: FruitThreshold) {
        _uiState.update { it.copy(editingThreshold = threshold, thresholdError = null) }
    }

    fun onDismissThresholdDialog() {
        _uiState.update { it.copy(editingThreshold = null, thresholdError = null) }
    }

    fun onSaveThreshold(
        fruitId: String,
        minTempText: String,
        maxTempText: String,
        minHumText: String,
        maxHumText: String
    ) {
        val minT = minTempText.toDoubleOrNull()
        val maxT = maxTempText.toDoubleOrNull()
        val minH = minHumText.toDoubleOrNull()
        val maxH = maxHumText.toDoubleOrNull()

        if (minT == null || maxT == null || minH == null || maxH == null) {
            _uiState.update {
                it.copy(thresholdError = "Ingresa valores numéricos válidos en todos los campos.")
            }
            return
        }

        val result = repository.updateFruitThreshold(fruitId, minT, maxT, minH, maxH)
        result.fold(
            onSuccess = { saved ->
                _uiState.update {
                    it.copy(
                        editingThreshold = null,
                        thresholdError = null,
                        feedbackMessage = "Umbral de ${saved.fruitName} actualizado (${saved.minTempCelsius}°C a ${saved.maxTempCelsius}°C • ${saved.minHumidityPercent.toInt()}%-${saved.maxHumidityPercent.toInt()}% RH)."
                    )
                }
            },
            onFailure = { err ->
                _uiState.update { it.copy(thresholdError = err.message) }
            }
        )
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }
}