package com.rebootech.fruitlogix.orderManagement.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

@Composable
fun EditOrderScreen(
    onBackClick: () -> Unit,
    onOrderUpdated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditOrderViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = FruitLogixTheme.colors.textOnLight,
        unfocusedTextColor = FruitLogixTheme.colors.textOnLight,
        focusedLabelColor = FruitLogixTheme.colors.primary,
        unfocusedLabelColor = FruitLogixTheme.colors.textOnLight.copy(alpha = 0.65f),
        cursorColor = FruitLogixTheme.colors.primary,
        focusedBorderColor = FruitLogixTheme.colors.primary,
        unfocusedBorderColor = FruitLogixTheme.colors.textOnLight.copy(alpha = 0.35f)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FruitLogixTheme.colors.bg)
            .verticalScroll(rememberScrollState())
            .padding(FruitLogixTheme.spacing.sm)
    ) {
        TextButton(
            onClick = onBackClick
        ) {
            Text(
                text = stringResource(R.string.edit_order_back),
                color = FruitLogixTheme.colors.textOnLight
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.edit_order_title),
            style = FruitLogixTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = FruitLogixTheme.colors.textOnLight
        )

        Text(
            text = stringResource(
                R.string.edit_order_subtitle,
                state.orderId
            ),
            style = FruitLogixTheme.typography.bodyMedium,
            color = FruitLogixTheme.colors.textOnLight.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!state.isEditable && !state.isLoading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = FruitLogixTheme.shapes.Card,
                colors = CardDefaults.cardColors(
                    containerColor = FruitLogixTheme.colors.surfaceDark
                )
            ) {
                Text(
                    text = stringResource(R.string.edit_order_blocked),
                    color = FruitLogixTheme.colors.textOnDark,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = state.productName,
            onValueChange = viewModel::onProductChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(stringResource(R.string.create_order_product))
            },
            readOnly = !state.isEditable,
            isError = state.productError,
            colors = fieldColors,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.quantity,
            onValueChange = viewModel::onQuantityChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(stringResource(R.string.create_order_quantity))
            },
            suffix = {
                Text("t")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            readOnly = !state.isEditable,
            isError = state.quantityError,
            colors = fieldColors,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.requiredDate,
            onValueChange = viewModel::onRequiredDateChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(stringResource(R.string.create_order_date))
            },
            readOnly = !state.isEditable,
            isError = state.requiredDateError,
            colors = fieldColors,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            text = stringResource(R.string.edit_order_save),
            onClick = viewModel::saveChanges,
            enabled = state.isEditable,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (state.isSaved) {
        AlertDialog(
            onDismissRequest = {},
            shape = FruitLogixTheme.shapes.Card,
            containerColor = FruitLogixTheme.colors.surfaceDark,
            titleContentColor = FruitLogixTheme.colors.textOnDark,
            textContentColor = FruitLogixTheme.colors.textMuted,
            title = {
                Text(
                    text = stringResource(R.string.edit_order_success_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.edit_order_success_message)
                )
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PrimaryButton(
                        text = stringResource(R.string.edit_order_success_button),
                        onClick = onOrderUpdated,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}