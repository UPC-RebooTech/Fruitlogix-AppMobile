package com.rebootech.fruitlogix.logisticsMonitoring.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.logisticsMonitoring.data.FakeFleetRepository
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.Arrival
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.ArrivalStatus
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
        val baseArrival = _uiState.value.arrivals.find { it.unitId == targetUnitId }
            ?: _uiState.value.arrivals.firstOrNull()

        // Simulation specific values: FL-102 at 1.8 km from gate, speed 18 km/h, computed ETA = 6 min
        val simulatedArrival = (baseArrival ?: Arrival(
            unitId = "FL-102",
            licensePlate = "BQK-482",
            driverName = "Jorge Huamán",
            cargoDescription = "Ica grapes",
            palletsCount = 16,
            orderId = "FX-1040",
            assignedDock = "Dock B",
            reeferTemp = "3.4°C",
            humidity = "88% RH",
            distanceKm = 1.8f,
            distanceLabel = "1.8 km to warehouse",
            status = ArrivalStatus.APPROACHING,
            etaMinutes = 6,
            speedKmh = "18 km/h"
        )).copy(
            distanceKm = 1.8f,
            speedKmh = "18 km/h",
            etaMinutes = 6,
            status = ArrivalStatus.APPROACHING
        )

        detectGeofenceEntryUseCase.evaluateAndPublishEvent(simulatedArrival)

        _uiState.update {
            it.copy(
                showGeofenceSheet = true,
                simulatedAlertArrival = simulatedArrival
            )
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
