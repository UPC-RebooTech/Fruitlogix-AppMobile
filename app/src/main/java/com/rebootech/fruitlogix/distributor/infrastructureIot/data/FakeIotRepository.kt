package com.rebootech.fruitlogix.distributor.infrastructureIot.data

import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.AlertSeverity
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.DeviceConnectionStatus
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.FruitThreshold
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotAlert
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotDevice
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.SensorReading
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.repository.IotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeIotRepository : IotRepository {

    companion object {
        private val _devices = MutableStateFlow(
            listOf(
                IotDevice(
                    id = "dev-103",
                    deviceCode = "SENSOR-RF-03",
                    vehicleId = "Camioneta C4-Chanchamayo",
                    connectionStatus = DeviceConnectionStatus.DISCONNECTED,
                    batteryPercent = 12,
                    lastReading = SensorReading(temperatureCelsius = 14.1, humidityPercent = 65.0),
                    lastSyncLabel = "Sin señal • Última lectura hace 35 min"
                ),
                IotDevice(
                    id = "dev-102",
                    deviceCode = "SENSOR-RF-02",
                    vehicleId = "Camión B1-Cañete",
                    connectionStatus = DeviceConnectionStatus.CONNECTED,
                    batteryPercent = 74,
                    lastReading = SensorReading(temperatureCelsius = 9.4, humidityPercent = 91.5),
                    lastSyncLabel = "En línea • Hace 1 min"
                ),
                IotDevice(
                    id = "dev-101",
                    deviceCode = "SENSOR-RF-01",
                    vehicleId = "Furgón A3-Huaral",
                    connectionStatus = DeviceConnectionStatus.CONNECTED,
                    batteryPercent = 88,
                    lastReading = SensorReading(temperatureCelsius = 4.8, humidityPercent = 85.0),
                    lastSyncLabel = "En línea • Ahora"
                )
            )
        )

        private val _alerts = MutableStateFlow(
            listOf(
                IotAlert(
                    id = "alt-01",
                    deviceId = "dev-102",
                    vehicleCode = "Camión B1-Cañete",
                    message = "Ruptura de cadena de frío: 9.4°C supera el umbral máximo configurado para Arándano Biloxi.",
                    severity = AlertSeverity.CRITICAL,
                    timestamp = "Hace 10 min"
                ),
                IotAlert(
                    id = "alt-02",
                    deviceId = "dev-103",
                    vehicleCode = "Camioneta C4-Chanchamayo",
                    message = "Sin señal de telemetría y batería en nivel crítico (12%). Atención inmediata.",
                    severity = AlertSeverity.WARNING,
                    timestamp = "Hace 35 min"
                )
            )
        )

        private val _thresholds = MutableStateFlow(
            listOf(
                FruitThreshold("fr-1", "Palta Hass", 4.0, 7.0, 85.0, 90.0),
                FruitThreshold("fr-2", "Arándano Biloxi", 0.0, 4.0, 88.0, 95.0),
                FruitThreshold("fr-3", "Mango Kent", 8.0, 12.0, 85.0, 90.0),
                FruitThreshold("fr-4", "Uva Red Globe", -1.0, 2.0, 90.0, 95.0)
            )
        )
    }

    override fun getDevices(): Flow<List<IotDevice>> = _devices.asStateFlow()

    override fun getActiveAlerts(): Flow<List<IotAlert>> = _alerts.asStateFlow()

    override fun getFruitThresholds(): Flow<List<FruitThreshold>> = _thresholds.asStateFlow()

    override suspend fun calibrateDevice(deviceId: String) {
        _devices.update { current ->
            current.map { device ->
                if (device.id == deviceId) {
                    device.copy(
                        connectionStatus = DeviceConnectionStatus.CALIBRATING,
                        lastSyncLabel = "Calibrando sensor..."
                    )
                } else device
            }
        }
    }

    override suspend fun dismissAlert(alertId: String) {
        _alerts.update { current ->
            current.filterNot { it.id == alertId }
        }
    }

    override fun updateFruitThreshold(
        fruitId: String,
        minTemp: Double,
        maxTemp: Double,
        minHum: Double,
        maxHum: Double
    ): Result<FruitThreshold> {
        if (minTemp < -5.0 || maxTemp > 30.0 || minTemp >= maxTemp) {
            return Result.failure(
                IllegalArgumentException("Rango térmico inválido: Mínimo debe ser menor al máximo y estar entre -5.0°C y 30.0°C.")
            )
        }
        if (minHum < 40.0 || maxHum > 100.0 || minHum >= maxHum) {
            return Result.failure(
                IllegalArgumentException("Rango de humedad inválido: Mínimo debe ser menor al máximo y estar entre 40% y 100%.")
            )
        }

        var updatedItem: FruitThreshold? = null
        _thresholds.update { list ->
            list.map { item ->
                if (item.id == fruitId) {
                    item.copy(
                        minTempCelsius = minTemp,
                        maxTempCelsius = maxTemp,
                        minHumidityPercent = minHum,
                        maxHumidityPercent = maxHum
                    ).also { updatedItem = it }
                } else item
            }
        }

        return updatedItem?.let { Result.success(it) }
            ?: Result.failure(IllegalArgumentException("Fruta no encontrada."))
    }
}