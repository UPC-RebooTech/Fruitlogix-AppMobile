package com.rebootech.fruitlogix.infrastructureIot.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.infrastructureIot.domain.model.AlertSeverity
import com.rebootech.fruitlogix.infrastructureIot.domain.model.DeviceConnectionStatus
import com.rebootech.fruitlogix.infrastructureIot.domain.model.IotAlert
import com.rebootech.fruitlogix.infrastructureIot.domain.model.IotDevice

@Composable
fun SensorsAlertsScreen(
    modifier: Modifier = Modifier,
    viewModel: SensorsAlertsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.screen_iot_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Sección 1: Alertas Telemáticas Activas
        item {
            Text(
                text = stringResource(id = R.string.iot_alerts_section),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (uiState.alerts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = stringResource(id = R.string.iot_no_alerts),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(uiState.alerts, key = { it.id }) { alert ->
                IotAlertCard(
                    alert = alert,
                    onDismiss = { viewModel.onDismissAlert(alert.id) }
                )
            }
        }

        // Sección 2: Dispositivos IoT Activos
        item {
            Text(
                text = stringResource(id = R.string.iot_devices_section),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(uiState.devices, key = { it.id }) { device ->
            IotDeviceCard(
                device = device,
                onCalibrate = { viewModel.onCalibrateDevice(device.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun IotAlertCard(
    alert: IotAlert,
    onDismiss: () -> Unit
) {
    val containerColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> Color(0xFFFFEBEE)
        AlertSeverity.WARNING -> Color(0xFFFFF8E1)
        AlertSeverity.INFO -> Color(0xFFE3F2FD)
    }
    val contentColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> Color(0xFFC62828)
        AlertSeverity.WARNING -> Color(0xFFF57F17)
        AlertSeverity.INFO -> Color(0xFF1565C0)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alert.vehicleCode,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = alert.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Descartar", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun IotDeviceCard(
    device: IotDevice,
    onCalibrate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = device.deviceCode,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = device.vehicleId,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                StatusChip(status = device.connectionStatus)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Temp: ${device.lastReading.temperatureCelsius}°C",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Humedad: ${device.lastReading.humidityPercent}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Bat: ${device.batteryPercent}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (device.batteryPercent < 20) Color.Red else Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onCalibrate,
                modifier = Modifier.fillMaxWidth(),
                enabled = device.connectionStatus != DeviceConnectionStatus.CALIBRATING,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = if (device.connectionStatus == DeviceConnectionStatus.CALIBRATING)
                        stringResource(R.string.iot_device_calibrating)
                    else
                        stringResource(R.string.iot_calibrate_action)
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: DeviceConnectionStatus) {
    val (bgColor, textColor, label) = when (status) {
        DeviceConnectionStatus.CONNECTED -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Conectado")
        DeviceConnectionStatus.CALIBRATING -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "Calibrando")
        DeviceConnectionStatus.DISCONNECTED -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Desconectado")
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = label, color = textColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}