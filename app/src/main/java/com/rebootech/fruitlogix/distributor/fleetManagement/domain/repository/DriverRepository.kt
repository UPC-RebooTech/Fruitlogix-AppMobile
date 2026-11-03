package com.rebootech.fruitlogix.distributor.fleetManagement.domain.repository

import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Driver
import kotlinx.coroutines.flow.Flow

interface DriverRepository {
    fun getDrivers(): Flow<List<Driver>>
}
