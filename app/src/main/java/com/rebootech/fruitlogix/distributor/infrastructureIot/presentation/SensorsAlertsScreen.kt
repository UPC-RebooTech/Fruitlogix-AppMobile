package com.rebootech.fruitlogix.distributor.infrastructureIot.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.AlertKind
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.AlertSeverity
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.DeviceStatus
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.FruitThreshold
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotAlert
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotDevice
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.LabeledTextField
import com.rebootech.fruitlogix.shared.ui.components.SectionHeader
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.format.formatDecimal
import com.rebootech.fruitlogix.shared.ui.format.formatTimeAgo
import com.rebootech.fruitlogix.shared.ui.components.whiteCard
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Sensors and limits. Same structure as before (alerts, sensors, limits per fruit),
 * now in the design system and in both languages. Alerts come first: they need action.
 */
@Composable
fun SensorsAlertsScreen(
    onBack: () -> Unit,
    onOpenUnit: (unitId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SensorsAlertsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val snackbar = remember { SnackbarHostState() }
    val savedMessage = state.savedFruit?.let { stringResource(R.string.sensors_saved, it) }

    LaunchedEffect(state.savedFruit) {
        if (savedMessage != null) {
            snackbar.showSnackbar(savedMessage)
            viewModel.onSavedShown()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(scheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = spacing.sm, end = spacing.sm, top = spacing.sm, bottom = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.inner)
        ) {
            item {
                BackHeader(
                    title = stringResource(R.string.sensors_title),
                    subtitle = stringResource(R.string.sensors_subtitle),
                    onBack = onBack
                )
            }
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    StatusBadge(text = stringResource(R.string.sensors_online, state.online), type = StatusBadgeType.SUCCESS, maxLines = 1, softWrap = false)
                    StatusBadge(text = stringResource(R.string.sensors_low_battery, state.lowBattery), type = StatusBadgeType.WARNING, maxLines = 1, softWrap = false)
                    StatusBadge(text = stringResource(R.string.sensors_offline, state.offline), type = StatusBadgeType.DANGER, maxLines = 1, softWrap = false)
                }
            }

            // 1. Active alerts (US18 / US44)
            item { SectionHeader(title = stringResource(R.string.sensors_section_alerts)) }
            if (state.alerts.isEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(FruitLogixTheme.shapes.Card)
                            .background(FruitLogixTheme.colors.successContainer)
                            .padding(spacing.inner),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIcon(id = R.drawable.ic_verified, contentDescription = null, tint = FruitLogixTheme.colors.successContent, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
                        Spacer(modifier = Modifier.width(spacing.xs))
                        Text(stringResource(R.string.fleet_alerts_empty), style = FruitLogixTheme.typography.bodyMedium, color = FruitLogixTheme.colors.onSuccessContainer)
                    }
                }
            } else {
                items(state.alerts, key = { it.id }) { alert ->
                    AlertRow(
                        alert = alert,
                        onOpen = { alert.unitId?.let(onOpenUnit) },
                        onResolve = { viewModel.resolveAlert(alert.id) }
                    )
                }
            }

            // 2. Sensors and battery (US45 / US63)
            item { SectionHeader(title = stringResource(R.string.sensors_section_devices)) }
            items(state.devices, key = { it.id }) { device ->
                DeviceCard(device = device, onCalibrate = { viewModel.calibrate(device.id) })
            }

            // 3. Limits per fruit (US62)
            item {
                SectionHeader(
                    title = stringResource(R.string.sensors_section_limits),
                    subtitle = stringResource(R.string.sensors_section_limits_sub)
                )
            }
            items(state.thresholds, key = { it.id }) { threshold ->
                ThresholdRow(threshold = threshold, onEdit = { viewModel.edit(threshold) })
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(spacing.sm)
        )
    }

    state.editing?.let { threshold ->
        EditThresholdDialog(
            threshold = threshold,
            error = state.editError?.let { stringResource(it) },
            onSave = viewModel::save,
            onDismiss = viewModel::cancelEdit
        )
    }
}

@Composable
private fun celsius(value: Double) = "${formatDecimal(value)} °C"

@Composable
private fun AlertRow(alert: IotAlert, onOpen: () -> Unit, onResolve: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val critical = alert.severity == AlertSeverity.CRITICAL
    val subject = alert.unitId ?: alert.deviceId
    val title = when (alert.kind) {
        AlertKind.TEMP_RISK -> stringResource(R.string.fleet_alert_temp_risk, subject, alert.limit?.let { celsius(it) } ?: "—")
        AlertKind.TEMP_OUT_OF_RANGE -> stringResource(R.string.fleet_alert_temp_out, subject, alert.value?.let { celsius(it) } ?: "—")
        AlertKind.SENSOR_OFFLINE -> stringResource(R.string.fleet_alert_offline, alert.deviceId)
        AlertKind.LOW_BATTERY -> stringResource(R.string.fleet_alert_low_battery, alert.deviceId, alert.value?.toInt() ?: 0)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FruitLogixTheme.shapes.Card)
            .background(if (critical) scheme.errorContainer else scheme.surfaceContainerLowest)
            .border(1.dp, scheme.outlineVariant, FruitLogixTheme.shapes.Card)
            .clickable(enabled = alert.unitId != null, role = Role.Button, onClick = onOpen)
            .padding(FruitLogixTheme.spacing.inner)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            AppIcon(
                id = if (critical) R.drawable.ic_warning else R.drawable.ic_alert,
                contentDescription = null,
                tint = if (critical) scheme.error else FruitLogixTheme.colors.attentionContent,
                modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
            )
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = FruitLogixTheme.typography.titleSmall, color = if (critical) scheme.onErrorContainer else scheme.onSurface)
                Text(
                    text = stringResource(R.string.fleet_alert_where, alert.deviceId, formatTimeAgo(alert.createdAt)),
                    style = FruitLogixTheme.typography.bodySmall,
                    color = if (critical) scheme.onErrorContainer else scheme.onSurfaceVariant
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextActionButton(text = stringResource(R.string.fleet_alert_resolve), onClick = onResolve)
            if (alert.unitId != null) TextActionButton(text = stringResource(R.string.fleet_alert_open_trip), onClick = onOpen)
        }
    }
}

