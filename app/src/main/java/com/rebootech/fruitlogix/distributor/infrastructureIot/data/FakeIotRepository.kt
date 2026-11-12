package com.rebootech.fruitlogix.distributor.infrastructureIot.data

import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.AlertKind
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.AlertSeverity
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.DeviceStatus
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.FruitThreshold
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotAlert
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotDevice
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.SensorReading
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.TemperatureReading
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.ThresholdRules
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.repository.IotRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import kotlin.math.sin

/**
 * Sample sensors for the same trucks as Logistics (FL-102, FL-408, FL-219) plus two
 * sensors at the Lima Central Hub. Use the shared instance from DistributorDependencies.
 */
class FakeIotRepository : IotRepository {

    private val now = LocalDateTime.now()

    private val history: Map<String, List<TemperatureReading>> = mapOf(
        // Uva Red Globe (-1 … 2 °C): steady all trip
        "FL-102" to steady(fromMinutes = 360, base = 0.75, amplitude = 0.15) + TemperatureReading(0, 0.8),
        // Palta Hass (5 … 7 °C): fine for hours, then rising toward the limit in the last 45 min
        "FL-408" to steady(fromMinutes = 300, base = 5.2, amplitude = 0.15, until = 54) + listOf(
            TemperatureReading(45, 5.1),
            TemperatureReading(36, 5.3),
            TemperatureReading(27, 5.6),
            TemperatureReading(18, 5.9),
            TemperatureReading(9, 6.1),
            TemperatureReading(0, 6.4)
        ),
        // Mango Kent (10 … 13 °C): one short excursion during the weigh-station stop
        "FL-219" to (steady(fromMinutes = 600, base = 11.3, amplitude = 0.25) + TemperatureReading(0, 11.4))
            .map { if (it.minutesAgo == 240) it.copy(celsius = 13.4) else it }
    )

    private val _devices = MutableStateFlow(
        listOf(
            IotDevice("TH-408", unitId = "FL-408", location = null, status = DeviceStatus.ONLINE, batteryPercent = 64,
                lastReading = SensorReading(6.4, 88.0, now)),
            IotDevice("TH-102", unitId = "FL-102", location = null, status = DeviceStatus.ONLINE, batteryPercent = 81,
                lastReading = SensorReading(0.8, 92.0, now.minusMinutes(1))),
            IotDevice("TH-219", unitId = "FL-219", location = null, status = DeviceStatus.LOW_BATTERY, batteryPercent = 14,
                lastReading = SensorReading(11.4, 86.0, now.minusMinutes(2))),
            IotDevice("TH-09", unitId = null, location = "Lima Central Hub · Cámara 2", status = DeviceStatus.OFFLINE,
                batteryPercent = 37, lastReading = SensorReading(3.1, 90.0, now.minusMinutes(47))),
            IotDevice("ENV-08", unitId = null, location = "Lima Central Hub · Muelle B", status = DeviceStatus.ONLINE,
                batteryPercent = 96, lastReading = SensorReading(4.2, 80.0, now.minusMinutes(1)))
        )
    )
    override val devices: StateFlow<List<IotDevice>> = _devices.asStateFlow()

    private val _alerts = MutableStateFlow(
        listOf(
            IotAlert("alt-1", deviceId = "TH-408", unitId = "FL-408", kind = AlertKind.TEMP_RISK,
                severity = AlertSeverity.CRITICAL, createdAt = now.minusMinutes(10), value = 6.4, limit = 7.0),
            IotAlert("alt-2", deviceId = "TH-219", unitId = "FL-219", kind = AlertKind.LOW_BATTERY,
                severity = AlertSeverity.WARNING, createdAt = now.minusMinutes(25), value = 14.0),
            IotAlert("alt-3", deviceId = "TH-09", unitId = null, kind = AlertKind.SENSOR_OFFLINE,
                severity = AlertSeverity.WARNING, createdAt = now.minusMinutes(47))
        )
    )
    override val alerts: StateFlow<List<IotAlert>> = _alerts.asStateFlow()

    private val _thresholds = MutableStateFlow(
        listOf(
            FruitThreshold("fr-1", "Palta Hass", 5.0, 7.0, 85.0, 90.0),
            FruitThreshold("fr-2", "Uva Red Globe", -1.0, 2.0, 90.0, 95.0),
            FruitThreshold("fr-3", "Mango Kent", 10.0, 13.0, 85.0, 90.0),
            FruitThreshold("fr-4", "Arándano", 0.0, 2.0, 88.0, 95.0),
            FruitThreshold("fr-5", "Fresa", 0.0, 2.0, 90.0, 95.0)
        )
    )
    override val thresholds: StateFlow<List<FruitThreshold>> = _thresholds.asStateFlow()

    override fun deviceForUnit(unitId: String): IotDevice? = _devices.value.firstOrNull { it.unitId == unitId }

    override fun temperatureHistory(unitId: String): List<TemperatureReading> = history[unitId].orEmpty()

    override fun thresholdFor(productName: String): FruitThreshold? =
        _thresholds.value.firstOrNull { it.fruitName.equals(productName.trim(), ignoreCase = true) }

    override suspend fun calibrate(deviceId: String) {
        setStatus(deviceId, DeviceStatus.CALIBRATING)
        delay(CALIBRATION_MS)
        _devices.update { list ->
            list.map {
                if (it.id == deviceId) {
                    it.copy(
                        status = if (it.isLowBattery) DeviceStatus.LOW_BATTERY else DeviceStatus.ONLINE,
                        lastReading = it.lastReading.copy(at = LocalDateTime.now())
                    )
                } else it
            }
        }
    }

    private fun setStatus(deviceId: String, status: DeviceStatus) =
        _devices.update { list -> list.map { if (it.id == deviceId) it.copy(status = status) else it } }

    override suspend fun resolveAlert(alertId: String) {
        _alerts.update { list -> list.filterNot { it.id == alertId } }
    }

    override suspend fun updateThreshold(
        fruitId: String,
        minTemp: Double,
        maxTemp: Double,
        minHum: Double,
        maxHum: Double
    ): ThresholdRules.Error? {
        ThresholdRules.validate(minTemp, maxTemp, minHum, maxHum)?.let { return it }
        _thresholds.update { list ->
            list.map {
                if (it.id == fruitId) {
                    it.copy(minTempCelsius = minTemp, maxTempCelsius = maxTemp, minHumidityPercent = minHum, maxHumidityPercent = maxHum)
                } else it
            }
        }
        return null
    }

    private companion object {
        const val CALIBRATION_MS = 2_000L

        /** One reading every 10 minutes from [fromMinutes] ago until [until] minutes ago, with a gentle wave. */
        fun steady(fromMinutes: Int, base: Double, amplitude: Double, until: Int = 10): List<TemperatureReading> =
            (fromMinutes downTo until step 10).map { minutes ->
                val wave = sin(minutes / 37.0) * amplitude
                TemperatureReading(minutes, ((base + wave) * 10).toInt() / 10.0)
            }
    }
}
