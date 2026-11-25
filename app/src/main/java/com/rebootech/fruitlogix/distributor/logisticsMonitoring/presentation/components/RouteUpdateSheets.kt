package com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.ActiveDispatch
import com.rebootech.fruitlogix.shared.ui.components.LabeledTextField
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.util.Locale

/** Checkpoint on the road. With "add my GPS position" the phone's location goes with it. */
@Composable
fun CheckpointSheetContent(error: String?, busy: Boolean, onSave: (name: String, note: String, withGps: Boolean) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var withGps by rememberSaveable { mutableStateOf(true) }
    Column(verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.sm)) {
        Text(stringResource(R.string.checkpoint_message), style = FruitLogixTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SheetError(error)
        LabeledTextField(
            label = stringResource(R.string.checkpoint_name), value = name, onValueChange = { name = it.take(80) },
            placeholder = stringResource(R.string.checkpoint_name_placeholder), leadingIconRes = R.drawable.ic_truck_pin,
            capitalization = KeyboardCapitalization.Sentences
        )
        LabeledTextField(
            label = stringResource(R.string.checkpoint_note), value = note, onValueChange = { note = it.take(200) },
            leadingIconRes = R.drawable.ic_edit, capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done
        )
        CheckRow(stringResource(R.string.route_with_gps), withGps) { withGps = it }
        PrimaryButton(
            text = stringResource(if (busy) R.string.route_sending else R.string.checkpoint_save),
            onClick = { onSave(name, note, withGps) },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * GPS position + reefer reading (tracking). [currentCelsius] pre-fills the temperature with the
 * last sensor reading, so the person only checks it.
 */
@Composable
fun PositionSheetContent(currentCelsius: Double?, error: String?, busy: Boolean, onSend: (celsius: String, humidity: String) -> Unit) {
    var celsius by rememberSaveable { mutableStateOf(currentCelsius?.let { String.format(Locale.ROOT, "%.1f", it) }.orEmpty()) }
    var humidity by rememberSaveable { mutableStateOf("") }
    fun clean(text: String) = text.filterIndexed { i, c -> c.isDigit() || c == '.' || c == ',' || (c == '-' && i == 0) }.take(6)
    Column(verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.sm)) {
        Text(stringResource(R.string.position_message), style = FruitLogixTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SheetError(error)
        Row(horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
            LabeledTextField(
                label = stringResource(R.string.position_celsius), value = celsius, onValueChange = { celsius = clean(it) },
                placeholder = "5.0", keyboardType = KeyboardType.Text, modifier = Modifier.weight(1f)
            )
            LabeledTextField(
                label = stringResource(R.string.position_humidity), value = humidity, onValueChange = { humidity = clean(it) },
                placeholder = "88", keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done, modifier = Modifier.weight(1f)
            )
        }
        PrimaryButton(
            text = stringResource(if (busy) R.string.route_sending else R.string.position_send),
            onClick = { onSend(celsius, humidity) },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Another driver or truck takes the trip (before the arrival). Pre-filled with the current values. */
@Composable
fun AssignmentSheetContent(
    dispatch: ActiveDispatch,
    error: String?,
    busy: Boolean,
    onSave: (driverName: String, driverPhone: String, plate: String, vehicleType: String) -> Unit
) {
    var driver by rememberSaveable { mutableStateOf(dispatch.driver.name) }
    var phone by rememberSaveable { mutableStateOf(dispatch.driver.phone) }
    var plate by rememberSaveable { mutableStateOf(dispatch.plate) }
    var type by rememberSaveable { mutableStateOf(dispatch.truckModel) }
    Column(verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.sm)) {
        Text(stringResource(R.string.assignment_message), style = FruitLogixTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SheetError(error)
        LabeledTextField(
            label = stringResource(R.string.assignment_driver), value = driver, onValueChange = { driver = it },
            leadingIconRes = R.drawable.ic_person, capitalization = KeyboardCapitalization.Words
        )
        LabeledTextField(
            label = stringResource(R.string.assignment_phone), value = phone, onValueChange = { phone = it },
            leadingIconRes = R.drawable.ic_call, keyboardType = KeyboardType.Phone
        )
        LabeledTextField(
            label = stringResource(R.string.assignment_plate), value = plate, onValueChange = { plate = it.uppercase().take(7) },
            placeholder = "ABC-123", leadingIconRes = R.drawable.ic_id, capitalization = KeyboardCapitalization.Characters
        )
        LabeledTextField(
            label = stringResource(R.string.assignment_vehicle_type), value = type, onValueChange = { type = it },
            leadingIconRes = R.drawable.ic_fleet_truck, imeAction = ImeAction.Done
        )
        PrimaryButton(
            text = stringResource(if (busy) R.string.route_sending else R.string.assignment_save),
            onClick = { onSave(driver, phone, plate, type) },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = FruitLogixTheme.sizing.minTouch)
            .clickable(role = Role.Checkbox) { onChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onChange)
        Text(label, style = FruitLogixTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}
