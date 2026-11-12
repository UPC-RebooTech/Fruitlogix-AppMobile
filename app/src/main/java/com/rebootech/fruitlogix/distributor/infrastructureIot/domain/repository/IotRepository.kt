package com.rebootech.fruitlogix.distributor.infrastructureIot.domain.repository

import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.FruitThreshold
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotAlert
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotDevice
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.TemperatureReading
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.ThresholdRules
import kotlinx.coroutines.flow.StateFlow

/**
 * Sensors, alerts and fruit limits. Other contexts (Logistics, Home) read the fleet's
 * temperatures only through this interface.
 */
interface IotRepository {
    val devices: StateFlow<List<IotDevice>>
    val alerts: StateFlow<List<IotAlert>>
    val thresholds: StateFlow<List<FruitThreshold>>

    fun deviceForUnit(unitId: String): IotDevice?

    /** Trip readings of a truck, oldest first ([TemperatureReading.minutesAgo] decreasing). */
    fun temperatureHistory(unitId: String): List<TemperatureReading>

    fun thresholdFor(productName: String): FruitThreshold?

    /** Sets the sensor to calibrating and back online when it finishes. */
    suspend fun calibrate(deviceId: String)

    suspend fun resolveAlert(alertId: String)

    /** @return null when saved, otherwise why it was refused. */
    suspend fun updateThreshold(
        fruitId: String,
        minTemp: Double,
        maxTemp: Double,
        minHum: Double,
        maxHum: Double
    ): ThresholdRules.Error?
}