@Composable
private fun DeviceCard(device: IotDevice, onCalibrate: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val (statusText, statusType) = when (device.status) {
        DeviceStatus.ONLINE -> stringResource(R.string.sensors_status_online) to StatusBadgeType.SUCCESS
        DeviceStatus.LOW_BATTERY -> stringResource(R.string.sensors_status_low_battery) to StatusBadgeType.WARNING
        DeviceStatus.OFFLINE -> stringResource(R.string.sensors_status_offline) to StatusBadgeType.DANGER
        DeviceStatus.CALIBRATING -> stringResource(R.string.sensors_status_calibrating) to StatusBadgeType.INFO
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .whiteCard(FruitLogixTheme.shapes.Card)
            .padding(FruitLogixTheme.spacing.inner)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(FruitLogixTheme.sizing.iconBox)
                    .clip(CircleShape)
                    .background(scheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    id = if (device.unitId != null) R.drawable.ic_fleet_truck else R.drawable.ic_warehouse,
                    contentDescription = null,
                    tint = scheme.onSecondaryContainer,
                    modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                )
            }
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
            Column(modifier = Modifier.weight(1f)) {
                Text(device.id, style = FruitLogixTheme.extraTypography.dataMono, color = scheme.onSurface)
                Text(
                    text = device.unitId?.let { stringResource(R.string.sensors_on_unit, it) } ?: device.location.orEmpty(),
                    style = FruitLogixTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }
            StatusBadge(text = statusText, type = statusType, maxLines = 1, softWrap = false)
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        Text(
            text = stringResource(R.string.sensors_reading, celsius(device.lastReading.celsius), device.lastReading.humidityPercent.toInt()),
            style = FruitLogixTheme.typography.titleSmall,
            color = scheme.onSurface
        )
        Text(
            text = "${stringResource(R.string.sensors_updated, formatTimeAgo(device.lastReading.at))} · ${stringResource(R.string.sensors_battery, device.batteryPercent)}",
            style = FruitLogixTheme.typography.bodySmall,
            color = if (device.isLowBattery) FruitLogixTheme.colors.attentionContent else scheme.onSurfaceVariant
        )
        if (device.status != DeviceStatus.CALIBRATING) {
            TextActionButton(
                text = stringResource(R.string.sensors_calibrate),
                onClick = onCalibrate,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun ThresholdRow(threshold: FruitThreshold, onEdit: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .whiteCard(FruitLogixTheme.shapes.Button)
            .clickable(role = Role.Button, onClick = onEdit)
            .padding(FruitLogixTheme.spacing.inner),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(id = R.drawable.ic_leaf, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
        Column(modifier = Modifier.weight(1f)) {
            Text(threshold.fruitName, style = FruitLogixTheme.typography.titleSmall, color = scheme.onSurface)
            Text(
                text = "${stringResource(R.string.sensors_temp_range, celsius(threshold.minTempCelsius), celsius(threshold.maxTempCelsius))} · " +
                    stringResource(R.string.sensors_humidity_range, threshold.minHumidityPercent.toInt(), threshold.maxHumidityPercent.toInt()),
                style = FruitLogixTheme.extraTypography.dataMonoSmall,
                color = scheme.onSurfaceVariant
            )
        }
        Text(stringResource(R.string.sensors_edit), style = FruitLogixTheme.typography.labelLarge, color = scheme.primary)
    }
}

@Composable
private fun EditThresholdDialog(
    threshold: FruitThreshold,
    error: String?,
    onSave: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    var minTemp by rememberSaveable(threshold.id) { mutableStateOf(threshold.minTempCelsius.toString()) }
    var maxTemp by rememberSaveable(threshold.id) { mutableStateOf(threshold.maxTempCelsius.toString()) }
    var minHum by rememberSaveable(threshold.id) { mutableStateOf(threshold.minHumidityPercent.toInt().toString()) }
    var maxHum by rememberSaveable(threshold.id) { mutableStateOf(threshold.maxHumidityPercent.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = FruitLogixTheme.shapes.Dialog,
        containerColor = scheme.surface,
        title = { Text(stringResource(R.string.sensors_edit_title, threshold.fruitName), style = FruitLogixTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.inner)
            ) {
                error?.let { Text(it, style = FruitLogixTheme.typography.bodyMedium, color = scheme.error) }
                LabeledTextField(label = stringResource(R.string.sensors_min_temp), value = minTemp, onValueChange = { minTemp = it }, keyboardType = KeyboardType.Decimal)
                LabeledTextField(label = stringResource(R.string.sensors_max_temp), value = maxTemp, onValueChange = { maxTemp = it }, keyboardType = KeyboardType.Decimal)
                LabeledTextField(label = stringResource(R.string.sensors_min_humidity), value = minHum, onValueChange = { minHum = it }, keyboardType = KeyboardType.Number)
                LabeledTextField(label = stringResource(R.string.sensors_max_humidity), value = maxHum, onValueChange = { maxHum = it }, keyboardType = KeyboardType.Number)
            }
        },
        confirmButton = { TextButton(onClick = { onSave(minTemp, maxTemp, minHum, maxHum) }) { Text(stringResource(R.string.sensors_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } }
    )
}
