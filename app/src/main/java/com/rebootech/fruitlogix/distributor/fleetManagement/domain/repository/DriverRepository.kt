package com.rebootech.fruitlogix.distributor.fleetManagement.domain.repository

import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Driver
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.DriverDraft
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.PendingDispatch
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Vehicle
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleDraft
import kotlinx.coroutines.flow.StateFlow

interface DriverRepository {
    val drivers: StateFlow<List<Driver>>
    val vehicles: StateFlow<List<Vehicle>>
    /** Orders ready to leave without a truck yet. */
    val pendingDispatches: StateFlow<List<PendingDispatch>>

    /** Call after [com.rebootech.fruitlogix.distributor.fleetManagement.domain.rules.AssignmentRules] accepted it. */
    /** @param routeKm road distance from the hub to the client; the server needs it to create the delivery. */
    suspend fun assign(orderId: String, driverId: String, vehicleId: String, routeKm: Double)

    /** Call after [com.rebootech.fruitlogix.distributor.fleetManagement.domain.rules.FleetResourceRules] accepted it. */
    suspend fun addDriver(draft: DriverDraft): Driver

    suspend fun addVehicle(draft: VehicleDraft): Vehicle

    /** Back from a trip or from resting: the driver (and the truck they had) can be assigned again. */
    suspend fun markDriverAvailable(driverId: String)

    /** Back from a trip or from the workshop. */
    suspend fun markVehicleAvailable(vehicleId: String)
}
