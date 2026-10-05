package com.rebootech.fruitlogix.logisticsMonitoring.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.logisticsMonitoring.data.FakeFleetRepository
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.Arrival
import com.rebootech.fruitlogix.logisticsMonitoring.domain.repository.FleetRepository
import com.rebootech.fruitlogix.logisticsMonitoring.domain.service.DetectGeofenceEntryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Inbound Arrivals screen.
 */
class ArrivalsViewModel(
    private val repository: FleetRepository = FakeFleetRepository(),
    private val detectGeofenceEntryUseCase: DetectGeofenceEntryUseCase = DetectGeofenceEntryUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArrivalsUiState())
    val uiState: StateFlow<ArrivalsUiState> = _uiState.asStateFlow()

    init {
        loadArrivals()
    }

    fun loadArrivals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val arrivalsList = repository.getArrivals()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    arrivals = arrivalsList
                )
            }
        }
    }

    fun selectFilter(filter: ArrivalsFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun simulateGeofenceEntry(targetUnitId: String = "FL-102") {
        val targetArrival = _uiState.value.arrivals.find { it.unitId == targetUnitId }
            ?: _uiState.value.arrivals.firstOrNull()

        if (targetArrival != null) {
            // Evaluate domain event
            detectGeofenceEntryUseCase.evaluateAndPublishEvent(targetArrival)

            _uiState.update {
                it.copy(
                    showGeofenceSheet = true,
                    simulatedAlertArrival = targetArrival
                )
            }
        }
    }

    fun dismissGeofenceSheet() {
        _uiState.update {
            it.copy(
                showGeofenceSheet = false,
                simulatedAlertArrival = null
            )
        }
    }
}
