package com.rebootech.fruitlogix.producer.qualityControl.harvests.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.validation.HarvestError
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.validation.HarvestField
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.validation.HarvestValidator
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.DetailSection
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.SelectableChip
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.format.formatRelativeDateTime
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.text.NumberFormat

/**
 * Register a harvest in the field.
 * Three short sections (what / how much and where / quality), one-tap choices for the
 * product and storage place, a lot code suggestion, and a fixed main button.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProducerRegisterHarvestScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: RegisterHarvestViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val input = state.input
    val savedMessage = stringResource(R.string.register_harvest_saved)

    LaunchedEffect(state.saved) {
        if (state.saved) {
            Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.background)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.inner)
        ) {
            BackHeader(
                title = stringResource(R.string.register_harvest_title),
                subtitle = stringResource(R.string.register_harvest_subtitle, formatRelativeDateTime(state.harvestedAt)),
                onBack = onNavigateBack
            )

            // 1. What
            DetailSection(title = stringResource(R.string.register_harvest_section_product)) {
                Spacer(modifier = Modifier.height(spacing.inner))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    state.productSuggestions.forEach { product ->
                        SelectableChip(
                            label = product,
                            selected = input.productName.equals(product, ignoreCase = true),
                            onClick = { viewModel.onInputChange(HarvestField.PRODUCT) { it.copy(productName = product) } }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(spacing.inner))
                FormField(
                    label = stringResource(R.string.register_harvest_product),
                    value = input.productName,
                    error = state.errors[HarvestField.PRODUCT],
                    capitalization = KeyboardCapitalization.Words,
                    onValueChange = { v -> viewModel.onInputChange(HarvestField.PRODUCT) { it.copy(productName = v) } }
                )
                Spacer(modifier = Modifier.height(spacing.inner))
                FormField(
                    label = stringResource(R.string.register_harvest_lot),
                    value = input.lotCode,
                    error = state.errors[HarvestField.LOT_CODE],
                    placeholder = stringResource(R.string.register_harvest_lot_hint),
                    capitalization = KeyboardCapitalization.Characters,
                    textStyle = FruitLogixTheme.extraTypography.dataMono,
                    onValueChange = { v -> viewModel.onInputChange(HarvestField.LOT_CODE) { it.copy(lotCode = v.uppercase()) } }
                )
                TextActionButton(
                    text = stringResource(R.string.register_harvest_lot_suggest),
                    onClick = viewModel::suggestLotCode,
                    leadingIcon = {
                        AppIcon(id = R.drawable.ic_flash, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
                    }
                )
            }

            // 2. How much and where
            DetailSection(title = stringResource(R.string.register_harvest_section_amount)) {
                Spacer(modifier = Modifier.height(spacing.inner))
                FormField(
                    label = stringResource(R.string.register_harvest_weight),
                    value = input.weightKg,
                    error = state.errors[HarvestField.WEIGHT],
                    suffix = stringResource(R.string.register_harvest_kg_suffix),
                    keyboardType = KeyboardType.Decimal,
                    textStyle = FruitLogixTheme.extraTypography.readingMono,
                    onValueChange = { v ->
                        viewModel.onInputChange(HarvestField.WEIGHT) {
                            it.copy(weightKg = v.filter { c -> c.isDigit() || c == '.' || c == ',' })
                        }
                    }
                )
                Spacer(modifier = Modifier.height(spacing.inner))
                Text(
                    text = stringResource(R.string.register_harvest_sector),
                    style = FruitLogixTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(spacing.xs))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    state.sectors.forEach { sector ->
                        SelectableChip(
                            label = sector,
                            selected = input.sector == sector,
                            onClick = { viewModel.onInputChange(HarvestField.SECTOR) { it.copy(sector = sector) } }
                        )
                    }
                }
            }

            // 3. Quality (optional)
            DetailSection(title = stringResource(R.string.register_harvest_section_quality)) {
                Spacer(modifier = Modifier.height(spacing.inner))
                FormField(
                    label = stringResource(R.string.register_harvest_brix),
                    value = input.brix,
                    error = state.errors[HarvestField.BRIX],
                    suffix = stringResource(R.string.register_harvest_brix_suffix),
                    keyboardType = KeyboardType.Decimal,
                    textStyle = FruitLogixTheme.extraTypography.dataMono,
                    onValueChange = { v ->
                        viewModel.onInputChange(HarvestField.BRIX) {
                            it.copy(brix = v.filter { c -> c.isDigit() || c == '.' || c == ',' })
                        }
                    }
                )
                Spacer(modifier = Modifier.height(spacing.inner))
                FormField(
                    label = stringResource(R.string.register_harvest_notes),
                    value = state.notes,
                    error = null,
                    placeholder = stringResource(R.string.register_harvest_notes_hint),
                    singleLine = false,
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default,
                    onValueChange = viewModel::onNotesChange
                )
            }
        }

        PrimaryButton(
            text = stringResource(R.string.register_harvest_save),
            onClick = viewModel::onSave,
            enabled = !state.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.sm, vertical = spacing.xs),
            leadingIcon = {
                AppIcon(id = R.drawable.ic_leaf, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
            }
        )
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    error: HarvestError?,
    onValueChange: (String) -> Unit,
    placeholder: String? = null,
    suffix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    imeAction: ImeAction = ImeAction.Next,
    singleLine: Boolean = true,
    textStyle: TextStyle = FruitLogixTheme.typography.bodyLarge
) {
    val scheme = MaterialTheme.colorScheme
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        suffix = suffix?.let { { Text(it, style = FruitLogixTheme.typography.titleSmall) } },
        isError = error != null,
        supportingText = error?.let { { Text(errorMessage(it)) } },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        textStyle = textStyle,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            capitalization = capitalization,
            imeAction = imeAction
        ),
        shape = FruitLogixTheme.shapes.Input,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = scheme.surfaceContainerLowest,
            unfocusedContainerColor = scheme.surfaceContainerLowest,
            unfocusedBorderColor = scheme.outline
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun errorMessage(error: HarvestError): String = when (error) {
    HarvestError.REQUIRED -> stringResource(R.string.register_harvest_error_required)
    HarvestError.LOT_TOO_SHORT -> stringResource(R.string.register_harvest_error_lot, HarvestValidator.MIN_LOT_LENGTH)
    HarvestError.WEIGHT_OUT_OF_RANGE -> stringResource(
        R.string.register_harvest_error_weight,
        NumberFormat.getNumberInstance(com.rebootech.fruitlogix.shared.ui.format.appLocale())
            .format(HarvestValidator.MAX_WEIGHT_KG.toInt())
    )
    HarvestError.BRIX_OUT_OF_RANGE -> stringResource(
        R.string.register_harvest_error_brix,
        com.rebootech.fruitlogix.shared.ui.format.formatDecimal(HarvestValidator.MAX_BRIX)
    )
}
