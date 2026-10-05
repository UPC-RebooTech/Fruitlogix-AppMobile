package com.rebootech.fruitlogix.orderManagement.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton

@Composable
fun CreateOrderScreen(
    onBackClick: () -> Unit,
    onOrderRegistered: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateOrderViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = FruitLogixTheme.colors.textOnLight,
        unfocusedTextColor = FruitLogixTheme.colors.textOnLight,
        focusedLabelColor = FruitLogixTheme.colors.primary,
        unfocusedLabelColor = FruitLogixTheme.colors.textOnLight.copy(alpha = 0.65f),
        focusedPlaceholderColor = FruitLogixTheme.colors.textOnLight.copy(alpha = 0.45f),
        unfocusedPlaceholderColor = FruitLogixTheme.colors.textOnLight.copy(alpha = 0.45f),
        cursorColor = FruitLogixTheme.colors.primary,
        focusedBorderColor = FruitLogixTheme.colors.primary,
        unfocusedBorderColor = FruitLogixTheme.colors.textOnLight.copy(alpha = 0.35f),
        errorTextColor = FruitLogixTheme.colors.textOnLight
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
                text = stringResource(R.string.create_order_back),
                color = FruitLogixTheme.colors.textOnLight
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.create_order_title),
            style = FruitLogixTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = FruitLogixTheme.colors.textOnLight
        )

        Text(
            text = stringResource(R.string.create_order_subtitle),
            style = FruitLogixTheme.typography.bodyMedium,
            color = FruitLogixTheme.colors.textOnLight.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = state.productName,
            onValueChange = viewModel::onProductChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(stringResource(R.string.create_order_product))
            },
            placeholder = {
                Text(stringResource(R.string.create_order_product_example))
            },
            isError = state.productError,
            supportingText = {
                if (state.productError) {
                    Text(stringResource(R.string.create_order_product_error))
                }
            },
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
            placeholder = {
                Text(stringResource(R.string.create_order_quantity_example))
            },
            suffix = {
                Text(
                    text = "t",
                    color = FruitLogixTheme.colors.textOnLight
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            isError = state.quantityError,
            supportingText = {
                if (state.quantityError) {
                    Text(stringResource(R.string.create_order_quantity_error))
                }
            },
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
            placeholder = {
                Text(stringResource(R.string.create_order_date_example))
            },
            isError = state.requiredDateError,
            supportingText = {
                if (state.requiredDateError) {
                    Text(stringResource(R.string.create_order_date_error))
                }
            },
            colors = fieldColors,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = viewModel::registerOrder,
            modifier = Modifier.fillMaxWidth(),
            shape = FruitLogixTheme.shapes.Pill,
            colors = ButtonDefaults.buttonColors(
                containerColor = FruitLogixTheme.colors.primary,
                contentColor = FruitLogixTheme.colors.onPrimary
            )
        ) {
            Text(
                text = "Registrar pedido",
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (state.isSuccess) {
        AlertDialog(
            onDismissRequest = {},
            shape = FruitLogixTheme.shapes.Card,
            containerColor = FruitLogixTheme.colors.surfaceDark,
            titleContentColor = FruitLogixTheme.colors.textOnDark,
            textContentColor = FruitLogixTheme.colors.textMuted,
            title = {
                Text(
                    text = stringResource(R.string.create_order_success_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.create_order_success_message,
                        state.registeredOrder?.id.orEmpty()
                    )
                )
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PrimaryButton(
                        text = stringResource(R.string.create_order_success_button),
                        onClick = onOrderRegistered,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}