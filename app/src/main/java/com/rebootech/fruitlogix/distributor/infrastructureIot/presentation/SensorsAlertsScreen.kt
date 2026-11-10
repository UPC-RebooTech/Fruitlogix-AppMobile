package com.rebootech.fruitlogix.distributor.infrastructureIot.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.AlertSeverity
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.DeviceConnectionStatus
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.FruitThreshold
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotAlert
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotDevice
import com.rebootech.fruitlogix.shared.ui.theme.ColorAppbar
import com.rebootech.fruitlogix.shared.ui.theme.ColorBg
import com.rebootech.fruitlogix.shared.ui.theme.ColorDanger
import com.rebootech.fruitlogix.shared.ui.theme.ColorDangerStrong
import com.rebootech.fruitlogix.shared.ui.theme.ColorOnPrimary
import com.rebootech.fruitlogix.shared.ui.theme.ColorPrimary
import com.rebootech.fruitlogix.shared.ui.theme.ColorSuccess
import com.rebootech.fruitlogix.shared.ui.theme.ColorSurfaceDark
import com.rebootech.fruitlogix.shared.ui.theme.ColorTextMuted
import com.rebootech.fruitlogix.shared.ui.theme.ColorTextOnDark
import com.rebootech.fruitlogix.shared.ui.theme.ColorTextOnLight
import com.rebootech.fruitlogix.shared.ui.theme.ColorWarning

@Composable
fun SensorsAlertsScreen(
    modifier: Modifier = Modifier,
    viewModel: SensorsAlertsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize().background(ColorBg), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ColorPrimary)
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ColorBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = stringResource(id = R.string.screen_iot_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextOnLight
            )
            Text(
                text = "Monitoreo telemático, estado de baterías y umbrales por fruta",
                fontSize = 12.sp,
                color = ColorAppbar
            )
        }

        uiState.feedbackMessage?.let { msg ->
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ColorSurfaceDark)
                        .border(1.dp, ColorPrimary, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = msg,
                        color = ColorPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = viewModel::clearFeedback) {
                        Text("OK", color = ColorTextOnDark, fontSize = 12.sp)
                    }
                }
            }
        }

        // Sección 1: Umbrales por Tipo de Fruta (US62)
        item {
            Text(
                text = "Umbrales de Conservación por Fruta (US62)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextOnLight
            )
        }

        items(uiState.thresholds, key = { it.id }) { threshold ->
            FruitThresholdCard(
                threshold = threshold,
                onEditClick = { viewModel.onSelectThresholdToEdit(threshold) }
            )
        }

        // Sección 2: Alertas Telemáticas Activas (US18 / US44)
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.iot_alerts_section),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ColorDanger
            )
        }

        if (uiState.alerts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark)
                ) {
                    Text(
                        text = stringResource(id = R.string.iot_no_alerts),
                        modifier = Modifier.padding(16.dp),
                        color = ColorSuccess,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
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

        // Sección 3: Estado de Sensores y Batería (US45 / US63)
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.iot_devices_section),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextOnLight
            )
        }

        items(uiState.devices, key = { it.id }) { device ->
            IotDeviceCard(
                device = device,
                onCalibrate = { viewModel.onCalibrateDevice(device.id) }
            )
        }
    }

    // Modal para configurar umbrales por fruta (US62)
    uiState.editingThreshold?.let { threshold ->
        EditThresholdDialog(
            threshold = threshold,
            errorMessage = uiState.thresholdError,
            onDismiss = viewModel::onDismissThresholdDialog,
            onSave = { minT, maxT, minH, maxH ->
                viewModel.onSaveThreshold(threshold.id, minT, maxT, minH, maxH)
            }
        )
    }
}

