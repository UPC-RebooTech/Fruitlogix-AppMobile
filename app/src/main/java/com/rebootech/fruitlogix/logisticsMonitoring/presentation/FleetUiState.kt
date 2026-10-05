package com.rebootech.fruitlogix.logisticsMonitoring.presentation

import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.DispatchSummary
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.FleetAlert
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.Sensor
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.SensorFilter
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.SensorStatus

/**
 * UI state for the Fleet Control Center screen.
 */
data class FleetUiState(
    val isLoading: Boolean = false,
    val onRouteCount: Int = 0,
    val alertCount: Int = 0,
    val dispatches: List<DispatchSummary> = emptyList(),
    val alerts: List<FleetAlert> = emptyList(),
    val allSensors: List<Sensor> = emptyList(),
    /** Currently selected tab index: 0=Active Dispatches, 1=Alerts, 2=Sensors. */
    val selectedTabIndex: Int = 0,
    /** Active filter for the Sensors sub-tab. */
    val sensorFilter: SensorFilter = SensorFilter.ALL,
    /** Sensor currently open in the bottom sheet, null when closed. */
    val selectedSensor: Sensor? = null
) {
    /** Filtered sensors list derived from [allSensors] and [sensorFilter]. */
    val filteredSensors: List<Sensor>
        get() = when (sensorFilter) {
            SensorFilter.ALL -> allSensors
            SensorFilter.ON_ROUTE -> allSensors.filter { it.linkedTruckId != null }
            SensorFilter.IN_WAREHOUSE -> allSensors.filter { it.warehouseLocation != null }
            SensorFilter.OFFLINE -> allSensors.filter {
                it.status == SensorStatus.OFFLINE
            }
        }

    val onlineSensorCount: Int
        get() = allSensors.count {
            it.status == SensorStatus.ONLINE
        }

    val lowBatterySensorCount: Int
        get() = allSensors.count {
            it.status == SensorStatus.LOW_BATTERY
        }

    val offlineSensorCount: Int
        get() = allSensors.count {
            it.status == SensorStatus.OFFLINE
        }
}
