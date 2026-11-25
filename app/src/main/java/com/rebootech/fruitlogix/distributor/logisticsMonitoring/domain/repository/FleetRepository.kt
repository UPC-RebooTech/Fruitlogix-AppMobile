package com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.repository

import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.ActiveDispatch
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DelayReason
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DeliveryState
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.FleetActionResult
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalTime

interface FleetRepository {
    val dispatches: StateFlow<List<ActiveDispatch>>

    fun dispatch(unitId: String): ActiveDispatch?

    /** On route: for a server trip it also moves the order to "in transit" (start-dispatch). */
    suspend fun updateDeliveryState(unitId: String, state: DeliveryState): FleetActionResult

    suspend fun updateEta(unitId: String, newEta: LocalTime, reason: DelayReason): FleetActionResult

    suspend fun recordReeferCheck(unitId: String)

    /**
     * Delivery proof registered at the client. A server trip is marked as arrived (POST arrive);
     * the client then confirms the reception from their app, which closes it as delivered.
     */
    suspend fun completeDelivery(unitId: String, latitude: Double? = null, longitude: Double? = null): FleetActionResult

    suspend fun rejectDelivery(unitId: String, reason: String)

    /** Point on the route (toll, weigh station…), only while the truck is on the road. */
    suspend fun addCheckpoint(unitId: String, name: String, note: String?, latitude: Double?, longitude: Double?): FleetActionResult =
        FleetActionResult(ok = false, onServer = false)

    /** Changes the driver or the truck before the arrival. Blank values are not sent.
     * With [driverId] or [vehicleId] (from /drivers and /vehicles) the server takes the names from them. */
    suspend fun changeAssignment(unitId: String, driverName: String, driverPhone: String, plate: String, vehicleType: String, driverId: Int? = null, vehicleId: Int? = null): FleetActionResult =
        FleetActionResult(ok = false, onServer = false)

    /** GPS position with the reefer reading (tracking). Inside the client's warehouse it becomes "arrived". */
    suspend fun sendPosition(unitId: String, latitude: Double, longitude: Double, celsius: Double, humidity: Double): FleetActionResult =
        FleetActionResult(ok = false, onServer = false)

    suspend fun refresh() {}
}
