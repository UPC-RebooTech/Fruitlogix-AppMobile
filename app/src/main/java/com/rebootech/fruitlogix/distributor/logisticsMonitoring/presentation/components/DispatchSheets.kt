package com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.FruitThreshold
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.ActiveDispatch
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.ColdChainCertificate
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DelayReason
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DeliveryProof
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DeliveryState
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.LotStageType
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules.LotTraceability
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules.ReeferRules
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.celsius
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.delayReasonLabel
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.deliveryStateLabel
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.LabeledTextField
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.components.SelectableChip
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.format.formatRelativeDateTime
import com.rebootech.fruitlogix.shared.ui.format.formatTime
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.time.LocalDate
import java.time.LocalTime

/** Error line inside a sheet. */
@Composable
fun SheetError(text: String?) {
    if (text == null) return
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FruitLogixTheme.shapes.Button)
            .background(scheme.errorContainer)
            .padding(FruitLogixTheme.spacing.inner),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(id = R.drawable.ic_warning, contentDescription = null, tint = scheme.onErrorContainer, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
        Text(text, style = FruitLogixTheme.typography.bodyMedium, color = scheme.onErrorContainer)
    }
}

// US14: order status --------------------------------------------------------

@Composable
fun StatusSheetContent(current: DeliveryState, error: String?, onSelect: (DeliveryState) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
        Text(stringResource(R.string.status_sheet_message), style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
        SheetError(error)
        val steps = listOf(DeliveryState.PENDING, DeliveryState.ON_ROUTE, DeliveryState.DELIVERED)
        steps.forEachIndexed { index, state ->
            val reached = steps.indexOf(current).let { it >= 0 && index <= it } || current == DeliveryState.DELIVERED
            val isCurrent = state == current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = FruitLogixTheme.sizing.fieldTouch)
                    .clip(FruitLogixTheme.shapes.Button)
                    .background(if (isCurrent) scheme.secondaryContainer else scheme.surfaceContainerLow)
                    .selectable(selected = isCurrent, role = Role.RadioButton, onClick = { onSelect(state) })
                    .padding(horizontal = FruitLogixTheme.spacing.inner),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (reached) scheme.primary else scheme.outlineVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (reached) AppIcon(id = R.drawable.ic_check, contentDescription = null, tint = scheme.onPrimary, modifier = Modifier.size(16.dp))
                    else Text("${index + 1}", style = FruitLogixTheme.typography.labelLarge, color = scheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
                Text(
                    text = stringResource(deliveryStateLabel(state)),
                    style = FruitLogixTheme.typography.titleSmall,
                    color = scheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (isCurrent) AppIcon(id = R.drawable.ic_check, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
            }
        }
        if (current == DeliveryState.REJECTED) {
            Text(stringResource(R.string.delivery_state_rejected), style = FruitLogixTheme.typography.titleSmall, color = scheme.error)
        }
    }
}

// US64: reefer ----------------------------------------------------------------

@Composable
fun ReeferSheetContent(
    productName: String,
    threshold: FruitThreshold?,
    currentReading: Double?,
    statusFor: (Double?) -> ReeferRules.Status?,
    onApprove: (Double?) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    var text by rememberSaveable { mutableStateOf(currentReading?.toString().orEmpty()) }
    val value = text.replace(',', '.').toDoubleOrNull()
    val status = statusFor(value)

    Column(verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.sm)) {
        threshold?.let {
            Text(
                text = stringResource(R.string.reefer_sheet_message, productName, celsius(it.minTempCelsius), celsius(it.maxTempCelsius)),
                style = FruitLogixTheme.typography.bodyLarge,
                color = scheme.onSurfaceVariant
            )
        }
        LabeledTextField(
            label = stringResource(R.string.reefer_reading),
            value = text,
            onValueChange = { text = it.filter { c -> c.isDigit() || c == '.' || c == ',' || c == '-' }.take(6) },
            leadingIconRes = R.drawable.ic_thermostat,
            error = if (value == null) stringResource(R.string.reefer_invalid) else null,
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
        )
        if (value != null && threshold != null) {
            val (message, ok) = when (status) {
                ReeferRules.Status.WITHIN_RANGE -> stringResource(R.string.reefer_ok) to true
                ReeferRules.Status.TOO_WARM -> stringResource(R.string.reefer_too_warm, celsius(value), celsius(threshold.maxTempCelsius)) to false
                ReeferRules.Status.TOO_COLD -> stringResource(R.string.reefer_too_cold, celsius(value), celsius(threshold.minTempCelsius)) to false
                null -> "" to false
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FruitLogixTheme.shapes.Button)
                    .background(if (ok) colors.successContainer else scheme.errorContainer)
                    .padding(FruitLogixTheme.spacing.inner),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(
                    id = if (ok) R.drawable.ic_verified else R.drawable.ic_warning,
                    contentDescription = null,
                    tint = if (ok) colors.onSuccessContainer else scheme.onErrorContainer,
                    modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                )
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                Text(message, style = FruitLogixTheme.typography.bodyMedium, color = if (ok) colors.onSuccessContainer else scheme.onErrorContainer)
            }
        }
        PrimaryButton(
            text = stringResource(R.string.reefer_approve),
            onClick = { onApprove(value) },
            enabled = status == ReeferRules.Status.WITHIN_RANGE,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// US17: delay -----------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DelaySheetContent(
    currentEta: LocalTime,
    error: String?,
    onSend: (LocalTime?, DelayReason?) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    var newEta by remember { mutableStateOf<LocalTime?>(null) }
    var reason by rememberSaveable { mutableStateOf<DelayReason?>(null) }
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val pickDescription = stringResource(R.string.delay_pick_time_cd)
    val today = LocalDate.now()

    Column(verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.sm)) {
        Text(
            text = stringResource(R.string.delay_sheet_message, formatTime(today.atTime(currentEta))),
            style = FruitLogixTheme.typography.bodyLarge,
            color = scheme.onSurfaceVariant
        )
        SheetError(error)
        Column {
            Text(stringResource(R.string.delay_new_eta), style = FruitLogixTheme.typography.titleSmall, color = scheme.onSurface)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = FruitLogixTheme.sizing.fieldTouch)
                    .clip(FruitLogixTheme.shapes.Input)
                    .background(scheme.surface)
                    .border(1.dp, scheme.outline, FruitLogixTheme.shapes.Input)
                    .clickable(role = Role.Button) { showPicker = true }
                    .semantics { contentDescription = pickDescription }
                    .padding(horizontal = FruitLogixTheme.spacing.inner),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(id = R.drawable.ic_schedule, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
                Text(
                    text = newEta?.let { formatTime(today.atTime(it)) } ?: stringResource(R.string.delay_pick_time),
                    style = FruitLogixTheme.typography.bodyLarge,
                    color = if (newEta != null) scheme.onSurface else scheme.onSurfaceVariant
                )
            }
        }
        Column {
            Text(stringResource(R.string.delay_reason), style = FruitLogixTheme.typography.titleSmall, color = scheme.onSurface)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)
            ) {
                DelayReason.entries.forEach { r ->
                    SelectableChip(label = stringResource(delayReasonLabel(r)), selected = reason == r, onClick = { reason = r })
                }
            }
        }
        PrimaryButton(text = stringResource(R.string.delay_send), onClick = { onSend(newEta, reason) }, modifier = Modifier.fillMaxWidth())
    }

    if (showPicker) {
        val start = newEta ?: currentEta.plusMinutes(30)
        val pickerState = rememberTimePickerState(initialHour = start.hour, initialMinute = start.minute)
        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    newEta = LocalTime.of(pickerState.hour, pickerState.minute)
                    showPicker = false
                }) { Text(stringResource(R.string.delay_time_ok)) }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text(stringResource(R.string.common_cancel)) } },
            text = { TimePicker(state = pickerState) }
        )
    }
}

