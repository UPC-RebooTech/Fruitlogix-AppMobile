package com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.distributor.DistributorDependencies
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.AlertSeverity
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotAlert
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.repository.IotRepository
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.GeofenceZone
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.ShipmentEnteredGeofence
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.repository.FleetRepository
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules.ArrivalRules
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.service.DetectGeofenceEntryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class FleetTab { ON_ROUTE, ARRIVALS, ALERTS }

/** A truck that just crossed the hub perimeter (shown as a sheet + notification). */
data class GeofenceEntry(val item: DispatchItem, val event: ShipmentEnteredGeofence, val minutesToDock: Int)

data class FleetUiState(
    val tab: FleetTab = FleetTab.ON_ROUTE,
    val onRoute: List<DispatchItem> = emptyList(),
    val arrivals: List<DispatchItem> = emptyList(),
    val alerts: List<IotAlert> = emptyList(),
    val geofenceEntry: GeofenceEntry? = null,
    val hubName: String = GeofenceZone().name
)

/**
 * Fleet center: one place for trucks on route, arrivals at the hub and alerts.
 * Trips come from Logistics; temperatures and alerts from IoT (domain interfaces only).
 */
class FleetViewModel(
    private val fleet: FleetRepository,
    private val iot: IotRepository
) : ViewModel() {

    /** Used by the default ViewModel factory. */
    constructor() : this(DistributorDependencies.fleetRepository, DistributorDependencies.iotRepository)

    private val geofence = DetectGeofenceEntryUseCase()
    private val tab = MutableStateFlow(FleetTab.ON_ROUTE)
    private val entry = MutableStateFlow<GeofenceEntry?>(null)

    val uiState: StateFlow<FleetUiState> = combine(
        fleet.dispatches, iot.devices, iot.alerts, tab, entry
    ) { dispatches, _, alerts, tab, entry ->
        val items = dispatches.filter { it.isActive }.map { buildDispatchItem(it, iot) }
        FleetUiState(
            tab = tab,
            onRoute = items.sortedWith(compareBy({ it.sortKey }, { it.dispatch.remainingKm })),
            arrivals = items.filter { ArrivalRules.isArriving(it.dispatch) }.sortedBy { it.dispatch.remainingKm },
            alerts = alerts.sortedWith(compareByDescending<IotAlert> { it.severity == AlertSeverity.CRITICAL }.thenByDescending { it.createdAt }),
            geofenceEntry = entry
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FleetUiState())

    fun onTabSelected(value: FleetTab) {
        tab.value = value
    }

    fun resolveAlert(alertId: String) {
        viewModelScope.launch { iot.resolveAlert(alertId) }
    }

    /**
     * Demo of the geofence: the closest approaching truck is placed 1.8 km from the hub.
     * TODO(gps): feed real positions from the trucks' GPS.
     */
    fun simulateGeofenceEntry() {
        val item = uiState.value.arrivals.firstOrNull { !ArrivalRules.isAtGate(it.dispatch) }
            ?: uiState.value.arrivals.firstOrNull() ?: return
        val distance = SIMULATED_DISTANCE_KM
        val event = geofence.evaluate(item.dispatch.unitId, distance) ?: return
        entry.value = GeofenceEntry(item, event, minutesToDock = (distance / HUB_SPEED_KMH * 60).roundToInt())
    }

    fun dismissGeofenceEntry() {
        entry.value = null
    }

    private companion object {
        const val SIMULATED_DISTANCE_KM = 1.8
        /** Average speed inside the Callao port area. */
        const val HUB_SPEED_KMH = 18.0
    }
}
