package com.rebootech.fruitlogix.distributor.qualityControl.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.RejectionKind
import com.rebootech.fruitlogix.distributor.qualityControl.domain.rules.FieldInspectionRules
import com.rebootech.fruitlogix.distributor.qualityControl.domain.rules.VoiceIncidentRules
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.LabeledTextField
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.SelectableChip
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import kotlinx.coroutines.delay

@Composable
fun QualitySheetError(text: String?) {
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

fun rejectionLabel(kind: RejectionKind): Int = when (kind) {
    RejectionKind.BRUISED -> R.string.qc_reason_bruised
    RejectionKind.OUT_OF_TEMPERATURE -> R.string.qc_reason_temperature
    RejectionKind.UNDERSIZE -> R.string.qc_reason_undersize
    RejectionKind.DECAY -> R.string.qc_reason_decay
    RejectionKind.OTHER -> R.string.qc_reason_other
}

// US10 ------------------------------------------------------------------------

@Composable
fun RejectLotSheet(error: String?, onReject: (RejectionKind?, String) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    var kind by rememberSaveable { mutableStateOf<RejectionKind?>(null) }
    var note by rememberSaveable { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.sm)) {
        Text(stringResource(R.string.qc_reject_message), style = FruitLogixTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
        QualitySheetError(error)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)
        ) {
            RejectionKind.entries.forEach { k ->
                SelectableChip(label = stringResource(rejectionLabel(k)), selected = kind == k, onClick = { kind = k })
            }
        }
        LabeledTextField(
            label = stringResource(R.string.qc_reason_note),
            value = note,
            onValueChange = { note = it },
            placeholder = stringResource(R.string.qc_reason_note_placeholder),
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done
        )
        PrimaryButton(text = stringResource(R.string.qc_reject_confirm), onClick = { onReject(kind, note) }, modifier = Modifier.fillMaxWidth())
    }
}

// US53 ------------------------------------------------------------------------

@Composable
fun FieldInspectionSheet(
    errors: Set<FieldInspectionRules.Error>,
    onSave: (lot: String, product: String, temp: String, humidity: String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    var lot by rememberSaveable { mutableStateOf("") }
    var product by rememberSaveable { mutableStateOf("") }
    var temp by rememberSaveable { mutableStateOf("") }
    var humidity by rememberSaveable { mutableStateOf("") }
    fun numeric(text: String) = text.filter { it.isDigit() || it == '.' || it == ',' || it == '-' }.take(6)

    Column(verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.sm)) {
        Text(stringResource(R.string.qc_field_message), style = FruitLogixTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
        LabeledTextField(
            label = stringResource(R.string.qc_field_lot),
            value = lot,
            onValueChange = { lot = it.uppercase() },
            placeholder = stringResource(R.string.qc_field_lot_placeholder),
            leadingIconRes = R.drawable.ic_receipt,
            error = if (FieldInspectionRules.Error.LOT_REQUIRED in errors) stringResource(R.string.qc_error_lot) else null,
            keyboardType = KeyboardType.Ascii,
            capitalization = KeyboardCapitalization.Characters
        )
        LabeledTextField(
            label = stringResource(R.string.qc_field_product),
            value = product,
            onValueChange = { product = it },
            placeholder = stringResource(R.string.dist_order_product_placeholder),
            leadingIconRes = R.drawable.ic_leaf,
            error = if (FieldInspectionRules.Error.PRODUCT_REQUIRED in errors) stringResource(R.string.qc_error_product) else null,
            capitalization = KeyboardCapitalization.Words
        )
        Row(horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
            LabeledTextField(
                label = stringResource(R.string.qc_field_temp),
                value = temp,
                onValueChange = { temp = numeric(it) },
                leadingIconRes = R.drawable.ic_thermostat,
                error = if (FieldInspectionRules.Error.TEMP_INVALID in errors) stringResource(R.string.qc_error_temp) else null,
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f)
            )
            LabeledTextField(
                label = stringResource(R.string.qc_field_humidity),
                value = humidity,
                onValueChange = { humidity = numeric(it) },
                leadingIconRes = R.drawable.ic_humidity,
                error = if (FieldInspectionRules.Error.HUMIDITY_INVALID in errors) stringResource(R.string.qc_error_humidity) else null,
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
                modifier = Modifier.weight(1f)
            )
        }
        PrimaryButton(text = stringResource(R.string.qc_field_save), onClick = { onSave(lot, product, temp, humidity) }, modifier = Modifier.fillMaxWidth())
    }
}

