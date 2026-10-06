package com.rebootech.fruitlogix.infrastructureIot.data

import com.rebootech.fruitlogix.infrastructureIot.domain.model.AlertSeverity
import com.rebootech.fruitlogix.infrastructureIot.domain.model.DeviceConnectionStatus
import com.rebootech.fruitlogix.infrastructureIot.domain.model.IotAlert
import com.rebootech.fruitlogix.infrastructureIot.domain.model.IotDevice
import com.rebootech.fruitlogix.infrastructureIot.domain.model.SensorReading
import com.rebootech.fruitlogix.infrastructureIot.domain.repository.IotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeIotRepository : IotRepository {

    private val _devices = MutableStateFlow(
        listOf(
            IotDevice(
                id = "dev-101",
                deviceCode = "SENSOR-RF-01",
                vehicleId = "Furgón A3-Huaral",
                connectionStatus = DeviceConnectionStatus.CONNECTED,
                batteryPercent = 88,
                lastReading = SensorReading(temperatureCelsius = 4.8, humidityPercent = 85.0)
            ),
            IotDevice(
                id = "dev-102",
                deviceCode = "SENSOR-RF-02",
                vehicleId = "Camión B1-Cañete",
                connectionStatus = DeviceConnectionStatus.CONNECTED,
                batteryPercent = 74,
                lastReading = SensorReading(temperatureCelsius = 9.4, humidityPercent = 91.5)
            ),
            IotDevice(
                id = "dev-103",
                deviceCode = "SENSOR-RF-03",
                vehicleId = "Camioneta C4-Chanchamayo",
                connectionStatus = DeviceConnectionStatus.DISCONNECTED,
                batteryPercent = 12,
                lastReading = SensorReading(temperatureCelsius = 14.1, humidityPercent = 65.0)
            )
        )
    )

    private val _alerts = MutableStateFlow(
        listOf(
            IotAlert(
                id = "alt-01",
                deviceId = "dev-102",
                vehicleCode = "Camión B1-Cañete",
                message = "Ruptura de cadena de frío: 9.4°C supera el umbral máximo de 6.0°C para arándanos.",
                severity = AlertSeverity.CRITICAL,
                timestamp = "Hace 10 min"
            ),
            IotAlert(
                id = "alt-02",
                deviceId = "dev-103",
                vehicleCode = "Camioneta C4-Chanchamayo",
                message = "Pérdida de señal de telemetría y batería baja (<15%).",
                severity = AlertSeverity.WARNING,
                timestamp = "Hace 35 min"
            )
        )
    )

    override fun getDevices(): Flow<List<IotDevice>> = _devices.asStateFlow()

    override fun getActiveAlerts(): Flow<List<IotAlert>> = _alerts.asStateFlow()

    override suspend fun calibrateDevice(deviceId: String) {
        _devices.update { current ->
            current.map { device ->
                if (device.id == deviceId) {
                    device.copy(connectionStatus = DeviceConnectionStatus.CALIBRATING)
                } else device
            }
        }
    }

    override suspend fun dismissAlert(alertId: String) {
        _alerts.update { current ->
            current.filterNot { it.id == alertId }
        }
    }
}