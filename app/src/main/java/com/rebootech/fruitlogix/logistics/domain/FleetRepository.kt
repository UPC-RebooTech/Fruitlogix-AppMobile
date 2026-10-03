package com.rebootech.fruitlogix.logistics.domain

/**
 * Repository interface for the Fleet Control Center.
 * The domain layer depends only on this abstraction.
 */
interface FleetRepository {

    /** Returns the list of active dispatches (on-route trucks). */
    fun getActiveDispatches(): List<DispatchSummary>

    /** Returns the list of active fleet alerts. */
    fun getFleetAlerts(): List<FleetAlert>

    /** Returns all sensors tracked by the system. */
    fun getAllSensors(): List<Sensor>

    /** Returns the count of dispatches currently on route. */
    fun getOnRouteCount(): Int = getActiveDispatches().size

    /** Returns the count of active alerts. */
    fun getAlertCount(): Int = getFleetAlerts().size
}
