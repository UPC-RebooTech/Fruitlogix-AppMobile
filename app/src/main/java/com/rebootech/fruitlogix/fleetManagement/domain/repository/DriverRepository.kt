package com.rebootech.fruitlogix.fleetManagement.domain.repository

import com.rebootech.fruitlogix.fleetManagement.domain.model.Driver
import kotlinx.coroutines.flow.Flow

interface DriverRepository {
    fun getDrivers(): Flow<List<Driver>>
}
