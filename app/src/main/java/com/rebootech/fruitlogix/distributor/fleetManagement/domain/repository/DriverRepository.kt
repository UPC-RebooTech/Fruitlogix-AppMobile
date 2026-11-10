package com.rebootech.fruitlogix.distributor.fleetManagement.domain.repository

import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Driver
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.PendingDispatchOption
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow

interface DriverRepository {
    fun getDrivers(): Flow<List<Driver>>
    fun getVehicles(): Flow<List<Vehicle>>
    fun getPendingDispatches(): Flow<List<PendingDispatchOption>>
    fun assignResourcesToDispatch(
        dispatchId: String,
        driverId: String,
        vehicleId: String
    ): Result<String>
}