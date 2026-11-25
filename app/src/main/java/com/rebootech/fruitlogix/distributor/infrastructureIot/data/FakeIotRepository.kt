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

/**
 * Sensors, alerts and cold-chain limits. There is no sample data: devices and alerts stay empty until
 * the IoT endpoints (/io-t-devices, /alerts) are aligned with the app. The limits per fruit are the
 * usual cold-chain ranges, replaced by the server's /alert-rules when it has them.
 */
class FakeIotRepository : IotRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Readings by fleet unit. Empty until the IoT endpoints are aligned (no sample data). */
    private val history: Map<String, List<TemperatureReading>> = emptyMap()

    private val _devices = MutableStateFlow<List<IotDevice>>(emptyList())
    override val devices: StateFlow<List<IotDevice>> = _devices.asStateFlow()

    private val _alerts = MutableStateFlow<List<IotAlert>>(emptyList())
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
    }
}