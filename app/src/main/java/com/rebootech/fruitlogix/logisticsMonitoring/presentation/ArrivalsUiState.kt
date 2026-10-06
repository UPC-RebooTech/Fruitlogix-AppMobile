package com.rebootech.fruitlogix.logisticsMonitoring.presentation

import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.Arrival
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.ArrivalStatus

/**
 * UI State for the Inbound Arrivals screen.
 */
data class ArrivalsUiState(
    val isLoading: Boolean = false,
    val selectedFilter: ArrivalsFilter = ArrivalsFilter.ALL,
    val arrivals: List<Arrival> = emptyList(),
    val showGeofenceSheet: Boolean = false,
    val simulatedAlertArrival: Arrival? = null
) {
    val filteredArrivals: List<Arrival>
        get() {
            val list = when (selectedFilter) {
                ArrivalsFilter.ALL -> arrivals
                ArrivalsFilter.APPROACHING -> arrivals.filter { it.status == ArrivalStatus.APPROACHING }
                ArrivalsFilter.AT_GATE -> arrivals.filter { it.status == ArrivalStatus.AT_GATE }
            }
            return list.sortedBy { it.distanceKm }.take(3)
        }
}