@Composable
private fun FruitThresholdCard(
    threshold: FruitThreshold,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = threshold.fruitName,
                    color = ColorTextOnDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Temp: ${threshold.minTempCelsius}°C a ${threshold.maxTempCelsius}°C • Humedad: ${threshold.minHumidityPercent.toInt()}% a ${threshold.maxHumidityPercent.toInt()}%",
                    color = ColorPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Button(
                onClick = onEditClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary,
                    contentColor = ColorOnPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Configurar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun EditThresholdDialog(
    threshold: FruitThreshold,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (minTemp: String, maxTemp: String, minHum: String, maxHum: String) -> Unit
) {
    var minTemp by remember(threshold) { mutableStateOf(threshold.minTempCelsius.toString()) }
    var maxTemp by remember(threshold) { mutableStateOf(threshold.maxTempCelsius.toString()) }
    var minHum by remember(threshold) { mutableStateOf(threshold.minHumidityPercent.toString()) }
    var maxHum by remember(threshold) { mutableStateOf(threshold.maxHumidityPercent.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorSurfaceDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Umbrales: ${threshold.fruitName}",
                color = ColorTextOnDark,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Define los rangos permitidos (-5°C a 30°C y 40% a 100% RH).",
                    color = ColorTextMuted,
                    fontSize = 12.sp
                )
                errorMessage?.let { err ->
                    Text(
                        text = err,
                        color = ColorDangerStrong,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThresholdTextField(
                        label = "Temp Mín (°C)",
                        value = minTemp,
                        onValueChange = { minTemp = it },
                        modifier = Modifier.weight(1f)
                    )
                    ThresholdTextField(
                        label = "Temp Máx (°C)",
                        value = maxTemp,
                        onValueChange = { maxTemp = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThresholdTextField(
                        label = "Hum Mín (%)",
                        value = minHum,
                        onValueChange = { minHum = it },
                        modifier = Modifier.weight(1f)
                    )
                    ThresholdTextField(
                        label = "Hum Máx (%)",
                        value = maxHum,
                        onValueChange = { maxHum = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(minTemp, maxTemp, minHum, maxHum) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary,
                    contentColor = ColorOnPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar Umbral", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = ColorTextMuted)
            }
        }
    )
}

@Composable
private fun ThresholdTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = ColorTextMuted, fontSize = 11.sp) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ColorTextOnDark,
            unfocusedTextColor = ColorTextOnDark,
            focusedBorderColor = ColorPrimary,
            unfocusedBorderColor = ColorAppbar,
            focusedContainerColor = ColorAppbar,
            unfocusedContainerColor = ColorAppbar
        ),
        modifier = modifier
    )
}

@Composable
fun IotAlertCard(
    alert: IotAlert,
    onDismiss: () -> Unit
) {
    val borderColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> ColorDangerStrong
        AlertSeverity.WARNING -> ColorWarning
        AlertSeverity.INFO -> ColorPrimary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark)
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
                    color = borderColor,
                    fontSize = 14.sp
                )
                Text(
                    text = alert.timestamp,
                    fontSize = 11.sp,
                    color = ColorTextMuted
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = alert.message,
                fontSize = 13.sp,
                color = ColorTextOnDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Resolver Incidencia", color = ColorPrimary, fontSize = 12.sp)
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
    val isLowBatteryOrOffline =
        device.connectionStatus == DeviceConnectionStatus.DISCONNECTED || device.batteryPercent < 20
    val tempExceeded = device.lastReading.temperatureCelsius > 8.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isLowBatteryOrOffline) {
                    Modifier.border(1.dp, ColorDangerStrong, RoundedCornerShape(14.dp))
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = device.deviceCode,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextOnDark,
                            fontSize = 15.sp
                        )
                        if (isLowBatteryOrOffline) {
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ColorDanger)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ATENCIÓN INMEDIATA",
                                    color = ColorDangerStrong,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text = "${device.vehicleId} • ${device.lastSyncLabel}",
                        fontSize = 11.sp,
                        color = ColorTextMuted
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
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (tempExceeded) ColorDangerStrong else ColorSuccess
                )
                Text(
                    text = "Humedad: ${device.lastReading.humidityPercent}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ColorTextOnDark
                )
                Text(
                    text = "Batería: ${device.batteryPercent}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (device.batteryPercent < 20) ColorDangerStrong else ColorPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onCalibrate,
                modifier = Modifier.fillMaxWidth(),
                enabled = device.connectionStatus != DeviceConnectionStatus.CALIBRATING,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary,
                    contentColor = ColorOnPrimary
                )
            ) {
                Text(
                    text = if (device.connectionStatus == DeviceConnectionStatus.CALIBRATING)
                        stringResource(R.string.iot_device_calibrating)
                    else
                        stringResource(R.string.iot_calibrate_action),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: DeviceConnectionStatus) {
    val (bgColor, textColor, label) = when (status) {
        DeviceConnectionStatus.CONNECTED -> Triple(ColorSuccess.copy(alpha = 0.2f), ColorSuccess, "En línea")
        DeviceConnectionStatus.CALIBRATING -> Triple(ColorWarning.copy(alpha = 0.2f), ColorWarning, "Calibrando")
        DeviceConnectionStatus.DISCONNECTED -> Triple(ColorDangerStrong.copy(alpha = 0.2f), ColorDangerStrong, "Sin señal")
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = label, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}