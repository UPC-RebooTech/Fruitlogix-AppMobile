package com.rebootech.fruitlogix.distributor.infrastructureIot.domain.repository

import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.FruitThreshold
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotAlert
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotDevice
import kotlinx.coroutines.flow.Flow

interface IotRepository {
    fun getDevices(): Flow<List<IotDevice>>
    fun getActiveAlerts(): Flow<List<IotAlert>>
    fun getFruitThresholds(): Flow<List<FruitThreshold>>
    suspend fun calibrateDevice(deviceId: String)
    suspend fun dismissAlert(alertId: String)
    fun updateFruitThreshold(
        fruitId: String,
        minTemp: Double,
        maxTemp: Double,
        minHum: Double,
        maxHum: Double
    ): Result<FruitThreshold>
}