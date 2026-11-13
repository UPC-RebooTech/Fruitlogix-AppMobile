package com.rebootech.fruitlogix.distributor.fleetManagement.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.DriverDraft
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleDraft
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleType
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.rules.FleetResourceRules
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.rules.FleetResourceRules.DriverError
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.rules.FleetResourceRules.VehicleError
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.LabeledTextField
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.SelectableChip
import com.rebootech.fruitlogix.shared.ui.format.appLocale
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Add a driver: name, phone, license category and expiry, cold-chain training. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddDriverSheet(errors: Set<DriverError>, onSave: (DriverDraft) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var licenseClass by rememberSaveable { mutableStateOf("A-IIIc") }
    var expiryEpochDay by rememberSaveable { mutableStateOf<Long?>(null) }
    var coldChain by rememberSaveable { mutableStateOf(false) }

    @Composable
    fun err(e: DriverError, res: Int) = if (e in errors) stringResource(res) else null

    ResourceSheet(title = stringResource(R.string.drv_add), onDismiss = onDismiss) {
        LabeledTextField(
            label = stringResource(R.string.drv_form_name), value = name, onValueChange = { name = it },
            leadingIconRes = R.drawable.ic_person, error = err(DriverError.NAME, R.string.drv_error_name),
            capitalization = KeyboardCapitalization.Words
        )
        LabeledTextField(
            label = stringResource(R.string.drv_form_phone), value = phone, onValueChange = { phone = it },
            placeholder = "+51 987 654 321", leadingIconRes = R.drawable.ic_call,
            error = err(DriverError.PHONE, R.string.drv_error_phone) ?: err(DriverError.PHONE_TAKEN, R.string.drv_error_phone_taken),
            keyboardType = KeyboardType.Phone
        )
        ChipChoice(
            label = stringResource(R.string.drv_form_license),
            options = FleetResourceRules.TRUCK_LICENSES.map { it to it },
            selected = licenseClass,
            onSelect = { licenseClass = it },
            error = err(DriverError.LICENSE_CLASS, R.string.drv_error_license_class)
        )
        DateChoice(
            label = stringResource(R.string.drv_form_license_expiry),
            date = expiryEpochDay?.let { LocalDate.ofEpochDay(it) },
            onDate = { expiryEpochDay = it.toEpochDay() },
            error = err(DriverError.LICENSE_EXPIRY, R.string.drv_error_license_expiry)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = FruitLogixTheme.sizing.minTouch)
                .clickable(role = Role.Checkbox) { coldChain = !coldChain },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = coldChain, onCheckedChange = { coldChain = it })
            Text(stringResource(R.string.drv_form_cold_chain), style = FruitLogixTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
        PrimaryButton(
            text = stringResource(R.string.drv_add_save),
            onClick = {
                onSave(DriverDraft(name, phone, licenseClass, expiryEpochDay?.let { LocalDate.ofEpochDay(it) }, coldChain))
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Add a vehicle: unit code, plate, type, capacity in kg and reefer range. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddVehicleSheet(errors: Set<VehicleError>, onSave: (VehicleDraft) -> Unit, onDismiss: () -> Unit) {
    var unitCode by rememberSaveable { mutableStateOf("") }
    var plate by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(VehicleType.REEFER_TRUCK) }
    var capacity by rememberSaveable { mutableStateOf("") }
    var minC by rememberSaveable { mutableStateOf("") }
    var maxC by rememberSaveable { mutableStateOf("") }

    @Composable
    fun err(e: VehicleError, res: Int) = if (e in errors) stringResource(res) else null
    fun number(text: String) = text.trim().replace(',', '.').toDoubleOrNull()
    // Digits, one decimal separator and a leading minus (reefers go below zero)
    fun cleanNumber(text: String, allowMinus: Boolean) =
        text.filterIndexed { i, c -> c.isDigit() || c == '.' || c == ',' || (allowMinus && c == '-' && i == 0) }.take(7)

    ResourceSheet(title = stringResource(R.string.veh_add), onDismiss = onDismiss) {
        LabeledTextField(
            label = stringResource(R.string.veh_form_unit), value = unitCode, onValueChange = { unitCode = it.uppercase().take(10) },
            placeholder = "FL-610", leadingIconRes = R.drawable.ic_fleet_truck,
            error = err(VehicleError.UNIT_CODE, R.string.veh_error_unit) ?: err(VehicleError.UNIT_CODE_TAKEN, R.string.veh_error_unit_taken),
            capitalization = KeyboardCapitalization.Characters
        )
        LabeledTextField(
            label = stringResource(R.string.veh_form_plate), value = plate, onValueChange = { plate = it.uppercase().take(7) },
            placeholder = "ABC-123", leadingIconRes = R.drawable.ic_id,
            error = err(VehicleError.PLATE, R.string.veh_error_plate) ?: err(VehicleError.PLATE_TAKEN, R.string.veh_error_plate_taken),
            capitalization = KeyboardCapitalization.Characters
        )
        ChipChoice(
            label = stringResource(R.string.veh_form_type),
            options = VehicleType.entries.map { it.name to stringResource(vehicleTypeLabel(it)) },
            selected = type.name,
            onSelect = { type = VehicleType.valueOf(it) }
        )
        LabeledTextField(
            label = stringResource(R.string.veh_form_capacity), value = capacity, onValueChange = { capacity = cleanNumber(it, false) },
            placeholder = "12000", leadingIconRes = R.drawable.ic_inventory,
            error = err(VehicleError.CAPACITY, R.string.veh_error_capacity), keyboardType = KeyboardType.Decimal
        )
        Row(horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
            LabeledTextField(
                label = stringResource(R.string.veh_form_min), value = minC, onValueChange = { minC = cleanNumber(it, true) },
                placeholder = "-1", keyboardType = KeyboardType.Text, modifier = Modifier.weight(1f)
            )
            LabeledTextField(
                label = stringResource(R.string.veh_form_max), value = maxC, onValueChange = { maxC = cleanNumber(it, true) },
                placeholder = "8", keyboardType = KeyboardType.Text, imeAction = ImeAction.Done, modifier = Modifier.weight(1f)
            )
        }
        err(VehicleError.TEMPERATURE, R.string.veh_error_temperature)?.let {
            Text(it, style = FruitLogixTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
        PrimaryButton(
            text = stringResource(R.string.veh_add_save),
            onClick = { onSave(VehicleDraft(unitCode, plate, type, number(capacity), number(minC), number(maxC))) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResourceSheet(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = scheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = FruitLogixTheme.spacing.md)
                .padding(bottom = FruitLogixTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.sm)
        ) {
            Text(title, style = FruitLogixTheme.typography.titleLarge, color = scheme.onSurface)
            Text(stringResource(R.string.fleet_resources_on_phone), style = FruitLogixTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
            content()
        }
    }
}

/** Label + one row of chips. [options] are (value, label). */
@Composable
private fun ChipChoice(
    label: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    error: String? = null
) {
    val scheme = MaterialTheme.colorScheme
    Column {
        Text(label, style = FruitLogixTheme.typography.titleSmall, color = if (error != null) scheme.error else scheme.onSurface)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)
        ) {
            options.forEach { (value, text) ->
                SelectableChip(label = text, selected = value == selected, onClick = { onSelect(value) })
            }
        }
        error?.let { Text(it, style = FruitLogixTheme.typography.bodySmall, color = scheme.error, modifier = Modifier.padding(top = 6.dp)) }
    }
}

/** Looks like a text field; tapping it opens the calendar. Only today or later can be picked. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateChoice(label: String, date: LocalDate?, onDate: (LocalDate) -> Unit, error: String?) {
    val scheme = MaterialTheme.colorScheme
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val format = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(appLocale())

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = FruitLogixTheme.typography.titleSmall, color = if (error != null) scheme.error else scheme.onSurface)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = FruitLogixTheme.sizing.fieldTouch)
                .clip(FruitLogixTheme.shapes.Input)
                .background(scheme.surface)
                .border(1.dp, if (error != null) scheme.error else scheme.outline, FruitLogixTheme.shapes.Input)
                .clickable(role = Role.Button) { showPicker = true }
                .padding(horizontal = FruitLogixTheme.spacing.inner),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(id = R.drawable.ic_schedule, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
            Text(
                text = date?.format(format) ?: stringResource(R.string.dist_order_date_placeholder),
                style = FruitLogixTheme.typography.bodyLarge,
                color = if (date != null) scheme.onSurface else scheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        error?.let { Text(it, style = FruitLogixTheme.typography.bodySmall, color = scheme.error, modifier = Modifier.padding(top = 6.dp)) }
    }

    if (showPicker) {
        val todayMillis = remember { LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }
        val state = rememberDatePickerState(
            initialSelectedDateMillis = (date ?: LocalDate.now().plusYears(1)).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= todayMillis
            }
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onDate(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }
                    showPicker = false
                }) { Text(stringResource(R.string.dist_order_date_ok)) }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text(stringResource(R.string.common_cancel)) } }
        ) {
            DatePicker(state = state)
        }
    }
}
