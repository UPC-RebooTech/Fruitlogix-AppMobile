package com.rebootech.fruitlogix.logisticsMonitoring.presentation

import androidx.lifecycle.ViewModel
import com.rebootech.fruitlogix.logisticsMonitoring.data.FakeFleetRepository
import com.rebootech.fruitlogix.logisticsMonitoring.domain.repository.FleetRepository
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.Sensor
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.SensorFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for the Fleet Control Center.
 *
 * Uses [FakeFleetRepository] directly (no DI framework required).
 * Real-world wiring would inject a [FleetRepository] abstraction.
 */
class FleetViewModel(
    private val repository: FleetRepository = FakeFleetRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FleetUiState(isLoading = true))
    val uiState: StateFlow<FleetUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val dispatches = repository.getActiveDispatches()
        val alerts = repository.getFleetAlerts()
        val sensors = repository.getAllSensors()

        _uiState.update {
            FleetUiState(
                isLoading = false,
                onRouteCount = dispatches.size,
                alertCount = alerts.size,
                dispatches = dispatches,
                alerts = alerts,
                allSensors = sensors
            )
        }
    }

    /** Switch the active segmented tab (0 = Dispatches, 1 = Alerts, 2 = Sensors). */
    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    /** Apply a sensor filter in the Sensors tab. */
    fun onSensorFilterSelected(filter: SensorFilter) {
        _uiState.update { it.copy(sensorFilter = filter) }
    }

    /** Open the sensor detail bottom sheet. */
    fun onSensorClick(sensor: Sensor) {
        _uiState.update { it.copy(selectedSensor = sensor) }
    }

    /** Dismiss the sensor detail bottom sheet. */
    fun onDismissSensorSheet() {
        _uiState.update { it.copy(selectedSensor = null) }
    }
}
