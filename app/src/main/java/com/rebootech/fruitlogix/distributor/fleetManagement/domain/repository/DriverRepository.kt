package com.rebootech.fruitlogix.distributor.fleetManagement.domain.repository

import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Driver
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.DriverDraft
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.PendingDispatch
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Vehicle
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleDraft
import kotlinx.coroutines.flow.StateFlow

/** Result of a fleet change sent to the server; [serverMessage] is its own text when it refused it. */
data class FleetSaveResult(val ok: Boolean, val serverMessage: String? = null, val offline: Boolean = false)

interface DriverRepository {
    val drivers: StateFlow<List<Driver>>
    val vehicles: StateFlow<List<Vehicle>>
    /** Orders ready to leave without a truck yet. */
    val pendingDispatches: StateFlow<List<PendingDispatch>>

    /**
     * Creates the delivery with the driver and the truck (POST /deliveries with driverId and vehicleId).
     * Call after [com.rebootech.fruitlogix.distributor.fleetManagement.domain.rules.AssignmentRules] accepted it.
     * @param routeKm road distance to the client; the server needs it to create the delivery.
     */
    suspend fun assign(orderId: String, driverId: String, vehicleId: String, routeKm: Double): FleetSaveResult

    /** Call after [com.rebootech.fruitlogix.distributor.fleetManagement.domain.rules.FleetResourceRules] accepted it. */
    suspend fun addDriver(draft: DriverDraft): FleetSaveResult

    suspend fun addVehicle(draft: VehicleDraft): FleetSaveResult

    /** Back from a trip or from resting: the driver (and the truck they had) can be assigned again. */
    suspend fun markDriverAvailable(driverId: String): FleetSaveResult

    /** Back from a trip or from the workshop. */
    suspend fun markVehicleAvailable(vehicleId: String): FleetSaveResult

    /** Reads drivers and vehicles from the server again. */
    suspend fun refresh() {}
}
