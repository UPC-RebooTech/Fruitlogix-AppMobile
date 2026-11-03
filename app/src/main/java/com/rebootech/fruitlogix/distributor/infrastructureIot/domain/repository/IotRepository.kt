package com.rebootech.fruitlogix.distributor.infrastructureIot.domain.repository

import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotAlert
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotDevice
import kotlinx.coroutines.flow.Flow

interface IotRepository {
    fun getDevices(): Flow<List<IotDevice>>
    fun getActiveAlerts(): Flow<List<IotAlert>>
    suspend fun calibrateDevice(deviceId: String)
    suspend fun dismissAlert(alertId: String)
}