// US57 ------------------------------------------------------------------------

private fun mmss(seconds: Int) = String.format(java.util.Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60)

/**
 * Voice incident. The recorder is simulated (timer only).
 * TODO(audio): record with MediaRecorder after asking RECORD_AUDIO.
 */
@Composable
fun VoiceIncidentSheet(error: String?, onSave: (lot: String, description: String, seconds: Int?) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    var lot by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var recording by rememberSaveable { mutableStateOf(false) }
    var elapsed by rememberSaveable { mutableIntStateOf(0) }
    var recorded by rememberSaveable { mutableStateOf<Int?>(null) }
    var tooShort by rememberSaveable { mutableStateOf(false) }
    val buttonLabel = stringResource(if (recording) R.string.qc_voice_stop else R.string.qc_voice_start)

    LaunchedEffect(recording) {
        if (recording) {
            elapsed = 0
            while (recording) {
                delay(1_000)
                elapsed++
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.sm)) {
        Text(stringResource(R.string.qc_voice_message), style = FruitLogixTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
        QualitySheetError(error ?: if (tooShort) stringResource(R.string.qc_voice_too_short) else null)
        LabeledTextField(
            label = stringResource(R.string.qc_field_lot),
            value = lot,
            onValueChange = { lot = it.uppercase() },
            placeholder = stringResource(R.string.qc_field_lot_placeholder),
            leadingIconRes = R.drawable.ic_receipt,
            keyboardType = KeyboardType.Ascii,
            capitalization = KeyboardCapitalization.Characters
        )
        LabeledTextField(
            label = stringResource(R.string.qc_voice_description),
            value = description,
            onValueChange = { description = it },
            placeholder = stringResource(R.string.qc_voice_description_placeholder),
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(FruitLogixTheme.shapes.Card)
                .background(if (recording) scheme.errorContainer else scheme.secondaryContainer)
                .padding(FruitLogixTheme.spacing.inner),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledIconButton(
                onClick = {
                    if (!recording) {
                        tooShort = false
                        recording = true
                    } else {
                        recording = false
                        // US57, scenario 2: under one second the audio is discarded
                        if (VoiceIncidentRules.isValidRecording(elapsed)) recorded = elapsed else {
                            recorded = null
                            tooShort = true
                        }
                    }
                },
                modifier = Modifier
                    .size(FruitLogixTheme.sizing.fieldTouch)
                    .semantics { contentDescription = buttonLabel },
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (recording) scheme.error else scheme.primary,
                    contentColor = if (recording) scheme.onError else scheme.onPrimary
                )
            ) {
                AppIcon(id = if (recording) R.drawable.ic_close else R.drawable.ic_mic, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.icon))
            }
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
            Box(modifier = Modifier.weight(1f)) {
                Text(
                    text = when {
                        recording -> stringResource(R.string.qc_voice_recording, mmss(elapsed))
                        recorded != null -> stringResource(R.string.qc_voice_recorded, mmss(recorded!!))
                        else -> stringResource(R.string.qc_voice_idle)
                    },
                    style = FruitLogixTheme.typography.titleSmall,
                    color = if (recording) scheme.onErrorContainer else scheme.onSecondaryContainer
                )
            }
        }
        PrimaryButton(
            text = stringResource(R.string.qc_voice_save),
            onClick = { onSave(lot, description, recorded) },
            enabled = !recording,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
