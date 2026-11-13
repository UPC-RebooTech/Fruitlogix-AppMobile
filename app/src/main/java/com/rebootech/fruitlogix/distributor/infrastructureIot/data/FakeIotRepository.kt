package com.rebootech.fruitlogix.distributor.infrastructureIot.data

import android.util.Log
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
import com.rebootech.fruitlogix.shared.core.network.CreateSensorReadingRequestDto
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import com.rebootech.fruitlogix.shared.core.network.SensorReadingResponseDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import kotlin.math.sin

class FakeIotRepository : IotRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val now = LocalDateTime.now()

    private val history: Map<String, List<TemperatureReading>> = mapOf(
        "FL-102" to steady(fromMinutes = 360, base = 0.75, amplitude = 0.15) + TemperatureReading(0, 0.8),
        "FL-408" to steady(fromMinutes = 300, base = 5.2, amplitude = 0.15, until = 54) + listOf(
            TemperatureReading(45, 5.1),
            TemperatureReading(36, 5.3),
            TemperatureReading(27, 5.6),
            TemperatureReading(18, 5.9),
            TemperatureReading(9, 6.1),
            TemperatureReading(0, 6.4)
        ),
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

    init {
        scope.launch { refresh() }
    }

    override fun deviceForUnit(unitId: String): IotDevice? = _devices.value.firstOrNull { it.unitId == unitId }

    override fun temperatureHistory(unitId: String): List<TemperatureReading> = history[unitId].orEmpty()

    override fun thresholdFor(productName: String): FruitThreshold? =
        _thresholds.value.firstOrNull { it.fruitName.equals(productName.trim(), ignoreCase = true) }

    override suspend fun calibrate(deviceId: String) {
        setStatus(deviceId, DeviceStatus.CALIBRATING)
        delay(CALIBRATION_MS)
        var calibratedReading: SensorReading? = null
        _devices.update { list ->
            list.map {
                if (it.id == deviceId) {
                    val updatedReading = it.lastReading.copy(at = LocalDateTime.now())
                    calibratedReading = updatedReading
                    it.copy(
                        status = if (it.isLowBattery) DeviceStatus.LOW_BATTERY else DeviceStatus.ONLINE,
                        lastReading = updatedReading
                    )
                } else it
            }
        }
        // Registrar lectura de verificación post-calibración usando el contrato real (deviceId, value, unit, timestamp)
        calibratedReading?.let { reading ->
            scope.launch {
                runCatching {
                    RetrofitClient.api.createSensorReading(
                        CreateSensorReadingRequestDto(
                            deviceId = deviceId,
                            value = reading.celsius,
                            unit = "C",
                            timestamp = reading.at.toString()
                        )
                    )
                }
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

    override suspend fun refresh() {
        withContext(Dispatchers.IO) {
            // 1. Consultar lecturas de sensores (deviceId, value, unit, timestamp)
            runCatching { RetrofitClient.api.getSensorReadings() }.onSuccess { response ->
                if (response.isSuccessful) {
                    val readings = response.body().orEmpty()
                    if (readings.isNotEmpty()) {
                        applyRemoteSensorReadings(readings)
                    }
                }
            }.onFailure { err ->
                Log.w("FruitLogix-API", "GET /sensor-readings no disponible: ${err.message}")
            }

            // 2. Consultar reglas de alerta por fruta (AlertRules)
            runCatching { RetrofitClient.api.getAlertRules() }.onSuccess { response ->
                if (response.isSuccessful) {
                    val rules = response.body().orEmpty()
                    val mappedRules = rules.mapIndexedNotNull { idx, dto ->
                        val name = dto.fruitName?.takeIf { it.isNotBlank() } ?: return@mapIndexedNotNull null
                        FruitThreshold(
                            id = "fr-api-${dto.id ?: idx}",
                            fruitName = name,
                            minTempCelsius = dto.minTemperature ?: 0.0,
                            maxTempCelsius = dto.maxTemperature ?: 10.0,
                            minHumidityPercent = dto.minHumidity ?: 85.0,
                            maxHumidityPercent = dto.maxHumidity ?: 95.0
                        )
                    }
                    if (mappedRules.isNotEmpty()) {
                        _thresholds.update { current ->
                            (mappedRules + current).distinctBy { it.fruitName.lowercase() }
                        }
                    }
                }
            }
        }
    }

    private fun applyRemoteSensorReadings(dtos: List<SensorReadingResponseDto>) {
        val grouped = dtos.filter { !it.deviceId.isNullOrBlank() && it.value != null }
            .groupBy { it.deviceId!! }

        _devices.update { currentDevices ->
            currentDevices.map { dev ->
                val devReadings = grouped[dev.id].orEmpty()
                if (devReadings.isEmpty()) {
                    dev
                } else {
                    val tempValue = devReadings.lastOrNull {
                        val u = it.unit.orEmpty().uppercase()
                        u == "C" || u == "°C" || u == "CELSIUS"
                    }?.value ?: dev.lastReading.celsius

                    val humValue = devReadings.lastOrNull {
                        val u = it.unit.orEmpty().uppercase()
                        u == "%" || u == "RH" || u == "HUMIDITY"
                    }?.value ?: dev.lastReading.humidityPercent

                    dev.copy(
                        lastReading = SensorReading(
                            celsius = tempValue,
                            humidityPercent = humValue,
                            at = LocalDateTime.now()
                        )
                    )
                }
            }
        }
    }

    private companion object {
        const val CALIBRATION_MS = 2_000L

        fun steady(fromMinutes: Int, base: Double, amplitude: Double, until: Int = 10): List<TemperatureReading> =
            (fromMinutes downTo until step 10).map { minutes ->
                val wave = sin(minutes / 37.0) * amplitude
                TemperatureReading(minutes, ((base + wave) * 10).toInt() / 10.0)
            }
    }
}