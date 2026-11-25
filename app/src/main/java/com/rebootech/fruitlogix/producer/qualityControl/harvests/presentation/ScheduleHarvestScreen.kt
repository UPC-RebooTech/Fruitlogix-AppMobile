package com.rebootech.fruitlogix.producer.qualityControl.harvests.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.LabeledTextField
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.format.appLocale
import com.rebootech.fruitlogix.shared.ui.format.formatTime
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** Form to plan a harvest (POST /harvest-plans). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleHarvestScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScheduleHarvestViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val locale = appLocale()
    var pickDate by rememberSaveable { mutableStateOf(false) }
    var pickTime by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.saved) { if (state.saved) onNavigateBack() }

    @Composable
    fun required(field: PlanField) = if (field in state.errors) stringResource(R.string.profile_error_required) else null

    @Composable
    fun number(field: PlanField) = if (field in state.errors) stringResource(R.string.prod_product_error_number) else null

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            BackHeader(
                title = stringResource(R.string.plan_title),
                subtitle = stringResource(R.string.plan_subtitle),
                onBack = onNavigateBack
            )
            LabeledTextField(
                label = stringResource(R.string.prod_product_fruit_type), value = state.fruitType,
                onValueChange = { v -> viewModel.onChange(PlanField.FRUIT) { it.copy(fruitType = v) } },
                placeholder = stringResource(R.string.prod_product_fruit_type_placeholder),
                leadingIconRes = R.drawable.ic_leaf, error = required(PlanField.FRUIT), capitalization = KeyboardCapitalization.Words
            )
            Text(stringResource(R.string.plan_when), style = FruitLogixTheme.typography.titleSmall, color = scheme.onSurface)
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                SecondaryButton(
                    text = state.date.format(DateTimeFormatter.ofPattern("EEE d MMM", locale)).replaceFirstChar { it.titlecase(locale) },
                    onClick = { pickDate = true },
                    modifier = Modifier.weight(1f)
                )
                SecondaryButton(
                    text = formatTime(LocalDateTime.of(state.date, state.time)),
                    onClick = { pickTime = true },
                    modifier = Modifier.weight(1f)
                )
            }
            if (PlanField.DATE in state.errors) {
                Text(stringResource(R.string.plan_error_past), style = FruitLogixTheme.typography.bodySmall, color = scheme.error)
            }
            LabeledTextField(
                label = stringResource(R.string.plan_yield), value = state.yieldKg,
                onValueChange = { v -> viewModel.onChange(PlanField.YIELD) { it.copy(yieldKg = v.filter { c -> c.isDigit() || c == '.' || c == ',' }) } },
                error = number(PlanField.YIELD), keyboardType = KeyboardType.Decimal
            )
            LabeledTextField(
                label = stringResource(R.string.plan_zone), value = state.zone,
                onValueChange = { v -> viewModel.onChange(PlanField.ZONE) { it.copy(zone = v) } },
                leadingIconRes = R.drawable.ic_gps, error = required(PlanField.ZONE), capitalization = KeyboardCapitalization.Words
            )
            LabeledTextField(
                label = stringResource(R.string.plan_crew), value = state.crewName,
                onValueChange = { v -> viewModel.onChange(PlanField.CREW) { it.copy(crewName = v) } },
                leadingIconRes = R.drawable.ic_person, error = required(PlanField.CREW), capitalization = KeyboardCapitalization.Words
            )
            LabeledTextField(
                label = stringResource(R.string.plan_pickers), value = state.pickers,
                onValueChange = { v -> viewModel.onChange(PlanField.PICKERS) { it.copy(pickers = v.filter(Char::isDigit).take(4)) } },
                error = number(PlanField.PICKERS), keyboardType = KeyboardType.Number
            )
            LabeledTextField(
                label = stringResource(R.string.prod_product_description), value = state.notes,
                onValueChange = { v -> viewModel.onChange(null) { it.copy(notes = v.take(500)) } },
                capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done
            )
            state.serverError?.let { message ->
                Text(
                    text = message.ifBlank { stringResource(R.string.plan_save_failed) },
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = scheme.error
                )
            }
        }
        PrimaryButton(
            text = stringResource(if (state.isSaving) R.string.route_sending else R.string.plan_submit),
            onClick = viewModel::submit,
            enabled = !state.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.sm, vertical = spacing.xs)
        )
    }

    if (pickDate) {
        val todayMillis = remember { LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= todayMillis
            }
        )
        DatePickerDialog(
            onDismissRequest = { pickDate = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val day = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        viewModel.onChange(PlanField.DATE) { it.copy(date = day) }
                    }
                    pickDate = false
                }) { Text(stringResource(R.string.dist_order_date_ok)) }
            },
            dismissButton = { TextButton(onClick = { pickDate = false }) { Text(stringResource(R.string.common_cancel)) } }
        ) { DatePicker(state = pickerState) }
    }

    if (pickTime) {
        val pickerState = rememberTimePickerState(initialHour = state.time.hour, initialMinute = state.time.minute)
        AlertDialog(
            onDismissRequest = { pickTime = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onChange(PlanField.DATE) { it.copy(time = LocalTime.of(pickerState.hour, pickerState.minute)) }
                    pickTime = false
                }) { Text(stringResource(R.string.delay_time_ok)) }
            },
            dismissButton = { TextButton(onClick = { pickTime = false }) { Text(stringResource(R.string.common_cancel)) } },
            text = { TimePicker(state = pickerState) }
        )
    }
}