// US16 / US52 / US56: delivery ------------------------------------------------

@Composable
fun DeliverySheetContent(
    destinationName: String,
    error: String?,
    onConfirm: (DeliveryProof) -> Unit,
    onReject: (String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    val current = remember { mutableStateListOf<Offset>() }
    var photo by rememberSaveable { mutableStateOf(false) }
    var outside by rememberSaveable { mutableStateOf(false) }
    var outsideConfirmed by rememberSaveable { mutableStateOf(false) }
    var rejecting by rememberSaveable { mutableStateOf(false) }
    var reason by rememberSaveable { mutableStateOf("") }
    val signatureDescription = stringResource(R.string.delivery_signature_cd)
    val lineColor = scheme.onSurface

    Column(verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.sm)) {
        SheetError(error)
        if (rejecting) {
            LabeledTextField(
                label = stringResource(R.string.delivery_reject_reason),
                value = reason,
                onValueChange = { reason = it },
                placeholder = stringResource(R.string.delivery_reject_placeholder),
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            )
            PrimaryButton(text = stringResource(R.string.delivery_reject_confirm), onClick = { onReject(reason) }, modifier = Modifier.fillMaxWidth())
            TextActionButton(text = stringResource(R.string.common_back), onClick = { rejecting = false }, modifier = Modifier.align(Alignment.CenterHorizontally))
            return@Column
        }

        // 1. Signature
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.delivery_step_signature), style = FruitLogixTheme.typography.titleSmall, color = scheme.onSurface, modifier = Modifier.weight(1f))
            if (strokes.isNotEmpty()) {
                TextActionButton(text = stringResource(R.string.delivery_signature_clear), onClick = { strokes.clear() })
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(FruitLogixTheme.shapes.Input)
                .background(scheme.surfaceContainerLow)
                .border(1.dp, scheme.outline, FruitLogixTheme.shapes.Input)
                .semantics { contentDescription = signatureDescription }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { current.clear(); current.add(it) },
                        onDragEnd = { strokes.add(current.toList()); current.clear() },
                        onDrag = { change, _ -> change.consume(); current.add(change.position) }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (strokes.isEmpty() && current.isEmpty()) {
                Text(stringResource(R.string.delivery_signature_hint), style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
            }
            Canvas(modifier = Modifier.fillMaxSize()) {
                (strokes + listOf(current.toList())).forEach { line ->
                    for (i in 1 until line.size) drawLine(lineColor, line[i - 1], line[i], strokeWidth = 5f, cap = StrokeCap.Round)
                }
            }
        }

        // 2. Photo (TODO(camera): open the camera and stamp date, time and GPS)
        Text(stringResource(R.string.delivery_step_photo), style = FruitLogixTheme.typography.titleSmall, color = scheme.onSurface)
        if (photo) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FruitLogixTheme.shapes.Button)
                    .background(colors.successContainer)
                    .padding(FruitLogixTheme.spacing.inner),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(id = R.drawable.ic_camera, contentDescription = null, tint = colors.onSuccessContainer, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                Text(stringResource(R.string.delivery_photo_taken), style = FruitLogixTheme.typography.bodyMedium, color = colors.onSuccessContainer)
            }
        } else {
            SecondaryButton(
                text = stringResource(R.string.delivery_take_photo),
                onClick = { photo = true },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { AppIcon(id = R.drawable.ic_camera, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)) }
            )
        }

        // 3. Location (TODO(gps): read the real position)
        Text(stringResource(R.string.delivery_step_location), style = FruitLogixTheme.typography.titleSmall, color = scheme.onSurface)
        if (outside) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FruitLogixTheme.shapes.Button)
                    .background(colors.attentionContainer)
                    .padding(FruitLogixTheme.spacing.inner)
            ) {
                Text(stringResource(R.string.delivery_location_outside, com.rebootech.fruitlogix.shared.ui.format.formatDecimal(1.8)), style = FruitLogixTheme.typography.bodyMedium, color = colors.onAttentionContainer)
                CheckLine(stringResource(R.string.delivery_location_confirm), outsideConfirmed) { outsideConfirmed = it }
            }
        } else {
            Text(stringResource(R.string.delivery_location_inside, destinationName), style = FruitLogixTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
        }
        CheckLine(stringResource(R.string.delivery_location_demo), outside) {
            outside = it
            outsideConfirmed = false
        }

        PrimaryButton(
            text = stringResource(R.string.delivery_confirm),
            onClick = {
                onConfirm(
                    DeliveryProof(
                        hasSignature = strokes.isNotEmpty(),
                        hasPhoto = photo,
                        insideDeliveryRadius = !outside,
                        outsideLocationConfirmed = outsideConfirmed
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        TextActionButton(
            text = stringResource(R.string.delivery_reject_link),
            onClick = { rejecting = true },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun CheckLine(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = FruitLogixTheme.sizing.minTouch)
            .clip(FruitLogixTheme.shapes.Button)
            .selectable(selected = checked, role = Role.Checkbox, onClick = { onChange(!checked) }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Checkbox(checked = checked, onCheckedChange = null)
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
        Text(label, style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurface)
    }
}

// US71: certificate -----------------------------------------------------------

@Composable
fun CertificateSheetContent(certificate: ColdChainCertificate?, onShare: (ColdChainCertificate) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    if (certificate == null) {
        Text(stringResource(R.string.certificate_no_data), style = FruitLogixTheme.typography.bodyLarge, color = scheme.error)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
        Text(
            text = stringResource(R.string.certificate_order, certificate.orderId, certificate.unitId, certificate.plate),
            style = FruitLogixTheme.typography.titleSmall,
            color = scheme.onSurface
        )
        Text("${certificate.productName} · ${certificate.route}", style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
        Text(
            text = stringResource(R.string.certificate_range, celsius(certificate.minCelsius), celsius(certificate.maxCelsius)),
            style = FruitLogixTheme.typography.bodyMedium,
            color = scheme.onSurface
        )
        Text(stringResource(R.string.certificate_readings, certificate.readingsCount), style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurface)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(FruitLogixTheme.shapes.Button)
                .background(if (certificate.isCompliant) colors.successContainer else colors.attentionContainer)
                .padding(FruitLogixTheme.spacing.inner)
        ) {
            Text(
                text = if (certificate.isCompliant) stringResource(R.string.certificate_compliant)
                else stringResource(R.string.certificate_excursions, certificate.excursions),
                style = FruitLogixTheme.typography.titleSmall,
                color = if (certificate.isCompliant) colors.onSuccessContainer else colors.onAttentionContainer
            )
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
        Text(stringResource(R.string.certificate_hash), style = FruitLogixTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
        Text(certificate.hash, style = FruitLogixTheme.extraTypography.dataMono, color = scheme.primary)
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        PrimaryButton(
            text = stringResource(R.string.certificate_share),
            onClick = { onShare(certificate) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { AppIcon(id = R.drawable.ic_share, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)) }
        )
    }
}

// US72: traceability ----------------------------------------------------------

@Composable
fun TraceabilitySheetContent(dispatch: ActiveDispatch) {
    val scheme = MaterialTheme.colorScheme
    val stages = LotTraceability.stages(dispatch)
    Column {
        Text(stringResource(R.string.trace_subtitle, dispatch.orderId), style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
        stages.forEachIndexed { index, stage ->
            Row(verticalAlignment = Alignment.Top) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (stage.done) scheme.primary else scheme.outlineVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (stage.done) AppIcon(id = R.drawable.ic_check, contentDescription = null, tint = scheme.onPrimary, modifier = Modifier.size(16.dp))
                    }
                    if (index < stages.lastIndex) {
                        Box(modifier = Modifier.width(2.dp).height(28.dp).background(if (stage.done) scheme.primary else scheme.outlineVariant))
                    }
                }
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
                Column(modifier = Modifier.padding(bottom = FruitLogixTheme.spacing.xs)) {
                    Text(
                        text = when (stage.type) {
                            LotStageType.HARVESTED -> stringResource(R.string.trace_harvested)
                            LotStageType.INSPECTED -> stringResource(R.string.trace_inspected)
                            LotStageType.LOADED -> stringResource(R.string.trace_loaded, dispatch.unitId)
                            LotStageType.RECEIVED -> stringResource(R.string.trace_received, dispatch.destinationName)
                            LotStageType.INVOICED -> stringResource(R.string.trace_invoiced)
                        },
                        style = FruitLogixTheme.typography.titleSmall,
                        color = if (stage.done) scheme.onSurface else scheme.onSurfaceVariant
                    )
                    Text(
                        text = when {
                            !stage.done || stage.at == null -> stringResource(R.string.trace_pending)
                            stage.type == LotStageType.HARVESTED -> "${formatRelativeDateTime(stage.at)} · ${dispatch.producerName}"
                            else -> formatRelativeDateTime(stage.at)
                        },
                        style = FruitLogixTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
