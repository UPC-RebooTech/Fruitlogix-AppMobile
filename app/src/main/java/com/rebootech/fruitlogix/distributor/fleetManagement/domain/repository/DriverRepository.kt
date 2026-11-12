package com.rebootech.fruitlogix.distributor.fleetManagement.domain.repository

import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Driver
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.PendingDispatch
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Vehicle
import kotlinx.coroutines.flow.StateFlow

interface DriverRepository {
    val drivers: StateFlow<List<Driver>>
    val vehicles: StateFlow<List<Vehicle>>
    /** Orders ready to leave without a truck yet. */
    val pendingDispatches: StateFlow<List<PendingDispatch>>

    /** Call after [com.rebootech.fruitlogix.distributor.fleetManagement.domain.rules.AssignmentRules] accepted it. */
    suspend fun assign(orderId: String, driverId: String, vehicleId: String)
}
