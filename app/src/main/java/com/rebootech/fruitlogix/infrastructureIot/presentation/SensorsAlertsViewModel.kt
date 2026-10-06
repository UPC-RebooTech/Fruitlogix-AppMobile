package com.rebootech.fruitlogix.infrastructureIot.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.infrastructureIot.data.FakeIotRepository
import com.rebootech.fruitlogix.infrastructureIot.domain.repository.IotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
            combine(repository.getDevices(), repository.getActiveAlerts()) { devices, alerts ->
                SensorsAlertsUiState(
                    isLoading = false,
                    devices = devices,
                    alerts = alerts
                )
            }.collect { state ->
                _uiState.value = state
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
}