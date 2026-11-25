package com.rebootech.fruitlogix.producer.qualityControl.stock.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/** Form to publish a product in the producer's catalog (POST /products). */
@Composable
fun NewProductScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewProductViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val form = state.form

    LaunchedEffect(state.saved) { if (state.saved) onNavigateBack() }

    @Composable
    fun required(field: ProductField) =
        if (field in state.errors) stringResource(R.string.profile_error_required) else null

    @Composable
    fun number(field: ProductField) =
        if (field in state.errors) stringResource(R.string.prod_product_error_number) else null

    fun decimals(value: String) = value.filter { it.isDigit() || it == '.' || it == ',' }

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
                title = stringResource(R.string.prod_product_new),
                subtitle = stringResource(R.string.prod_product_new_subtitle),
                onBack = onNavigateBack
            )
            LabeledTextField(
                label = stringResource(R.string.prod_product_name), value = form.name,
                onValueChange = { v -> viewModel.onChange(ProductField.NAME) { it.copy(name = v) } },
                placeholder = stringResource(R.string.prod_product_name_placeholder),
                leadingIconRes = R.drawable.ic_leaf, error = required(ProductField.NAME),
                capitalization = KeyboardCapitalization.Words
            )
            LabeledTextField(
                label = stringResource(R.string.prod_product_fruit_type), value = form.fruitType,
                onValueChange = { v -> viewModel.onChange(ProductField.FRUIT_TYPE) { it.copy(fruitType = v) } },
                placeholder = stringResource(R.string.prod_product_fruit_type_placeholder),
                error = required(ProductField.FRUIT_TYPE), capitalization = KeyboardCapitalization.Words
            )
            LabeledTextField(
                label = stringResource(R.string.prod_product_variety), value = form.variety,
                onValueChange = { v -> viewModel.onChange(null) { it.copy(variety = v) } },
                capitalization = KeyboardCapitalization.Words
            )
            LabeledTextField(
                label = stringResource(R.string.prod_product_price), value = form.unitPrice,
                onValueChange = { v -> viewModel.onChange(ProductField.PRICE) { it.copy(unitPrice = decimals(v)) } },
                error = number(ProductField.PRICE), keyboardType = KeyboardType.Decimal
            )
            LabeledTextField(
                label = stringResource(R.string.prod_product_available), value = form.availableKg,
                onValueChange = { v -> viewModel.onChange(ProductField.AVAILABLE) { it.copy(availableKg = decimals(v)) } },
                error = number(ProductField.AVAILABLE), keyboardType = KeyboardType.Decimal
            )
            LabeledTextField(
                label = stringResource(R.string.prod_product_min_order), value = form.minOrderKg,
                onValueChange = { v -> viewModel.onChange(ProductField.MIN_ORDER) { it.copy(minOrderKg = decimals(v)) } },
                error = number(ProductField.MIN_ORDER), keyboardType = KeyboardType.Decimal
            )
            LabeledTextField(
                label = stringResource(R.string.prod_product_description), value = form.description,
                onValueChange = { v -> viewModel.onChange(null) { it.copy(description = v) } },
                capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done
            )
            state.serverError?.let { message ->
                Text(
                    text = message.ifBlank { stringResource(R.string.prod_product_save_failed) },
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = scheme.error
                )
            }
        }
        PrimaryButton(
            text = stringResource(if (state.isSaving) R.string.route_sending else R.string.prod_product_publish),
            onClick = viewModel::submit,
            enabled = !state.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.sm, vertical = spacing.xs)
        )
    }
}
