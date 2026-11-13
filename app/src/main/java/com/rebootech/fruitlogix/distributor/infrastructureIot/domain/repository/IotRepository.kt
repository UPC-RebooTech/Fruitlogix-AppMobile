package com.rebootech.fruitlogix.distributor.infrastructureIot.domain.repository

import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.FruitThreshold
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotAlert
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotDevice
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.TemperatureReading
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.ThresholdRules
import kotlinx.coroutines.flow.StateFlow

interface IotRepository {
    val devices: StateFlow<List<IotDevice>>
    val alerts: StateFlow<List<IotAlert>>
    val thresholds: StateFlow<List<FruitThreshold>>

    fun deviceForUnit(unitId: String): IotDevice?

    fun temperatureHistory(unitId: String): List<TemperatureReading>

    fun thresholdFor(productName: String): FruitThreshold?

    suspend fun calibrate(deviceId: String)

    suspend fun resolveAlert(alertId: String)

    suspend fun updateThreshold(
        fruitId: String,
        minTemp: Double,
        maxTemp: Double,
        minHum: Double,
        maxHum: Double
    ): ThresholdRules.Error?

    suspend fun refresh() {}
}